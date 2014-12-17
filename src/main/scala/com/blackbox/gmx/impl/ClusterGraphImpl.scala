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
  def bethe(factors: Set[Factor], sc : SparkContext) : ClusterGraphImpl = {

    // get the scopes of all factors
    val scopes = factors.map(factor => factor.scope()).toSet
    // generate the unary scopes. eg X = {x,y,z} => {{x}, {y}, {z}}
    val unaryScopes = scopes.aggregate(Set[Variable]())(_ ++ _, _ ++ _).map(v => Set(v))
    // the clusters is the join of actual scopes and unary scopes
    val clusters = aggergateFactors(factors.groupBy((f) => f.scope())) ++ unaryScopes.map(s => s -> Factor.uniform(s)).toMap

    val edges:mutable.HashSet[(Set[Variable], Set[Variable])] = mutable.HashSet()

    unaryScopes foreach(unaryScope => {
      scopes foreach (scope => {
        if (scope.intersect(unaryScope).nonEmpty) {
          edges += ((unaryScope, scope))
        }
      })
    })

    applyRaw(clusters, processEdges(edges.toSet), sc)
  }

  def apply
  (clusters: Map[Set[Variable], Set[Factor]],
   edges: Set[(Set[Variable], Set[Variable])],
   sc: SparkContext) : ClusterGraphImpl = {

    val aggClusters = clusters map {
      case (scope, factorSet) => {
        (scope,
          factorSet.aggregate(Factor.constant(scope, 1.0))(_ * _, _ * _)
          )
      }
    }
    applyRaw(aggClusters, processEdges(edges), sc)
  }

  private def aggergateFactors(f: Map[Set[Variable],Set[Factor]]) : Map[Set[Variable],Factor] = {
    f.map({
      case (scope, factorSet) => (scope, factorSet.aggregate(Factor.constant(scope, 1.0))(_ * _, _ * _))
    })
  }

  private def processEdges(edges: Set[(Set[Variable], Set[Variable])]) : List[(Set[Variable], Set[Variable])] = {
    val edgeList : mutable.MutableList[(Set[Variable], Set[Variable])] = new mutable.MutableList()
    edges foreach (e => {
      edgeList += e
      edgeList += e.swap
    })
    edgeList.toList
  }

  private def applyRaw
    (clusters: Map[Set[Variable], Factor],
    // Edges as a set of pairs of nodes, where a pair is a set and each node is also a set.
     edges: List[(Set[Variable], Set[Variable])],
     sc:SparkContext) : ClusterGraphImpl = {
    // generate the vertexs as (id, factor) pairs where each factor works represents a cluster.
    val vertexs: Array[(VertexId, Factor)] = ((0 until clusters.size).map((i) => i.toLong) zip clusters.values.toList).toArray

    vertexs.map(v => v._2 -> v._1).toMap
    val vertexMap : Map[Set[Variable], VertexId] = vertexs.map(v => v._2.scope() -> v._1).toMap
    println(vertexMap)
    // generate edges as the intersection of clusters
    val newEdges = edges.map({
      case (set1, set2) => Edge(vertexMap(set1), vertexMap(set2), set1.intersect(set2))
    })
    println(newEdges)

    // generate RDD
    val rddEdge : RDD[Edge[Set[Variable]]] = sc.parallelize(newEdges.toList)
    val rddVertex : RDD[(VertexId, Factor)] = sc.parallelize(vertexs)

    // return cluster graph structure
    new ClusterGraphImpl(Graph[Factor, Set[Variable]](rddVertex, rddEdge, null))
  }

}
