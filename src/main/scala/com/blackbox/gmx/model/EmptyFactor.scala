package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
protected class EmptyFactor(val value: Double) extends Factor with LogFactor {
  override def scope(): Set[Variable] = Set[Variable]()

  override def z(): Double = value

  override def update(assignment: Map[Variable, Int], value: Double): Unit = {
    assert(assignment isEmpty)
    new EmptyFactor(value)
  }

  override def log(): LogFactor = new EmptyFactor(Math.log(value))

  override def /(c: Double): Factor = {
    assert(c > 0)
    new EmptyFactor(value / c)
  }

  override def size(): Int = 0

  override def normalized(): Factor = new EmptyFactor(1)

  override def apply(assignment: Map[Variable, Int]): Double = {
    assert(assignment.isEmpty)
    value
  }


  override def *(factor: Factor): Factor = {
    assert(factor != null)
    factor * value
  }

  override def *(c: Double): Factor = {
    assert(c > 0)
    new EmptyFactor(value * c)
  }

  override def +(factor: LogFactor): LogFactor = {
    assert(factor != null)
    factor + value
  }

  override def exp(): Factor = new EmptyFactor(Math.exp(value))

  override def +(c: Double): LogFactor = {
    assert(!(c.isNaN || c.isInfinity))
    new EmptyFactor(value + c)
  }

  override def -(c: Double): LogFactor = {
    assert(!(c.isNaN || c.isInfinity))
    new EmptyFactor(value - c)
  }

  override def marginalize(variables: Set[Variable]): Factor = {
    assert(variables isEmpty)
    new EmptyFactor(value)
  }
  override def marginal(variables: Set[Variable]): Factor = {
    assert(variables isEmpty)
    new EmptyFactor(value)
  }
  override def maxMarginal(variables: Set[Variable]): Factor = {
    assert(variables isEmpty)
    new EmptyFactor(value)
  }

  override def maxMarginalize(variables: Set[Variable]): Factor = {
    assert(variables isEmpty)
    new EmptyFactor(value)
  }

  override def toString : String = s"EmptyFactor with value $value"
}
object EmptyFactor {

  def distance(phi1:EmptyFactor, phi2:EmptyFactor) : Double = {
    Math.abs(phi1.value - phi2.value)
  }

}
