package com.blackbox.gmx.exampledata

import com.blackbox.gmx.GMX
import com.blackbox.gmx.model.{TableFactor, SingleVariableFactor, VariableOrFactor}
import com.blackbox.gmx.model.api.{NamedVariable, Variable}
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
object Sprinkler {

  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("sprinkler")
    val sc: SparkContext = new SparkContext(conf)


    val cloudy    = new NamedVariable(1L, 2, "CLOUDY")
    val sprinkler = new NamedVariable(2L, 2, "SPRINKLER")
    val rain      = new NamedVariable(3L, 2, "RAIN")
    val wetGrass  = new NamedVariable(4L, 2, "WET_GRASS")
    val cloudyFactor = new SingleVariableFactor(
      1L,
      cloudy,
      Array(0.5, 0.5)
    )
    val sprinklerGivenCloudyFactor = new TableFactor(
      2L,
      Array(sprinkler, cloudy),
      Array(0.5, 0.5, 0.9, 0.1)
    )
    val rainGivenCloudyFactor = new TableFactor(
      3L,
      Array(rain, cloudy),
      Array(0.8, 0.2, 0.2, 0.8)
    )
    val wetGrassGivenSprinklerAndRainFactor = new TableFactor(
      4L,
      Array(wetGrass, sprinkler, rain),
      Array(1, 0, 0.1, 0.9, 0.1, 0.9, 0.01, 0.99)
    )

    val nodes: RDD[(VertexId, VariableOrFactor)] =
    sc.parallelize(Array(
      (1L, new VariableOrFactor(cloudy)),
      (2L, new VariableOrFactor(sprinkler)),
      (3L, new VariableOrFactor(rain)),
      (4L, new VariableOrFactor(wetGrass)),
      (5L, new VariableOrFactor(cloudyFactor)),
      (6L, new VariableOrFactor(sprinklerGivenCloudyFactor)),
      (7L, new VariableOrFactor(rainGivenCloudyFactor)),
      (8L, new VariableOrFactor(wetGrassGivenSprinklerAndRainFactor))
    ))
    val edges: RDD[Edge[Nothing]] =
    sc.parallelize(Array(
      Edge(1L, 5L),
      Edge(5L, 1L),
      Edge(1L, 6L),
      Edge(6L, 1L),
      Edge(1L, 7L),
      Edge(7L, 1L),
      Edge(2L, 6L),
      Edge(6L, 2L),
      Edge(2L, 8L),
      Edge(8L, 2L),
      Edge(3L, 7L),
      Edge(7L, 3L),
      Edge(3L, 8L),
      Edge(8L, 3L),
      Edge(4L, 8L),
      Edge(8L, 4L)
    ))

    // prints kilometers of logs and numbers in two lines:
    // 4
    // [logs]
    // 4
    val graph: Graph[VariableOrFactor, Nothing] = Graph[VariableOrFactor, Nothing](nodes, edges, new VariableOrFactor())
    val gmx: GMX = new GMX()
    println(gmx.countFactors(graph))
    println(gmx.countVariables(graph))
  }

}
