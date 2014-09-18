package com.blackbox.gmx.model.api

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
class LabelledVariable[L](labelsx:Array[L]) extends Variable(labelsx.length) {

  val labels: Array[L] = labelsx

}
