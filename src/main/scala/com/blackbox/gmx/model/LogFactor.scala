package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
trait LogFactor extends Serializable {

  def scope() : Set[Variable]
  def size() : Int
  def + (factor: LogFactor) : LogFactor
  def + (c: Double) : LogFactor
  def - (c: Double) : LogFactor
  def exp() : Factor

  def update(assignment : Map[Variable, Int], value: Double)
  def apply(assignment : Map[Variable, Int]) : Double

}
object LogFactor {
  def apply(variable: Variable) : LogFactor = apply(Set[Variable](variable))
  def apply(variables: Variable*) : LogFactor = apply(variables.toSet)
  def apply(variables: Set[Variable]) : LogFactor = ArrayLogFactor(variables)
  def constantFactor(variables: Set[Variable], constant: Double) : LogFactor = if (variables.isEmpty) emptyFactor(constant) else ArrayLogFactor(variables, constant)
  def emptyFactor(value:Double) : LogFactor = new EmptyFactor(value)
}