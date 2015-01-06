package com.blackbox.gmx

import com.blackbox.gmx.impl.ClusterGraphImpl
import com.blackbox.gmx.model._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph

/**
 * Represents a cluster graph structure where vertex are set of variables and a factor with this scope
 * and edges the intersection of connected vertex.
 *
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Definition 10.1 (page 346)
 */
abstract class ClusterGraph protected () extends Serializable {

  /** Underlying raw graph */
  val graph: Graph[Factor, Set[Variable]]

  /** Factors contained in the graph */
  val factors : Set[Factor]

  /** Complete set of variables of the graph */
  val variables : Set[Variable]

  /** Calibrates the marginals of the graph */
  def calibrated(maxIters:Int = 10, epsilon:Double=0.1) : ClusterGraph

  /** Calibrates MAPs of the graph */
  def map(maxIterations :Int = 10, epsilon:Double=0.1): ClusterGraph

  /** Normalizes the cluster grpah factors */
  def normalized() : ClusterGraph

  /** Counts the number of clusters in the graph */
  def countClusters() : Long

}

object ClusterGraph {

  /**
   * Builds a Bethe Cluster Graph with given factors.
   * @param factors Factor set
   * @param sc Spark Context
   * @return Bethe Cluster Graph with given factors
   */
  def apply(factors: Set[Factor], sc : SparkContext) : ClusterGraph = {
    ClusterGraphImpl.bethe(factors, sc)
  }

  def apply
    (clusters: Map[Set[Variable], Set[Factor]],
     edges: Set[(Set[Variable], Set[Variable])],
     sc: SparkContext) : ClusterGraph = {
    ClusterGraphImpl(clusters, edges, sc)
  }

}
