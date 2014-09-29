package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
trait Variable extends Serializable {

  def id: java.io.Serializable

  def cardinality: Int

  override def toString : String = s"$id (card=$cardinality)"

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
  def apply[I <: java.io.Serializable](_id: I, _size: Int): Variable = {
    new VariableImpl(_id, _size)
  }
}