package com.blackbox.gmx

import com.blackbox.gmx.impl.ClusterGraphImpl
import com.blackbox.gmx.model._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph
import org.apache.spark.rdd.RDD

/**
 * Created by guillermoblascojimenez on 27/09/14.
 */
abstract class ClusterGraph protected () extends Serializable {

  val graph: Graph[Factor, Set[Variable]]
  val factors : Set[Factor]
  val variables : Set[Variable]

  def calibrate() : ClusterGraph

}
object ClusterGraph {
  def apply(factors: Set[Factor], sc : SparkContext) : ClusterGraph = {
    ClusterGraphImpl(factors, sc)
  }
}
