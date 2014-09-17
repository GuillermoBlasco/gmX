package com.blackbox.gmx.model.api

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
class NamedVariable(idx: Long, sizex: Int, namex: String) extends Variable(idx, sizex) {

  val name: String = namex

}
