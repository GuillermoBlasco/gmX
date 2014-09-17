package com.blackbox.gmx.model.api

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
class LabelledVariable[L](idx:Long, labelsx:Array[L]) extends Variable(idx, labelsx.length) {

  val labels: Array[L] = labelsx

}
