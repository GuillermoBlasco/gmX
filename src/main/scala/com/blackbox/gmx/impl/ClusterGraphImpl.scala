package com.blackbox.gmx.impl

import scala.collection.mutable
import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model.{Variable, Factor}
import org.apache.spark.SparkContext
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

/**
 * Implements a cluster graph structure where vertex are set of variables and a factor with this scope
 * and edges the intersection of connected vertex.
 *
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Definition 10.1 (page 346)
 */
class ClusterGraphImpl(
                        override val graph: Graph[Factor,Set[Variable]]
                        ) extends ClusterGraph {

  def this() = this(null)
  override val factors: Set[Factor] = graph.vertices.aggregate(mutable.Set[Factor]())((s, v) => s + v._2, (s1, s2) => s1 ++ s2).toSet

  override val variables: Set[Variable] = graph.edges.aggregate(mutable.Set[Variable]())((s,e) => s ++ e.attr, (s1, s2) => s1 ++ s2).toSet

  override def calibrated(maxIterations :Int = 10, epsilon:Double = 0.1): ClusterGraph = {
    val g = BeliefPropagation.sum(maxIterations, epsilon)(graph)
    new ClusterGraphImpl(g).normalized()
  }

  override def map(maxIterations :Int = 10, epsilon:Double = 0.1): ClusterGraph = {
    val g = BeliefPropagation.max(maxIterations, epsilon)(graph)
    new ClusterGraphImpl(g).normalized()
  }

  override def normalized() : ClusterGraph = {
    val g = graph.mapVertices[Factor]((id, f) => f.normalized()).cache()
    new ClusterGraphImpl(g)
  }

  override def countClusters() : Long = {
    graph.vertices.count()
  }

}
object ClusterGraphImpl {
  /*
   * Builds a Bethe Cluster Graph.
   *
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Section 11.3.5.2 (page 405)
   */
  def apply(factors: Set[Factor], sc : SparkContext) : ClusterGraphImpl = {

    // Vertexs
    // here we are going to multiply all factors with same scope
    val variablesToFactor: mutable.Map[Set[Variable], Factor] = factors.foldLeft(mutable.Map[Set[Variable], Factor]())((m: mutable.Map[Set[Variable], Factor], f: Factor) => {
      if (m.contains(f.scope())) {
        m.put(f.scope(), m.get(f.scope()).get * f)
      } else {
        m.put(f.scope(), f)
      }
      m
    })
    // build the set of all variables
    val X: Set[Variable] = factors.aggregate(Set[Variable]())((s, f) => s ++ f.scope(), (s1,s2) => s1++s2).toSet
    // build the set of all needed set of variables (yes, a set of sets) that is the set of individuals variables union scopes of factors
    val U: Set[Set[Variable]] =
      X.foldLeft(Set[Set[Variable]]())((S, s) => S + Set[Variable](s)) ++ // individual variables
      factors.foldLeft(Set[Set[Variable]]())((S, f) => S + f.scope()) // scopes of factors
    // Add all missing factors to cluster graph
    U foreach ((u : Set[Variable]) => variablesToFactor.getOrElseUpdate(u, Factor.constantFactor(u, 1.0)))
    // now take the set of computed factors as clusters
    val clusters: Set[Factor] = variablesToFactor.values.toSet
    // index the clusters
    val vertex: Array[(VertexId, Factor)] = ((0 until clusters.size).map((i) => i.toLong) zip clusters.toList).toArray
    // parallellize the vertex
    val rddVertex : RDD[(VertexId, Factor)] = sc.parallelize(vertex)

    // Edges
    val edge : mutable.MutableList[Edge[Set[Variable]]] = new mutable.MutableList()
    vertex foreach (v1 => vertex foreach (v2 => {
      val x: Set[Variable] = v1._2.scope & v2._2.scope
      if (v1 != v2 && x.nonEmpty) {
        edge += Edge(v1._1, v2._1, x)
      }
      }))
    val rddEdge : RDD[Edge[Set[Variable]]] = sc.parallelize(edge)

    // Graph
    new ClusterGraphImpl(Graph[Factor, Set[Variable]](rddVertex, rddEdge, null))
  }
}
