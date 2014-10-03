package com.blackbox.gmx.impl

import com.blackbox.gmx.model.{Variable, Factor}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import scala.collection.mutable

/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 11.1 (page 397)
 */
object BeliefPropagation {

  /*
   * Additional structure for BP
   */

  private class BPVertex (val factor: Factor, var deltas: Map[VertexId, Factor]) extends java.io.Serializable {
    // multiplies deltas but the one coming from j cluster
    def packDeltasFor(j:VertexId) : Factor = factor * deltas.aggregate[Factor](Factor.emptyFactor(1.0))(
      { case (f, (id, d)) => if (id != j) f * d else f }, _ * _
    )
    def aggregate() : Factor = factor * deltas.values.aggregate[Factor](Factor.emptyFactor(1.0))(_ * _, _ * _)
  }
  private object BPVertex {
    def apply(factor:Factor) : BPVertex = apply(factor, Map[VertexId, Factor]())
    def apply(factor:Factor, deltas:Map[VertexId, Factor]) : BPVertex = new BPVertex(factor, deltas)
  }

  /*
   * Support methods for BP
   */

  // complete the vertex with incoming messages.
  // each vertex is the original Factor and a mapping of vertex to factors that are the incoming deltas.
  // Vertex i have the factor phi_i and a mapping j -> delta(j->i) where j takes values over the N(j)
  private def toBPGraph(graph: Graph[Factor, Set[Variable]]) : Graph[BPVertex, Set[Variable]] = graph.mapVertices[BPVertex]((id, f) => BPVertex(f))

  // builds a new BP vertex with updated deltas
  private def vertexProcess(id:VertexId, vertex: BPVertex, newDeltas: Map[VertexId, Factor]) : BPVertex = BPVertex(vertex.factor, newDeltas)

  private def deltaAggregation(d1: Map[VertexId, Factor], d2: Map[VertexId, Factor]) : Map[VertexId, Factor] = d1 ++ d2

  private def sumDeltaMessage(triplet: EdgeTriplet[BPVertex, Set[Variable]]) : Iterator[(VertexId, Map[VertexId, Factor])] =
    Iterator((triplet.dstId, Map[VertexId, Factor](
      // delta srcId -> dstId is src factor * deltas to srcId (but the one coming from dstId)
      triplet.srcId -> triplet.srcAttr.packDeltasFor(triplet.dstId).marginal(triplet.attr).normalized()
    )))

  private def maxDeltaMessage(triplet: EdgeTriplet[BPVertex, Set[Variable]]) : Iterator[(VertexId, Map[VertexId, Factor])] =
    Iterator((triplet.dstId, Map[VertexId, Factor](
      // delta srcId -> dstId is src factor * deltas to srcId (but the one coming from dstId)
      triplet.srcId -> triplet.srcAttr.packDeltasFor(triplet.dstId).maxMarginal(triplet.attr).normalized()
    )))


  /*
   * Computes the error of each step
   */
  private def stopFunction(
                            epsilon:Double
                            )(
                            deltas: RDD[((VertexId, Map[VertexId, Factor]),(VertexId, Map[VertexId, Factor]))]
                            ) : Boolean = {
    val error: Double = deltas.aggregate(0.0)(
      (error, v) => {
        assert(v._1._1 == v._2._1) // old and news are matched
        val vertexId = v._1._1
        var vertexError = 0.0
        v._1._2.keys foreach((k) =>
          {vertexError = vertexError + Factor.distance(v._1._2(k).normalized(), v._2._2(k).normalized()) }
        )
        vertexError
      },
      (e1, e2) => e1 + e2
    )
    println(s"error: $error")
    error < epsilon
  }

  /*
   * Core BP algorithm
   */
  private def apply(
      deltaMessage: (EdgeTriplet[BPVertex, Set[Variable]]) => Iterator[(VertexId, Map[VertexId, Factor])],
      maxIterations : Int,
      epsilon : Double
    )(
      graph : Graph[Factor, Set[Variable]]
    ) : Graph[Factor, Set[Variable]] = {

    val g = toBPGraph(graph)

    //Initial empty message
    val calibrated = Pregel[BPVertex, Set[Variable], Map[VertexId,Factor]](
      g,
      Map.empty[VertexId, Factor],
      maxIterations,
      EdgeDirection.Either,
      stopFunction(epsilon)
    )(
      vertexProcess,
      deltaMessage,
      deltaAggregation
    )

    val output: Graph[Factor, Set[Variable]] = calibrated.mapVertices((id ,vertex) => vertex.aggregate())
    output
  }
  def sum(maxIterations: Int, epsilon: Double)(graph : Graph[Factor, Set[Variable]]) : Graph[Factor, Set[Variable]] = apply(sumDeltaMessage, maxIterations, epsilon)(graph)
  def max(maxIterations: Int, epsilon: Double)(graph : Graph[Factor, Set[Variable]]) : Graph[Factor, Set[Variable]] = apply(maxDeltaMessage, maxIterations, epsilon)(graph)
}
