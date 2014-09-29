package com.blackbox.gmx.model

/**
 * Factor trait that
 */
trait Factor extends Serializable {

  def scope() : Set[Variable]
  def size() : Int = scope().foldLeft(1)((z,v) => z * v.cardinality)
  def * (factor: Factor) : Factor
  def * (c: Double) : Factor
  def / (c: Double) : Factor

  def update(assignment : Map[Variable, Int], value: Double)
  def apply(assignment : Map[Variable, Int]) : Double

}
object Factor {
  def apply(variables: Set[Variable]) : Factor = {
    TableFactor(variables)
  }
}
