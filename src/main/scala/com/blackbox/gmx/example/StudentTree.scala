package com.blackbox.gmx.example

import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model.{Factor, Variable}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by guillermoblascojimenez on 21/10/14.
 */
object StudentTree {

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
    val difficulty    = Variable("DIFFICULTY", 2)
    val intelligence = Variable("INTELLIGENCE", 2)
    val grade      = Variable("GRADE", 3)
    val sat  = Variable("SAT", 2)
    val letter  = Variable("LETTER", 2)

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

    val clusters = Map[Set[Variable], Set[Factor]](
      Set[Variable](difficulty) -> Set[Factor](difficultyFactor),
      Set[Variable](grade, difficulty, intelligence) -> Set[Factor](gradeGivenDifficultyIntelligence),
      Set[Variable](sat, intelligence) -> Set[Factor](satGivenIntelligence, intelligenceFactor),
      Set[Variable](grade, letter) -> Set[Factor](letterGivenGrade)
    )

    val edges = Set[(Set[Variable], Set[Variable])](
      (Set(difficulty)    , Set(grade, difficulty, intelligence)),
      (Set(sat, intelligence)    , Set(grade, difficulty, intelligence)),
      (Set(grade, letter) , Set(grade, difficulty, intelligence))
    )
    ClusterGraph(clusters, edges, sc)
  }

}
