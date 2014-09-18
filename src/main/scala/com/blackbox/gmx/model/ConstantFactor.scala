package com.blackbox.gmx.model
import com.blackbox.gmx.model.api._

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
class ConstantFactor(variablesx: Seq[Variable], val valuex: Double) extends MultiVariableFactor(variablesx) {

  override def value(indexs: Seq[Int]): Double = valuex
}
