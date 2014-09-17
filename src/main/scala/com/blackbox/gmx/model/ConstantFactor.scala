package com.blackbox.gmx.model
import com.blackbox.gmx.model.api._

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
class ConstantFactor(idx: Long, variablesx: Seq[Variable], val valuex: Double) extends MultiVariableFactor(idx, variablesx) {

  override def value(indexs: Seq[Int]): Double = valuex
}
