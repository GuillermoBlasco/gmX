package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
trait LogFactor extends Serializable {

  def scope() : Set[Variable]
  def size() : Int
  def + (factor: LogFactor) : LogFactor
  def + (c: Double) : LogFactor

  /**
   * Let be f this log factor and g the given log factor. Then this method computes and returns
   * f - g that is h(x) = f(x) - g(x)
   * @param factor A log factor g.
   * @return Returns f - g.
   */
  def - (factor: LogFactor) : LogFactor
  def - (c: Double) : LogFactor
  def exp() : Factor
  /**
   * Let be f a factor and g the log of f, that is g(x) = log(f(x)). Then this method returns
   * h(x) = - g(x) that is h(x) = log(1/f(x))
   */
  def opposite() : LogFactor
  def marginal(variables: Set[Variable]) : LogFactor
  def marginalize(variables: Set[Variable]) : LogFactor
  def maxMarginal(variables: Set[Variable]) : LogFactor
  def maxMarginalize(variables: Set[Variable]) : LogFactor

  def update(assignment : Map[Variable, Int], value: Double)
  def apply(assignment : Map[Variable, Int]) : Double

}
object LogFactor {
  def apply(variable: Variable) : LogFactor = apply(Set[Variable](variable))
  def apply(variables: Variable*) : LogFactor = apply(variables.toSet)
  def apply(variables: Set[Variable]) : LogFactor = ArrayLogFactor(variables)
  def constantFactor(variables: Set[Variable], constant: Double) : LogFactor = ArrayLogFactor(variables, constant)
  def distance(phi1: LogFactor, phi2: LogFactor) : Double = {
    if (phi1.scope() != phi2.scope()) {
      throw new IllegalArgumentException(s"Distance for factors with scopes ${phi1.scope()} and ${phi2.scope()} is not legal.")
    }
    if (phi1.getClass.equals(classOf[ArrayLogFactor]) && phi2.getClass.equals(classOf[ArrayLogFactor])) {
      return ArrayLogFactor.distance(phi1.asInstanceOf[ArrayLogFactor], phi2.asInstanceOf[ArrayLogFactor])
    }
    throw new UnsupportedOperationException(s"Distance for factors of classes ${phi1.getClass} and ${phi2.getClass} is not supported.")
  }
}