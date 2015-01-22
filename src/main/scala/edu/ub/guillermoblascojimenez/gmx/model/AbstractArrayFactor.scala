/*
 * Copyright (c) [year], [fullname]
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ub.guillermoblascojimenez.gmx.model

import scala.collection.immutable._
import scala.collection.mutable
import scala.util.control.Breaks._

/**
 * Created by guillermoblascojimenez on 29/09/14.
 */
protected class AbstractArrayFactor(
                           val scope : Set[Variable],
                           protected val strides: Map[Variable, Int],
                           protected val values: Array[Double]
                           ) {

  val size : Int = scope.foldLeft(1)((z,v) => z * v.cardinality)

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
    val assignment = collection.mutable.Map[Variable,Int]() ++= strides
    assignment.transform((v, s) => (index / s) % v.cardinality).toMap
  }

  override def toString: String =
    s"AbstractArrayFactor with scope {${scope.mkString(",")}}" +
    s" and values {${values.mkString(",")}}"

}
protected object AbstractArrayFactor {

  def computeStrides(variables: Set[Variable]) : Map[Variable, Int] = {
    val strides: mutable.HashMap[Variable, Int] = mutable.HashMap[Variable, Int]()
    var stride = 1
    // Variables are arranged to strides with default order since Variables
    // are comparable objects
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
    val sortedX = X.toList.sorted
    assert(X.equals(psi.scope))
    val assignment: mutable.Map[Variable, Int] = mutable.HashMap[Variable, Int]()
    for (v <- X) {
      assignment(v) = 0
    }
    var j = 0 // phi1 index
    var k = 0 // phi2 index
    for (i <- 0 until psi.size) {
      psi.values(i) = op(phi1.values(j), phi2.values(k))
      breakable {
        sortedX foreach { case (v) =>
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
