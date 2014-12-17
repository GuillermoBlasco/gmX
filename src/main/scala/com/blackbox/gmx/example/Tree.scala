package com.blackbox.gmx.example

import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model.{Factor, Variable}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by guillermoblascojimenez on 21/10/14.
 */
object Tree {

  def main(args: Array[String]) = {
    val conf = new SparkConf()
      .setAppName("StudentChain")
      .setMaster("local[1]")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc: SparkContext = new SparkContext(conf)
    execute(sc)
  }

  def execute(sc: SparkContext) = {

    val clusterGraph = buildGraph(sc)
    OperationExamples.marginalizeAndMap(clusterGraph)
  }

  def buildGraph(sc : SparkContext) : ClusterGraph = {
    // VARIABLES
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    val c = Variable("c", 2)
    val d = Variable("d", 2)
    val e = Variable("e", 2)

    // FACTORS
    val phi1 = Factor(a)
    phi1(Map(a -> 0)) = 0.6
    phi1(Map(a -> 1)) = 0.4
    val phi2 = Factor(b)
    phi2(Map(b -> 0)) = 0.2
    phi2(Map(b -> 1)) = 0.8
    val phi3 = Factor(a, b)
    phi3(Map(a -> 0, b -> 0)) = 0.1
    phi3(Map(a -> 1, b -> 0)) = 0.9
    phi3(Map(a -> 0, b -> 1)) = 0.7
    phi3(Map(a -> 1, b -> 1)) = 0.3
    val phi4 = Factor(b, c)
    phi4(Map(c -> 0, b -> 0)) = 0.9
    phi4(Map(c -> 1, b -> 0)) = 0.1
    phi4(Map(c -> 0, b -> 1)) = 0.75
    phi4(Map(c -> 1, b -> 1)) = 0.25
    val phi5 = Factor(e, d)
    phi5(Map(e -> 0, d -> 0)) = 0.5
    phi5(Map(e -> 1, d -> 0)) = 0.5
    phi5(Map(e -> 0, d -> 1)) = 0.9
    phi5(Map(e -> 1, d -> 1)) = 0.1
    val phi6 = Factor(b, e)
    phi6(Map(e -> 0, b -> 0)) = 0.2
    phi6(Map(e -> 1, b -> 0)) = 0.8
    phi6(Map(e -> 0, b -> 1)) = 0.05
    phi6(Map(e -> 1, b -> 1)) = 0.95



    val clusters = Map[Set[Variable], Set[Factor]](
      Set(a) -> Set(phi1),
      Set(b) -> Set(phi2),
      Set(a, b) -> Set(phi3),
      Set(b, c) -> Set(phi4),
      Set(e, d) -> Set(phi5),
      Set(b, e) -> Set(phi6)
    )
    
    val edges = Set[(Set[Variable], Set[Variable])](
      (Set(a)    , Set(a, b)), // intersection: a
      (Set(b)    , Set(a, b)), // intersection: b
      (Set(a, b) , Set(b, c)), // intersection: b
      (Set(b, c) , Set(b, e)), // intersection: b
      (Set(b, e) , Set(e, d))  // intersection: e
    )

    ClusterGraph(clusters, edges, sc)
  }

}
