package com.blackbox.gmx.model

import com.blackbox.gmx.model.api.{Factor, Variable}

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
class VariableOrFactor(variablex:  Variable, factorx: Factor) extends Serializable {

  val variable: Variable = variablex
  val factor: Factor = factorx

  def this(v: Variable) {
    this(v, null)
  }
  def this(f: Factor) {
    this(null, f)
  }
  def this() {
    this(null, null)
  }
  def isFactor : Boolean = factor != null

  def isVariable : Boolean = variable != null

}
