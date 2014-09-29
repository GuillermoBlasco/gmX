package com.blackbox.gmx.model

import scala.collection.{mutable, immutable}
import util.control.Breaks._


/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 358)
 */
protected class TableFactor(
                             val scope : immutable.Set[Variable],
                             private val strides: immutable.Map[Variable, Int],
                             private val values: Array[Double]
                             ) extends Factor {

  override def update(assignment: Map[Variable, Int], value: Double) = values(indexOfAssignment(assignment)) = value

  override def apply(assignment: Map[Variable, Int]) : Double = values(indexOfAssignment(assignment))

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 358)
   */
  private def indexOfAssignment(assignment: Map[Variable, Int]) : Int = assignment.foldLeft(0)({case (z, (v, i)) => z + strides(v) * i})

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 359)
   */
  private def assignmentOfIndex(index: Int) : Map[Variable, Int] = strides.transform((v, s) => (index / s) % v.cardinality).toMap

  override def *(factor: Factor): Factor = {
    factor match {
      case phi2: TableFactor =>
        TableFactor.product(this, phi2)
      case _ =>
        throw new UnsupportedOperationException
    }
  }

  override def *(c: Double): Factor = new TableFactor(scope, strides, values.transform((v : Double) => v * c).toArray)
  override def /(c: Double): Factor = new TableFactor(scope, strides, values.transform((v : Double) => v / c).toArray)
}
protected object TableFactor {
  def apply(variables: Set[Variable], defaultValue: Double = 0.0) : TableFactor = {
    val size = variables.foldLeft(1)((z,v) => z * v.cardinality)
    val strides: mutable.HashMap[Variable, Int] = mutable.HashMap[Variable, Int]()
    var stride = 1
    // Variables are arranged to strides with no order. If we would like to arrange them to strides in some
    // particular order here we should take the set to an ordered list and iterate over it.
    variables foreach { case (v) =>
      strides(v) = stride
      stride = stride * v.cardinality
    }
    new TableFactor(variables, strides.toMap, Array.fill(size)(defaultValue))
  }

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 10.A.1 (page 359)
   */
  def product(phi1: TableFactor, phi2: TableFactor) : TableFactor = {
    val X1 = phi1.scope
    val X2 = phi2.scope
    val X: Set[Variable] = X1 ++ X2
    val psi: TableFactor = TableFactor(X)
    val assignment: mutable.Map[Variable, Int] = mutable.HashMap[Variable, Int]()
    for (v <- X) {
      assignment(v) = 0
    }
    var j = 0
    var k = 0
    for (i <- 0 until psi.size) {
      psi.values(i) = phi1.values(j) * phi2.values(k)
      X foreach { case (v) =>
        breakable {
          assignment(v) = assignment(v) + 1
          if (assignment(v) equals v.cardinality) {
            assignment(v) = 0
            j = j - (v.cardinality - 1) * phi1.strides.getOrElse(v, 0)
            k = k - (v.cardinality - 1) * phi2.strides.getOrElse(v, 0)
          } else {
            j = j + phi1.strides.getOrElse(v, 0)
            k = k + phi2.strides.getOrElse(v, 0)
            break()
          }
        }
      }
    }
    psi
  }
}