package com.blackbox.gmx.example

import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model._
import org.apache.spark._

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
object Sprinkler {

  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("sprinkler")
    val sc: SparkContext = new SparkContext(conf)

    // VARIABLES
    val cloudy    = Variable[String]("CLOUDY", 2)
    val sprinkler = Variable[String]("SPRINKLER", 2)
    val rain      = Variable[String]("RAIN", 2)
    val wetGrass  = Variable[String]("WET_GRASS", 2)

    // FACTORS
    val cloudyFactor = Factor(cloudy)
    cloudyFactor(Map(cloudy -> 0)) = 0.5
    cloudyFactor(Map(cloudy -> 1)) = 0.5
    val sprinklerGivenCloudyFactor = Factor(sprinkler, cloudy)
    sprinklerGivenCloudyFactor(Map(sprinkler -> 0, cloudy -> 0)) = 0.5
    sprinklerGivenCloudyFactor(Map(sprinkler -> 1, cloudy -> 0)) = 0.5
    sprinklerGivenCloudyFactor(Map(sprinkler -> 0, cloudy -> 1)) = 0.9
    sprinklerGivenCloudyFactor(Map(sprinkler -> 1, cloudy -> 1)) = 0.1
    val rainGivenCloudyFactor = Factor(rain, cloudy)
    rainGivenCloudyFactor(Map(rain -> 0, cloudy -> 0)) = 0.8
    rainGivenCloudyFactor(Map(rain -> 1, cloudy -> 0)) = 0.2
    rainGivenCloudyFactor(Map(rain -> 0, cloudy -> 1)) = 0.2
    rainGivenCloudyFactor(Map(rain -> 1, cloudy -> 1)) = 0.8
    val wetGrassGivenSprinklerAndRainFactor = Factor(wetGrass, sprinkler, rain)
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 0, sprinkler -> 0, rain -> 0)) = 1.0
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 1, sprinkler -> 0, rain -> 0)) = 0.0
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 0, sprinkler -> 1, rain -> 0)) = 0.1
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 1, sprinkler -> 1, rain -> 0)) = 0.9
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 0, sprinkler -> 0, rain -> 1)) = 0.1
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 1, sprinkler -> 0, rain -> 1)) = 0.9
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 0, sprinkler -> 1, rain -> 1)) = 0.01
    wetGrassGivenSprinklerAndRainFactor(Map(wetGrass -> 1, sprinkler -> 1, rain -> 1)) = 0.99

    val factors: Set[Factor] = Set[Factor](cloudyFactor, sprinklerGivenCloudyFactor, rainGivenCloudyFactor, wetGrassGivenSprinklerAndRainFactor)
    val clusterGraph: ClusterGraph = ClusterGraph(factors, sc)
    println("Cluster Graph built")
    val clusterNumber = clusterGraph.graph.vertices.count()
    assert(clusterNumber == 7)
    println(s"Cluster with $clusterNumber clusters")
  }

}
