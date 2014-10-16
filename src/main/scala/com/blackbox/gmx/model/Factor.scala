package com.blackbox.gmx.model

/**
 * Factor trait that
 */
trait Factor extends Serializable {

  def scope() : Set[Variable]
  def size() : Int
  def * (factor: Factor) : Factor
  def * (c: Double) : Factor
  def / (factor: Factor) : Factor
  def / (c: Double) : Factor
  def log() : LogFactor
  def inverse() : Factor
  def marginal(variables: Set[Variable]) : Factor
  def marginalize(variables: Set[Variable]) : Factor
  def maxMarginal(variables: Set[Variable]) : Factor
  def maxMarginalize(variables: Set[Variable]) : Factor

  def update(assignment : Map[Variable, Int], value: Double)
  def apply(assignment : Map[Variable, Int]) : Double
  def z() : Double
  def normalized() : Factor

}
object Factor {
  def apply(variable: Variable) : Factor = apply(Set[Variable](variable))
  def apply(variables: Variable*) : Factor = apply(variables.toSet)
  def apply(variables: Set[Variable]) : Factor = ArrayFactor(variables)
  def constantFactor(variables: Set[Variable], constant: Double) : Factor = ArrayFactor(variables, constant)
  def uniform(variables: Set[Variable]) : Factor = ArrayFactor(variables, 1.0).normalized()
  def distance(phi1: Factor, phi2: Factor) : Double = {
    if (phi1.scope() != phi2.scope()) {
      throw new IllegalArgumentException(s"Distance for factors with scopes ${phi1.scope()} and ${phi2.scope()} is not legal.")
    }
    if (phi1.getClass.equals(classOf[ArrayFactor]) && phi2.getClass.equals(classOf[ArrayFactor])) {
      return ArrayFactor.distance(phi1.asInstanceOf[ArrayFactor], phi2.asInstanceOf[ArrayFactor])
    }
    throw new UnsupportedOperationException(s"Distance for factors of classes ${phi1.getClass} and ${phi2.getClass} is not supported.")
  }
}
