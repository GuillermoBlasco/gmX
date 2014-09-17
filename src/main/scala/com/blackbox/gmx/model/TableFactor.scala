package com.blackbox.gmx.model

import com.blackbox.gmx.model.api._

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
class TableFactor(idx: Long, variablesx: Seq[Variable], valuesx: Seq[Double]) extends MultiVariableFactor(idx, variablesx) {

  val values: Seq[Double] = valuesx

  override def value(indexs: Seq[Int]): Double = {
    this.valuesx.apply(this.indexOf(indexs))
  }

  protected def indexOf(indexs: Seq[Int]) : Int = {
    val scopes: Seq[Int] = variables.map(v => v.size)
    var stride = 1
    var index = indexs.apply(0)

    for (i <- 1 until scopes.size) {
      stride *= scopes.apply(i)
      index += indexs.apply(i) * stride
    }
    index
  }

}