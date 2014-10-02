package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 16/09/14.
 */
trait Variable extends Serializable with Comparable[Variable]{

  def id: String

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

  override def compareTo(v : Variable) : Int = id.compareTo(v.id)

}
object Variable {
  def apply(id: String, size: Int): Variable = new VariableImpl(id, size)
}