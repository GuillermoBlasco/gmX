package com.blackbox.gmx.model.api

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
trait Factor extends Serializable {

  def id() : Long

  def variables() : Seq[Variable]

  def size(): Int = variables().foldLeft[Int](1)((z: Int, v: Variable) => z * v.size)

  def value(indexs: Seq[Int]): Double

}
