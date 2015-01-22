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

/**
 * Variable of a factor. Represents a discrete value with a unique id and a cardinality.
 * its possible values are: {0, ..., cardinality - 1}
 */
trait Variable extends Serializable with Comparable[Variable]{

  /**
   * The unique id of the variable.
   */
  val id: String

  /**
   * The cardinality of the variable.
   */
  val cardinality: Int

  override def toString : String = s"$id (card=$cardinality)"

  /**
   * Equals delegates the equality to the id.
   * 
   * @param any
   * @return True if the given object is a variable and equals their ids.
   */
  override def equals(any: Any) : Boolean = {
    any match {
      case anyVariable: Variable =>
        this.id.equals(anyVariable.id)
      case _ =>
        false
    }
  }

  /**
   * Delegates to id attribute.
   * 
   * @return Hash code of the id.
   */
  override def hashCode : Int = id.hashCode

  /**
   * Delegates to id comparison.
   * @param v
   * @return
   */
  override def compareTo(v : Variable) : Int = id.compareTo(v.id)

}

object Variable {

  /**
   * Creates a new variable with the given id and cardinality.
   * @param id Id of the variable.
   * @param cardinality Cardinality of the variable, greater than zero.
   * @return
   */
  def apply(id: String, cardinality: Int): Variable = {
    assert (cardinality > 0)
    assert (id != null)
    new VariableImpl(id, cardinality)
  }

}
