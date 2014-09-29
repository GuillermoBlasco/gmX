package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 28/09/14.
 */
class VariableImpl[I >: Serializable](
                                       val id: I,
                                       val cardinality: Int
                                       ) extends Variable[I] {

}
