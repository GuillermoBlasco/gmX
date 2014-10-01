package com.blackbox.gmx.example

import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model.{ArrayFactor, Factor, Variable}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Figure 3.4 (page 53)
 */
object Student {

  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("student")
    val sc: SparkContext = new SparkContext(conf)

    val clusterGraph = buildGraph(sc)

    println("Cluster Graph built")
    val clusterNumber = clusterGraph.graph.vertices.count()
    assert(clusterNumber == 8)
    println(s"Cluster with $clusterNumber clusters")
    val calibrated = clusterGraph.calibrated()
    println(s"Calibrated")
    // print the posteriors
    val factors = calibrated.factors
    factors foreach ((factor: Factor) => println(factor.toString))
  }

  def buildGraph(sc: SparkContext) : ClusterGraph = {
    // VARIABLES
    val difficulty    = Variable[String]("DIFFICULTY", 2)
    val intelligence = Variable[String]("INTELLIGENCE", 2)
    val grade      = Variable[String]("GRADE", 3)
    val sat  = Variable[String]("SAT", 2)
    val letter  = Variable[String]("LETTER", 2)

    // FACTORS
    val difficultyFactor = Factor(difficulty)
    difficultyFactor(Map(difficulty -> 0)) = 0.6
    difficultyFactor(Map(difficulty -> 1)) = 0.4
    val intelligenceFactor = Factor(intelligence)
    intelligenceFactor(Map(intelligence -> 0)) = 0.7
    intelligenceFactor(Map(intelligence -> 1)) = 0.3
    val satGivenIntelligence = Factor(sat, intelligence)
    satGivenIntelligence(Map(sat -> 0, intelligence -> 0)) = 0.95
    satGivenIntelligence(Map(sat -> 1, intelligence -> 0)) = 0.05
    satGivenIntelligence(Map(sat -> 0, intelligence -> 1)) = 0.2
    satGivenIntelligence(Map(sat -> 1, intelligence -> 1)) = 0.8
    val gradeGivenDifficultyIntelligence = Factor(grade, difficulty, intelligence)
    gradeGivenDifficultyIntelligence(Map(grade -> 0, difficulty -> 0, intelligence -> 0)) = 0.3
    gradeGivenDifficultyIntelligence(Map(grade -> 1, difficulty -> 0, intelligence -> 0)) = 0.4
    gradeGivenDifficultyIntelligence(Map(grade -> 2, difficulty -> 0, intelligence -> 0)) = 0.3
    gradeGivenDifficultyIntelligence(Map(grade -> 0, difficulty -> 1, intelligence -> 0)) = 0.05
    gradeGivenDifficultyIntelligence(Map(grade -> 1, difficulty -> 1, intelligence -> 0)) = 0.25
    gradeGivenDifficultyIntelligence(Map(grade -> 2, difficulty -> 1, intelligence -> 0)) = 0.7
    gradeGivenDifficultyIntelligence(Map(grade -> 0, difficulty -> 0, intelligence -> 1)) = 0.9
    gradeGivenDifficultyIntelligence(Map(grade -> 1, difficulty -> 0, intelligence -> 1)) = 0.08
    gradeGivenDifficultyIntelligence(Map(grade -> 2, difficulty -> 0, intelligence -> 1)) = 0.02
    gradeGivenDifficultyIntelligence(Map(grade -> 0, difficulty -> 1, intelligence -> 1)) = 0.5
    gradeGivenDifficultyIntelligence(Map(grade -> 1, difficulty -> 1, intelligence -> 1)) = 0.3
    gradeGivenDifficultyIntelligence(Map(grade -> 2, difficulty -> 1, intelligence -> 1)) = 0.2
    val letterGivenGrade = Factor(letter, grade)
    letterGivenGrade(Map(letter -> 0, grade -> 0)) = 0.1
    letterGivenGrade(Map(letter -> 1, grade -> 0)) = 0.9
    letterGivenGrade(Map(letter -> 0, grade -> 1)) = 0.4
    letterGivenGrade(Map(letter -> 1, grade -> 1)) = 0.6
    letterGivenGrade(Map(letter -> 0, grade -> 2)) = 0.99
    letterGivenGrade(Map(letter -> 1, grade -> 2)) = 0.01

    val factors: Set[Factor] = Set[Factor](difficultyFactor, intelligenceFactor, satGivenIntelligence, gradeGivenDifficultyIntelligence, letterGivenGrade)
    ClusterGraph(factors, sc)
  }
}
