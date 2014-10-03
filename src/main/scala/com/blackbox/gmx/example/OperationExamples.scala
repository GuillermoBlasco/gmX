package com.blackbox.gmx.example

import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model.Factor

/**
 * Created by guillermoblascojimenez on 01/10/14.
 */
object OperationExamples {

  def marginalizeAndMap(clusterGraph: ClusterGraph) = {
    println("Cluster Graph built")
    val clusterNumber = clusterGraph.graph.vertices.count()
    println(s"Cluster with $clusterNumber clusters")
    val calibrated = clusterGraph.calibrated(100, 0.000001)
    val mapCalibrated = clusterGraph.map(100, 0.000001)
    println(s"Calibrated")
    // print the posteriors
    println("marginals")
    var factors = calibrated.factors
    factors foreach ((factor: Factor) => println(factor.toString))
    println("maps")
    factors = mapCalibrated.factors
    factors foreach ((factor: Factor) => println(factor.toString))
  }
}
