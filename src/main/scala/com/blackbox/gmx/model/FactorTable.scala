package com.blackbox.gmx.model

import scala.collection.{mutable, immutable}

/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 358)
 */
protected class FactorTable(
                             override val scope : immutable.Set[Variable],
                             override protected val strides: immutable.Map[Variable, Int],
                             override protected val values: Array[Double]
                             ) extends AbstractFactorTable(scope, strides, values) with Factor {


  override def *(factor: Factor): Factor = {
    factor match {
      case phi2: FactorTable =>
        FactorTable.product(this, phi2)
      case _ =>
        throw new UnsupportedOperationException
    }
  }

  override def *(c: Double): Factor = new FactorTable(scope, strides, values.transform((v : Double) => v * c).toArray)
  override def /(c: Double): Factor = new FactorTable(scope, strides, values.transform((v : Double) => v / c).toArray)

  override def log(): LogFactor = new LogFactorTable(scope, strides, values.transform((v: Double) => Math.log(v)).toArray)
}
protected object FactorTable {
  private val op : (Double, Double) => Double = (a, b) => a * b

  def apply(variables: Set[Variable], defaultValue: Double = 0.0) : FactorTable = {
    val size = variables.foldLeft(1)((z,v) => z * v.cardinality)
    val strides: mutable.HashMap[Variable, Int] = mutable.HashMap[Variable, Int]()
    var stride = 1
    // Variables are arranged to strides with no order. If we would like to arrange them to strides in some
    // particular order here we should take the set to an ordered list and iterate over it.
    variables foreach { case (v) =>
      strides(v) = stride
      stride = stride * v.cardinality
    }
    new FactorTable(variables, strides.toMap, Array.fill(size)(defaultValue))
  }

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 10.A.1 (page 359)
   */
  def product(phi1: FactorTable, phi2: FactorTable) : FactorTable = {
    val X1 = phi1.scope
    val X2 = phi2.scope
    val X: Set[Variable] = X1 ++ X2
    val psi: FactorTable = FactorTable(X)
    AbstractFactorTable.product(phi1, phi2, psi, op)
  }
}