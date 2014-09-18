package com.blackbox.gmx.model

import com.blackbox.gmx.model.api._

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
class SingleVariableFactor(variablex: Variable, valuesx: Seq[Double]) extends MultiVariableFactor(Array[Variable](variablex)) {

  val values: Seq[Double] = valuesx

  override def value(indexs: Seq[Int]): Double = values.apply(indexs.apply(0))

}
