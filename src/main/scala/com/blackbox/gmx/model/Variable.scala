package com.blackbox.gmx.model

/**
 * Variable of a factor. Represents a discrete value with a unique id and a cardinality.
 * its possible values are: {0, ..., cardinality - 1}
 */
trait Variable extends Serializable with Comparable[Variable]{

  /**
   * The unique id of the variable.
   */
  val id: String

  /**
   * The cardinality of the variable.
   */
  val cardinality: Int

  override def toString : String = s"$id (card=$cardinality)"

  /**
   * Equals delegates the equality to the id.
   * 
   * @param any
   * @return True if the given object is a variable and equals their ids.
   */
  override def equals(any: Any) : Boolean = {
    any match {
      case anyVariable: Variable =>
        this.id.equals(anyVariable.id)
      case _ =>
        false
    }
  }

  /**
   * Delegates to id attribute.
   * 
   * @return Hash code of the id.
   */
  override def hashCode : Int = id.hashCode

  /**
   * Delegates to id comparison.
   * @param v
   * @return
   */
  override def compareTo(v : Variable) : Int = id.compareTo(v.id)

}

object Variable {

  /**
   * Creates a new variable with the given id and cardinality.
   * @param id Id of the variable.
   * @param cardinality Cardinality of the variable, greater than zero.
   * @return
   */
  def apply(id: String, cardinality: Int): Variable = {
    assert (cardinality > 0)
    assert (id != null)
    new VariableImpl(id, cardinality)
  }

}
