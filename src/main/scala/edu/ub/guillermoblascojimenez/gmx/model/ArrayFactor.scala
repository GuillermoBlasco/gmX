/*
 * BSD License
 *
 * Copyright (c) 2015, University of Barcelona
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ub.guillermoblascojimenez.gmx.model


import scala.collection.immutable

/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Box 10.A (page 358)
 */
class ArrayFactor
  (override val scope : immutable.Set[Variable],
   override val strides: immutable.Map[Variable, Int],
   override val values: Array[Double])
  extends AbstractArrayFactor(scope, strides, values) with Factor {


  override def *(factor: Factor): Factor = {
    assert(factor != null)
    factor match {
      case phi2: ArrayFactor =>
        ArrayFactor.product(this, phi2)
      case _ =>
        throw new UnsupportedOperationException(
          s"Can not multiply factor of class ${factor.getClass} " +
          s"with this factor of class ${this.getClass}"
        )
    }
  }

  override def /(factor : Factor) : Factor = this * factor.inverse()

  override def *(c: Double): Factor = {
    assert(c > 0)
    val newArray = new Array[Double](values.size)
    Array.copy(values, 0, newArray, 0, values.size)
    new ArrayFactor(scope, strides, newArray.transform((v : Double) => v * c).toArray)
  }

  override def copy() : Factor = this * Factor.constant(this.scope, 1.0)

  override def /(c: Double): Factor = {
    assert(c >= 0)
    if (c > 0) {
      val newArray = new Array[Double](values.size)
      Array.copy(values, 0, newArray, 0, values.size)
      new ArrayFactor(scope, strides, newArray.transform((v: Double) => v / c).toArray)
    } else {
      new ArrayFactor(scope, strides, Array.fill[Double](size)(0.0))
    }
  }

  override def z() : Double = values.aggregate(0.0)(_ + _, _ + _)

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
  override def maxMarginal(variables: Set[Variable]) : Factor = {
    assert((variables diff scope).isEmpty)
    maxMarginalize(scope diff variables)
  }
  override def maxMarginalize(variables: Set[Variable]) : Factor = {
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

  override def inverse(): Factor = {
    val newArray = new Array[Double](values.size)
    Array.copy(values, 0, newArray, 0, values.size)
    new ArrayFactor(scope, strides, newArray.transform((v: Double) => {
      if (v != 0) {
        1.0 / v
      } else {
        0.0
      }
    }
    ).toArray)
  }

}
protected object ArrayFactor {
  private val op : (Double, Double) => Double = (a, b) => a * b

  def apply(variables: Set[Variable]) : ArrayFactor = {
    apply(variables, {0.0})
  }
  def apply(variables: Set[Variable], valueFactory: => Double) : ArrayFactor = {
    val size = variables.foldLeft(1)((z,v) => z * v.cardinality)
    new ArrayFactor(
      variables,
      AbstractArrayFactor.computeStrides(variables),
      Array.fill[Double](size)(valueFactory))
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
