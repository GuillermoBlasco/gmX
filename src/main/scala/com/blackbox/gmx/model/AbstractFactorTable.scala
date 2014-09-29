package com.blackbox.gmx.model

import scala.collection.{mutable, immutable}
import scala.util.control.Breaks._

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
class AbstractFactorTable(
                           val scope : immutable.Set[Variable],
                           protected val strides: immutable.Map[Variable, Int],
                           protected val values: Array[Double]
                           ) {

  def size() : Int = scope.foldLeft(1)((z,v) => z * v.cardinality)

  def update(assignment: Map[Variable, Int], value: Double) = values(indexOfAssignment(assignment)) = value

  def apply(assignment: Map[Variable, Int]) : Double = values(indexOfAssignment(assignment))


  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 358)
   */
  protected def indexOfAssignment(assignment: Map[Variable, Int]) : Int = assignment.foldLeft(0)({case (z, (v, i)) => z + strides(v) * i})

  /*
   * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 359)
   */
  protected def assignmentOfIndex(index: Int) : Map[Variable, Int] = strides.transform((v, s) => (index / s) % v.cardinality).toMap


}
object AbstractFactorTable {
  /*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 10.A.1 (page 359)
 */
  def product[I <: AbstractFactorTable](phi1: I, phi2: I, psi: I, op: (Double, Double) => Double) : I = {
    val X1 = phi1.scope
    val X2 = phi2.scope
    val X: Set[Variable] = X1 ++ X2
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
