package com.blackbox.gmx.model.api


/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
class Variable(idx: Long, sizex: Int) extends Serializable {

  val id: Long = idx
  val size: Int = sizex

}
