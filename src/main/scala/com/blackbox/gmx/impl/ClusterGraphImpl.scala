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

  /**
   * Builds a Bethe Cluster Graph.
   *
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Section 11.3.5.2 (page 405)
   */
  def apply(factors: Set[Factor], sc : SparkContext) : ClusterGraphImpl = {

    // get the scopes of all factors
    val scopes = factors.map(factor => factor.scope()).toSet
    // generate the unary scopes. eg X = {x,y,z} => {{x}, {y}, {z}}
    val unaryScopes = scopes.aggregate(Set[Variable]())(_ ++ _, _ ++ _).map(v => Set(v))
    // the clusters is the join of actual scopes and unary scopes
    val clusters = scopes ++ unaryScopes

    apply(clusters, factors, sc)
  }

  /**
   * Generates a new cluster graph with the topology given by the clusters. There have to be
   * for each factor a cluster that matches exactly its scope.
   * @param clusters
   * @param factors
   * @param sc
   * @return
   */
  def apply(clusters: Set[Set[Variable]], factors: Set[Factor], sc: SparkContext) : ClusterGraphImpl = {

    // set of all variables
    val X: Set[Variable] = clusters.aggregate(Set[Variable]())(_ ++ _, _ ++ _).toSet

    // put in each cluster a factor
    val clusterToFactor: mutable.Map[Set[Variable], Factor] = mutable.Map[Set[Variable], Factor]()
    // start with uniform factors
    clusters foreach (cluster => clusterToFactor.put(cluster, Factor.uniform(cluster)))
    // and then put each factor in its cluster
    factors foreach (factor => clusterToFactor.update(factor.scope(), clusterToFactor.get(factor.scope()).get * factor))

    applyRaw(clusterToFactor.toMap, sc)
  }

  def apply(clusters: Map[Set[Variable], Set[Factor]], sc: SparkContext) : ClusterGraphImpl = {
    val aggClusters = clusters map {
      case (scope, factorSet) => (scope, factorSet.aggregate(Factor.constantFactor(scope, 1.0))(_ * _, _ * _))
    }
    applyRaw(aggClusters, sc)
  }

  private def applyRaw(clusters: Map[Set[Variable], Factor], sc:SparkContext) : ClusterGraphImpl = {

    // generate the vertexs as (id, factor) pairs where each factor works represents a cluster.
    val vertex: Array[(VertexId, Factor)] = ((0 until clusters.size).map((i) => i.toLong) zip clusters.values.toList).toArray

    // generate edges as the intersection of clusters
    val edge : mutable.MutableList[Edge[Set[Variable]]] = new mutable.MutableList()
    vertex foreach (v1 => vertex foreach (v2 => {
      val x: Set[Variable] = v1._2.scope() & v2._2.scope()
      if (v1 != v2 && x.nonEmpty) {
        edge += Edge(v1._1, v2._1, x)
      }
    }))

    // generate RDD
    val rddEdge : RDD[Edge[Set[Variable]]] = sc.parallelize(edge.toList)
    val rddVertex : RDD[(VertexId, Factor)] = sc.parallelize(vertex)

    // return cluster graph structure
    new ClusterGraphImpl(Graph[Factor, Set[Variable]](rddVertex, rddEdge, null))
  }

}
