package com.blackbox.gmx.model

import scala.collection.immutable

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
class ArrayLogFactor( override val scope : immutable.Set[Variable],
                      override protected val strides: immutable.Map[Variable, Int],
                      override protected val values: Array[Double]
                      ) extends AbstractArrayFactor(scope, strides, values) with LogFactor {

  override def + (factor: LogFactor): LogFactor = {
    factor match {
      case phi2: ArrayLogFactor =>
        ArrayLogFactor.product(this, phi2)
      case _ =>
        throw new UnsupportedOperationException(
          s"Can not multiply factor of class ${factor.getClass} " +
          s"with this factor of class ${this.getClass}"
        )
    }
  }

  override def - (factor: LogFactor): LogFactor = this + factor.opposite()

  override def + (c: Double): LogFactor =
    new ArrayLogFactor(scope, strides, values.transform((v : Double) => v + c).toArray)

  override def - (c: Double): LogFactor =
    new ArrayLogFactor(scope, strides, values.transform((v : Double) => v - c).toArray)

  override def exp(): Factor = new ArrayFactor(scope, strides, values.transform((v: Double) => Math.exp(v)).toArray)

  override def marginalize(variables: Set[Variable]): LogFactor = {
    assert((variables diff scope).isEmpty)
    val X: Set[Variable] = scope diff variables
    val phi: ArrayLogFactor = ArrayLogFactor(X, 0.0)
    for (i <- 0 until this.size) {
      val assignment = this.assignmentOfIndex(i)
      phi(assignment) = MathUtils.logSum(phi(assignment), this.values(i))
    }
    phi
  }

  override def marginal(variables: Set[Variable]) : LogFactor = {
    assert((variables diff scope).isEmpty)
    marginalize(scope diff variables)
  }

  override def maxMarginal(variables: Set[Variable]) : LogFactor = {
    assert((variables diff scope).isEmpty)
    maxMarginalize(scope diff variables)
  }

  override def maxMarginalize(variables: Set[Variable]) : LogFactor = {
    assert((variables diff scope).isEmpty)
    val X: Set[Variable] = scope diff variables
    val phi: ArrayLogFactor = ArrayLogFactor(X, 0.0)
    for (i <- 0 until this.size) {
      val assignment = this.assignmentOfIndex(i)
      phi(assignment) = Math.max(phi(assignment), this.values(i))
    }
    phi
  }

  override def toString : String = s"ArrayLogFactor [${super.toString}]"

  override def opposite(): LogFactor
  = new ArrayLogFactor(scope, strides, values.transform((v: Double) => - v).toArray)

}

protected object ArrayLogFactor {

  private val op : (Double, Double) => Double = (a, b) => a + b

  def apply(variables: Set[Variable], defaultValue: Double = 0.0) : ArrayLogFactor = {
    val size = variables.foldLeft(1)((z,v) => z * v.cardinality)
    new ArrayLogFactor(
      variables,
      AbstractArrayFactor.computeStrides(variables),
      Array.fill(size)(defaultValue))
  }

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman,
   * Algorithm 10.A.1 (page 359)
   */
  def product(phi1: ArrayLogFactor, phi2: ArrayLogFactor) : ArrayLogFactor = {
    val X1 = phi1.scope
    val X2 = phi2.scope
    val X: Set[Variable] = X1 ++ X2
    val psi: ArrayLogFactor = ArrayLogFactor(X)
    AbstractArrayFactor.product(phi1, phi2, psi, op)
  }

}
