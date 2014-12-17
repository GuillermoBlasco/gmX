package com.blackbox.gmx.impl

import com.blackbox.gmx.model.{Variable, Factor}
import org.apache.spark.graphx._
import org.apache.spark.Logging

/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 11.1 (page 397)
 */
object BeliefPropagation extends Logging {

  /*
   * Additional structure for BP
   */

  private class BPVertex (val factor: Factor, val potential: Factor) extends java.io.Serializable {

  }
  private object BPVertex {
    def apply(factor:Factor) : BPVertex = apply(factor, Factor.uniform(factor.scope()))
    def apply(factor:Factor, potential: Factor) : BPVertex = new BPVertex(factor, potential)
  }

  /*
   * Support methods for BP
   */

  // complete the vertex with incoming messages.
  // each vertex is the original Factor and a mapping of vertex to factors that are the incoming deltas.
  // Vertex i have the factor phi_i and a mapping j -> delta(j->i) where j takes values over the N(j)
  private def toBPGraph
    (graph: Graph[Factor, Set[Variable]])
    : Graph[BPVertex, Factor] = {
    graph.mapVertices((id, f) => BPVertex(f)).mapEdges((edge) => Factor.uniform(edge.attr))
  }

  private def toClusterGraph
    (graph: Graph[BPVertex, Factor])
    : Graph[Factor, Set[Variable]] = {
    graph.mapVertices((id, v) => (v.factor * v.potential).normalized()).mapEdges((edge) => edge.attr.scope())
  }

  private def reduceDeltas(d1: Factor, d2: Factor) : Factor = d1 * d2

  private def sumProjection(f: Factor, s: Set[Variable]) : Factor = f.marginal(s)
  private def maxProjection(f: Factor, s: Set[Variable]) : Factor = f.maxMarginal(s)

  /*
   * Core BP algorithm
   */
  def apply
    (projection: (Factor, Set[Variable]) => Factor,
     maxIterations : Int,
     epsilon : Double)
    (graph : Graph[Factor, Set[Variable]])
    : Graph[Factor, Set[Variable]] = {

    assert(maxIterations > 0)
    assert(epsilon > 0.0)

    println(s"BP epsilon $epsilon and maxIters $maxIterations")
    // deltas are set
    var g: Graph[BPVertex, Factor] = toBPGraph(graph).cache()

    var iteration: Int = 0
    var iterationError: Double = Double.PositiveInfinity

    do {
      println()
      println("==============================================")
      println(s"Iteration $iteration starts")

      // compute new deltas
      // for each edge i->j generate delta as
      //    i->j_potential := i_factor * i_potential / j->i_potential
      // Trick: for each edge i->j set as potential j_factor * j_potential / i->j_potential and then reverse all edges
      val newDeltas = g
        // for each node compute i_factor * i_potential
        .mapVertices((id, vertex) => vertex.factor * vertex.potential)
        // for each edge i->j compute j_factor * j_potential / i->j_potential
        .mapTriplets((triplet) => projection(triplet.dstAttr / triplet.attr, triplet.attr.scope()))
        // reverse so the edge j->i now contains j_factor * j_potential / i->j_potential
        .reverse
        .cache()

      // Compute new potentials and put them into a new graph
      // for each node i collect incoming edges and update as:
      //    i_potential := PRODUCT [j->i_potential, for j in N(i)]
      val newPotentials = newDeltas
        .mapReduceTriplets((triplet) => Iterator((triplet.dstId, triplet.attr)), reduceDeltas)

      val newG = newDeltas
        .outerJoinVertices(g.vertices)((id, vertex, phi) => BPVertex(phi.get.factor))
        .outerJoinVertices(newPotentials)((id, vertex, phi) => BPVertex(vertex.factor, phi.get))
        .cache()

      iterationError = newG.edges.innerJoin(g.edges)(
        (srcId, dstId, ij, ji) => Factor.distance(ij.normalized(), ji.normalized())
      ).aggregate(0.0)((e, errorEdge) => e + errorEdge.attr, _ + _)

      // unpersist things
      g.unpersistVertices(blocking = false)
      g.edges.unpersist(blocking = false)

      // update cached things
      g = newG
      iteration += 1
      println(s"Iteration ends with error $iterationError")
    } while (iteration < maxIterations && iterationError > epsilon)

    val outputGraph = toClusterGraph(g).cache()
    g.unpersistVertices(blocking = false)
    g.edges.unpersist(blocking = false)
    println(s"BP ended in $iteration iterations and $iterationError errors")
    outputGraph
  }

  def sum
    (maxIterations: Int,
     epsilon: Double)
    (graph : Graph[Factor, Set[Variable]]) : Graph[Factor, Set[Variable]] = apply(sumProjection, maxIterations, epsilon)(graph)
  def max
    (maxIterations: Int,
     epsilon: Double)
    (graph : Graph[Factor, Set[Variable]]) : Graph[Factor, Set[Variable]] = apply(maxProjection, maxIterations, epsilon)(graph)
}
