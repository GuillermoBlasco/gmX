package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
trait Variable[I >: Serializable] extends Serializable {

  def id: I

  def cardinality: Int

  override def toString : String = s"Variable $id with scope $cardinality"

  override def equals(any: Any) : Boolean = {
    any match {
      case anyVariable: Variable =>
        this.id.equals(anyVariable.id)
      case _ =>
        false
    }
  }

  override def hashCode : Int = id.hashCode

}
object Variable {
  def apply[I >: Serializable](_id: I, _size: Int): Variable[I] = {
    new VariableImpl[I](_id, _size)
  }
}