package com.blackbox.gmx.model

import scala.collection.{mutable, immutable}
import scala.util.control.Breaks._

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
class LogFactorTable( override val scope : immutable.Set[Variable],
                      override protected val strides: immutable.Map[Variable, Int],
                      override protected val values: Array[Double]
                      ) extends AbstractFactorTable(scope, strides, values) with LogFactor {

  override def +(factor: LogFactor): LogFactor = {
    factor match {
      case phi2: LogFactorTable =>
        LogFactorTable.product(this, phi2)
      case _ =>
        throw new UnsupportedOperationException(s"Can not multiply factor of class ${factor.getClass} with this factor of class ${this.getClass}")
    }
  }

  override def +(c: Double): LogFactor = new LogFactorTable(scope, strides, values.transform((v : Double) => v + c).toArray)
  override def -(c: Double): LogFactor = new LogFactorTable(scope, strides, values.transform((v : Double) => v - c).toArray)
  override def exp(): Factor = new FactorTable(scope, strides, values.transform((v: Double) => Math.exp(v)).toArray)
}
protected object LogFactorTable {
  private val op : (Double, Double) => Double = (a, b) => a+b

  def apply(variables: Set[Variable], defaultValue: Double = 0.0) : LogFactorTable = {
    val size = variables.foldLeft(1)((z,v) => z * v.cardinality)
    val strides: mutable.HashMap[Variable, Int] = mutable.HashMap[Variable, Int]()
    var stride = 1
    // Variables are arranged to strides with no order. If we would like to arrange them to strides in some
    // particular order here we should take the set to an ordered list and iterate over it.
    variables foreach { case (v) =>
      strides(v) = stride
      stride = stride * v.cardinality
    }
    new LogFactorTable(variables, strides.toMap, Array.fill(size)(defaultValue))
  }

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 10.A.1 (page 359)
   */
  def product(phi1: LogFactorTable, phi2: LogFactorTable) : LogFactorTable = {
    val X1 = phi1.scope
    val X2 = phi2.scope
    val X: Set[Variable] = X1 ++ X2
    val psi: LogFactorTable = LogFactorTable(X)
    AbstractFactorTable.product(phi1, phi2, psi, op)
  }
}