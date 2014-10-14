package com.blackbox.gmx.model

import scala.collection.immutable

/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 358)
 */
class ArrayFactor(
                             override val scope : immutable.Set[Variable],
                             override val strides: immutable.Map[Variable, Int],
                             override val values: Array[Double]
                             ) extends AbstractArrayFactor(scope, strides, values) with Factor {


  override def *(factor: Factor): Factor = {
    assert(factor != null)
    factor match {
      case phi2: ArrayFactor =>
        ArrayFactor.product(this, phi2)
      case phi2: EmptyFactor =>
        this * phi2.value
      case _ =>
        throw new UnsupportedOperationException(s"Can not multiply factor of class ${factor.getClass} with this factor of class ${this.getClass}")
    }
  }

  override def *(c: Double): Factor = {
    assert(c > 0)
    new ArrayFactor(scope, strides, values.transform((v : Double) => v * c).toArray)
  }

  override def /(c: Double): Factor = {
    assert(c > 0)
    new ArrayFactor(scope, strides, values.transform((v : Double) => v / c).toArray)
  }

  override def log(): LogFactor = new ArrayLogFactor(scope, strides, values.transform((v: Double) => Math.log(v)).toArray)

  override def z() : Double = values.aggregate[Double](0.0)((a,b) => a+b, (a,b) => a+b)

  override def normalized() : Factor = this / z()

  override def marginalize(variables: Set[Variable]): Factor = {
    assert((variables diff scope).isEmpty)
    val X: Set[Variable] = scope diff variables
    val phi: ArrayFactor = ArrayFactor(X, 0.0)
    for (i <- 0 until this.size) {
      val assignment = this.assignmentOfIndex(i)
      phi(assignment) = phi(assignment) + this.values(i)
    }
    phi
  }

  override def marginal(variables: Set[Variable]) : Factor = {
    assert((variables diff scope).isEmpty)
    marginalize(scope diff variables)
  }
  def maxMarginal(variables: Set[Variable]) : Factor = {
    assert((variables diff scope).isEmpty)
    maxMarginalize(scope diff variables)
  }
  def maxMarginalize(variables: Set[Variable]) : Factor = {
    assert((variables diff scope).isEmpty)
    val X: Set[Variable] = scope diff variables
    val phi: ArrayFactor = ArrayFactor(X, 0.0)
    for (i <- 0 until this.size) {
      val assignment = this.assignmentOfIndex(i)
      phi(assignment) = Math.max(phi(assignment), this.values(i))
    }
    phi
  }

  override def toString : String = s"ArrayFactor [${super.toString}]"
}
protected object ArrayFactor {
  private val op : (Double, Double) => Double = (a, b) => a * b

  def apply(variables: Set[Variable], defaultValue: Double = 0.0) : ArrayFactor = {
    val size = variables.foldLeft(1)((z,v) => z * v.cardinality)
    new ArrayFactor(variables, AbstractArrayFactor.computeStrides(variables), Array.fill(size)(defaultValue))
  }

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 10.A.1 (page 359)
   */
  def product(phi1: ArrayFactor, phi2: ArrayFactor) : ArrayFactor = {
    val X1 = phi1.scope
    val X2 = phi2.scope
    val X: Set[Variable] = X1 ++ X2
    val psi: ArrayFactor = ArrayFactor(X)
    AbstractArrayFactor.product(phi1, phi2, psi, op)
  }

  /*
   * Returns the squared root of the sum of squared differences
   */
  def distance(phi1: ArrayFactor, phi2: ArrayFactor) : Double = {
    assert(phi1.scope == phi2.scope)
    (phi1.values zip phi2.values).aggregate[Double](0.0)((v, p) => v + Math.pow(p._1 - p._2, 2), (v1, v2) => v1 + v2)
  }
}