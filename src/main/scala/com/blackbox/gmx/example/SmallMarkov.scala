package com.blackbox.gmx.example

import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model.{Factor, Variable}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Figure 11.15 (page 438)
 */
object SmallMarkov {

  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("sprinkler")
    val sc: SparkContext = new SparkContext(conf)

    val clusterGraph: ClusterGraph = buildGraph(sc)

    OperationExamples.marginalizeAndMap(clusterGraph)
  }
  def buildGraph(sc: SparkContext) : ClusterGraph = {
    // VARIABLES
    val a    = Variable[String]("a", 2)
    val b = Variable[String]("b", 2)
    val c      = Variable[String]("c", 2)
    val d  = Variable[String]("d", 2)

    // FACTORS
    val phi1 = Factor(a,b)
    phi1(Map(a -> 0, b -> 0)) = 10
    phi1(Map(a -> 1, b -> 0)) = 0.1
    phi1(Map(a -> 0, b -> 1)) = 0.1
    phi1(Map(a -> 1, b -> 1)) = 10
    val phi2 = Factor(a,c)
    phi2(Map(a -> 0, c -> 0)) = 5
    phi2(Map(a -> 1, c -> 0)) = 0.2
    phi2(Map(a -> 0, c -> 1)) = 0.2
    phi2(Map(a -> 1, c -> 1)) = 5
    val phi3 = Factor(b,d)
    phi3(Map(b -> 0, d -> 0)) = 5
    phi3(Map(b -> 1, d -> 0)) = 0.2
    phi3(Map(b -> 0, d -> 1)) = 0.2
    phi3(Map(b -> 1, d -> 1)) = 5
    val phi4 = Factor(c,d)
    phi4(Map(c -> 0, d -> 0)) = 0.5
    phi4(Map(c -> 1, d -> 0)) = 20
    phi4(Map(c -> 0, d -> 1)) = 1
    phi4(Map(c -> 1, d -> 1)) = 2.5


    val factors: Set[Factor] = Set[Factor](phi1, phi2, phi3, phi4)
    ClusterGraph(factors, sc)
  }
}
