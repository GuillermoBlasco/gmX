package com.blackbox.gmx.model

import scala.collection.mutable
import scala.collection.immutable._
import scala.util.control.Breaks._

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
protected class AbstractArrayFactor(
                           val scope : Set[Variable],
                           protected val strides: Map[Variable, Int],
                           protected val values: Array[Double]
                           ) {

  def size() : Int = scope.foldLeft(1)((z,v) => z * v.cardinality)

  def update(assignment: Map[Variable, Int], value: Double) = values(indexOfAssignment(assignment)) = value

  def apply(assignment: Map[Variable, Int]) : Double = values(indexOfAssignment(assignment))


  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 358)
   */
  protected def indexOfAssignment(assignment: Map[Variable, Int]) : Int = {
    assert(scope.diff(assignment.keySet).isEmpty)
    assignment.foldLeft(0)({case (z, (v, i)) => z + strides.getOrElse(v, 0) * i})
  }

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 359)
   */
  protected def assignmentOfIndex(index: Int) : Map[Variable, Int] = {
    assert(index >= 0 && index < size)
    strides.transform((v, s) => (index / s) % v.cardinality).toMap
  }

  override def toString: String = s"AbstractArrayFactor with scope {${scope.mkString(",")}} and values {${values.mkString(",")}}"
}
protected object AbstractArrayFactor {

  def computeStrides(variables: Set[Variable]) : Map[Variable, Int] = {
    val strides: mutable.HashMap[Variable, Int] = mutable.HashMap[Variable, Int]()
    var stride = 1
    // Variables are arranged to strides with no order. If we would like to arrange them to strides in some
    // particular order here we should take the set to an ordered list and iterate over it.
    val sortedVariables: List[Variable] = variables.toList.sorted
    sortedVariables foreach { case (v) =>
      strides(v) = stride
      stride = stride * v.cardinality
    }
    strides.toMap
  }
  /*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 10.A.1 (page 359)
 */
  def product[I <: AbstractArrayFactor](phi1: I, phi2: I, psi: I, op: (Double, Double) => Double) : I = {
    assert(phi1 != null)
    assert(phi2 != null)
    assert(psi != null)
    assert(op != null)
    val X1 = phi1.scope
    val X2 = phi2.scope
    val X: Set[Variable] = X1 ++ X2
    assert(X.equals(psi.scope))
    val assignment: mutable.Map[Variable, Int] = mutable.HashMap[Variable, Int]()
    for (v <- X) {
      assignment(v) = 0
    }
    var j = 0
    var k = 0
    for (i <- 0 until psi.size) {
      psi.values(i) = op(phi1.values(j), phi2.values(k))
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
