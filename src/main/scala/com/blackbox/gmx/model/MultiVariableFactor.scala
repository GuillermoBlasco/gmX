package com.blackbox.gmx.model

import com.blackbox.gmx.model.api.{Variable, Factor}

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
abstract class MultiVariableFactor(variablesx: Seq[Variable]) extends Factor {

  override val variables: Seq[Variable] = variablesx

}
