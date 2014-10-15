package com.blackbox.gmx.impl

import com.blackbox.gmx.model.{Variable, Factor}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 11.1 (page 397)
 */
object BeliefPropagation {

  /*
   * Additional structure for BP
   */

  private class BPVertex (val factor: Factor, val deltas: Factor) extends java.io.Serializable {

  }
  private object BPVertex {
    def apply(factor:Factor) : BPVertex = apply(factor, Factor.constantFactor(factor.scope(), 1.0))
    def apply(factor:Factor, deltas: Factor) : BPVertex = new BPVertex(factor, deltas)
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
    graph.mapVertices[BPVertex]((id, f) => BPVertex(f)).mapEdges[Factor]((edge) => Factor.constantFactor(edge.attr, 1.0))
  }

  private def toClusterGraph
    (graph: Graph[BPVertex, Factor])
    : Graph[Factor, Set[Variable]] = {
    graph.mapVertices[Factor]((id, v) => v.factor * v.deltas).mapEdges[Set[Variable]]((edge) => edge.attr.scope())
  }

  private def reduceDeltas(d1: Factor, d2: Factor) : Factor = d1 * d2

  private def sumProjection(f: Factor, s: Set[Variable]) : Factor = f.marginal(s)
  private def maxProjection(f: Factor, s: Set[Variable]) : Factor = f.maxMarginal(s)

  /*
   * Core BP algorithm
   */
  private def apply
    (projection: (Factor, Set[Variable]) => Factor,
     maxIterations : Int,
     epsilon : Double)
    (graph : Graph[Factor, Set[Variable]])
    : Graph[Factor, Set[Variable]] = {

    assert(maxIterations > 0)
    assert(epsilon > 0.0)

    val g: Graph[BPVertex, Factor] = toBPGraph(graph).cache()

    var iteration: Int = 0
    var iterationError: Double = Double.PositiveInfinity

    do {

      // TODO: iteration

      // TODO: compute errors
      iterationError = Double.PositiveInfinity
      iteration += 1
    } while (iteration < maxIterations && iterationError > epsilon)

    val outputGraph = toClusterGraph(g).cache()
    g.unpersistVertices()
    g.edges.unpersist()
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
