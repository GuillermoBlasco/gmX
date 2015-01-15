package edu.ub.guillermoblascojimenez.gmx.model

/**
 * Factor trait that defines the operations of a function over a finite set of variables. The factors are mutable
 * via direct assignment of its values, but any other operation that is not update a value of the factor creates
 * a new factor with no modification of this or other factors implied in the operation.
 * 
 * Let name this factor as phi.
 */
trait Factor extends Serializable {

  /**
   * The scope of the factor.
   * 
   * @return The scope of the factor.
   */
  val scope : Set[Variable]

  /**
   * The amount of entries that the factor has.
   * 
   * @return Amount of entries of the factor
   */
  val size : Int

  /**
   * Creates a new factor containing the values of the product between this factor and the given factor. That is: 
   * phi * psi
   * The scope of the returned factor is the union of scopes.
   * 
   * @param psi The factor to multiply with.
   * @return The product of this factor and the given.
   */
  def * (psi: Factor) : Factor

  /**
   * Creates a new factor containing the values of the product between this factor and a constant value. This is:
   * phi * c
   * 
   * @param c The constant value to multiply, greater than zero.
   * @return The product of this factor and the constant.
   */
  def * (c: Double) : Factor

  /**
   * Creates a new factor containing the values of the division between this factor and the given factor. That is:
   * phi / psi That is computed as phi * ( 1 / psi)
   * The scope of the returned factor is the union of scopes.
   * 
   * @param psi The divisor factor
   * @return The division of this factor and the given.
   */
  def / (psi: Factor) : Factor

  /**
   * Creates a new factor containing the values of the division between this factor and a constant value. This is:
   * phi / c that is computed as phi * (1 / c)
   * 
   * @param c The constant value to multiply, greater than zero.
   * @return The division of this factor and the given constant.
   */
  def / (c: Double) : Factor

  /**
   * Creates a new factor that has the values inverted. The rule 1/0 := 0 applies. This is: 1 / phi
   * 
   * @return The inverse of the factor
   */
  def inverse() : Factor

  /**
   * Creates a new factor with as scope the given variables. The values of the new factor is the marginalization
   * via summation, also called summed-out. This is sum_{variables}(phi)
   *  
   * @param variables Variables to get as marginal.
   * @return A factor marginalized over the given variables.
   */
  def marginal(variables: Set[Variable]) : Factor

  /**
   * Creates a new factor with as scope the scope of the factor minus the given variables. The values of the new factor
   * is the marginalization via summation. This is sum_{scope - variables}(phi)
   * 
   * @param variables Variables to be marginalized.
   * @return A factor with the given variables marginalized.
   */
  def marginalize(variables: Set[Variable]) : Factor

  /**
   * Creates a new factor with as scope the given variables. The values of the new factor is the marginalization
   * via maximization. This is max_{variables}(phi)
   *
   * @param variables Variables to get as max marginal.
   * @return A factor max marginalized over the given variables.
   */
  def maxMarginal(variables: Set[Variable]) : Factor

  /**
   * Creates a new factor with as scope the scope of the factor minus the given variables. The values of the new factor
   * is the marginalization via maximization. This is max{scope - variables}(phi)
   *
   * @param variables Variables to be max marginalized.
   * @return A factor with the given variables max marginalized.
   */
  def maxMarginalize(variables: Set[Variable]) : Factor

  /**
   * Creates a new factor that is contains the same values and scope that this.
   * 
   * @return A copy of this factor.
   */
  def copy() : Factor

  /**
   * Updates the value of this factor for an assignment. This is phi(assignment) := value. Usage:
   * phi(Map(/* assignments */)) = value
   * 
   * @param assignment Assignment to be updated.
   * @param value Value of the assignment.
   */
  def update(assignment : Map[Variable, Int], value: Double)

  /**
   * Gets the value of this factor for an assignment. This is phi(assignment). Usage:
   * phi(Map(/* assignment */))
   * 
   * @param assignment Assignment to be read.
   * @return The value attached to the given assignment for this factor.
   */
  def apply(assignment : Map[Variable, Int]) : Double

  /**
   * Returns the summation of all values. Often named as z value or partition function. This is sum_{scope}(phi)
   * 
   * @return The summation of all values.
   */
  def z(): Double

  /**
   * Creates a new factor with the values divided by z value. This is: phi / z. The z value of the returned factor is 1.
   * 
   * @return The factor normalized.
   */
  def normalized() : Factor

}

object Factor {

  /**
   * Creates a constant factor for the given variable. The constant value is 0.
   * 
   * @param variable The scope of the factor.
   * @return A constant factor for the given scope.
   */
  def apply(variable: Variable) : Factor = apply(Set[Variable](variable))

  /**
   * Creates a constant factor for the given list of variables. The constant value is 0.
   *
   * @param variables The scope of the factor.
   * @return A constant factor for the given scope.
   */
  def apply(variables: Variable*) : Factor = apply(variables.toSet)

  /**
   * Creates a constant factor for the given set of variables. The constant value is 0.
   *
   * @param variables The scope of the factor.
   * @return A constant factor for the given scope.
   */
  def apply(variables: Set[Variable]) : Factor = ArrayFactor(variables)

  /**
   * Creates a constant factor for the given set variables and with the given constant value.
   *
   * @param variables The scope of the factor.
   * @param constant The value of each entry of the factor.
   * @return A constant factor for the given scope.
   */
  def constant(variables: Set[Variable], constant: Double) : Factor =
    ArrayFactor(variables, constant)

  /**
   * Creates a uniform factor for the given set of variables. The factor is normalized.
   * 
   * @param variables Ths scope of the factor.
   * @return A uniform factor for the given scope.
   */
  def uniform(variables: Set[Variable]) : Factor = ArrayFactor(variables, 1.0).normalized()

  /**
   * Creates a factor for the given set of variables with random values in range (0,1).
   * 
   * @param variables The scope of the factor.
   * @return A factor with random values.
   */
  def randomized(variables: Set[Variable]): ArrayFactor = ArrayFactor(variables, math.random)

  /**
   * Computes the distance between two factors. This is d(phi, psi). The distance is defined as the squared
   * euclidean metric of the values of phi - psi. Both factors have to share scope.
   *
   * @param phi One factor.
   * @param psi Another factor.
   * @return The distance of the two given factors.
   */
  def distance(phi: Factor, psi: Factor) : Double = {
    if (phi.scope != psi.scope) {
      throw new IllegalArgumentException(
        s"Distance for factors with scopes ${phi.scope} and ${psi.scope} is not legal."
      )
    }
    if (phi.getClass.equals(classOf[ArrayFactor]) && psi.getClass.equals(classOf[ArrayFactor])) {
      return ArrayFactor.distance(phi.asInstanceOf[ArrayFactor], psi.asInstanceOf[ArrayFactor])
    }
    throw new UnsupportedOperationException(
      s"Distance for factors of classes ${phi.getClass} and ${psi.getClass} is not supported."
    )
  }
}
