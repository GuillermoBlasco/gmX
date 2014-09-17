package com.blackbox.gmx.model

import com.blackbox.gmx.model.api._

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
class SparseFactor(idx: Long, variablesx: Seq[Variable], valuesx: Map[Seq[Int], Double]) extends MultiVariableFactor(idx, variablesx) {

  val values: Map[Seq[Int], Double] = valuesx

  override def value(indexs: Seq[Int]): Double = values.getOrElse(indexs, 0.0)

}
