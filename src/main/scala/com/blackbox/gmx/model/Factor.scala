package com.blackbox.gmx.model

/**
 * Factor trait that
 */
trait Factor extends Serializable {

  def scope() : Set[Variable]
  def size() : Int
  def * (factor: Factor) : Factor
  def * (c: Double) : Factor
  def / (c: Double) : Factor
  def log() : LogFactor
  def marginal(variable: Variable) : Factor
  def marginal(variables: Set[Variable]) : Factor

  def update(assignment : Map[Variable, Int], value: Double)
  def apply(assignment : Map[Variable, Int]) : Double

}
object Factor {
  def apply(variable: Variable) : Factor = apply(Set[Variable](variable))
  def apply(variables: Variable*) : Factor = apply(variables.toSet)
  def apply(variables: Set[Variable]) : Factor = FactorTable(variables)
  def constantFactor(variables: Set[Variable], constant: Double) : Factor = FactorTable(variables, constant)
}
