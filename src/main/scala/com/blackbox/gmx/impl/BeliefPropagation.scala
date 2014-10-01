package com.blackbox.gmx.impl

import com.blackbox.gmx.model.{Variable, Factor}
import org.apache.spark.graphx._

/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 11.1 (page 397)
 */
class BeliefPropagation {

}
object BeliefPropagation {

  class BPVertex (val factor: Factor, var deltas: Map[VertexId, Factor]) extends java.io.Serializable {
    // builds a new BP vertex with updated deltas
    def updateDeltas(newDeltas: Map[VertexId, Factor]) : BPVertex = new BPVertex(factor, newDeltas)
    // multiplies deltas but the one coming from j
    def packDeltasFor(j:VertexId) : Factor = deltas.aggregate[Factor](Factor.emptyFactor(1.0))(
      { case (f, (id, d)) => if (id != j) f * d else f },
      { case (f1, f2) => f1 * f2 }
    )
  }

  def apply(maxIterations : Int = 10)(graph : Graph[Factor, Set[Variable]]) : Graph[Factor, Set[Variable]] = {
    // complete the vertex with incoming messages.
    // each vertex is the original Factor and a mapping of vertex to factors that are the incoming deltas.
    // Vertex i have the factor phi_i and a mapping j -> delta(j->i) where j takes values over the N(j)
    val g = graph.mapVertices[BPVertex]((id, f) => new BPVertex(f, Map[VertexId, Factor]()))
      // vertex process: update deltas
    val vertexProcess : (VertexId, BPVertex, Map[VertexId, Factor]) => BPVertex =
        (vId, vertex, newDeltas) => vertex.updateDeltas(newDeltas) // just replace with new deltas
    // delta src -> dst
    val deltaMessage : (EdgeTriplet[BPVertex, Set[Variable]]) => Iterator[(VertexId, Map[VertexId, Factor])] =
      (triplet) => Iterator((triplet.dstId, Map[VertexId, Factor](
          // delta srcId -> dstId is src factor * deltas to srcId (but the one coming from dstId)
          triplet.srcId -> (triplet.srcAttr.factor * triplet.srcAttr.packDeltasFor(triplet.dstId)).marginal(triplet.attr).normalized()
    )))
    // deltas aggregation
    val deltaAggregation : (Map[VertexId, Factor], Map[VertexId, Factor]) => Map[VertexId, Factor] = _ ++ _

    //Initial empty message
    val calibrated = g.pregel[Map[VertexId,Factor]](Map.empty[VertexId, Factor], maxIterations)(
      vertexProcess,
      deltaMessage,
      deltaAggregation
    )
    val output: Graph[Factor, Set[Variable]] = calibrated.mapVertices({
      case (id ,vertex) => vertex.factor * vertex.deltas.values.aggregate[Factor](Factor.emptyFactor(1.0))((d1,d2) => d1 * d2,(d1,d2) => d1 * d2)
    })
    output
  }
}
