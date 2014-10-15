package com.blackbox.gmx.model

/**
 * Created by guillermoblascojimenez on 15/10/14.
 */
class MathUtils {

}
object MathUtils {

  /**
   * log(a + b) = log(a * (1 + b/a)) = log(a) + log(1 + b/a) = log (a) + log(1 + exp(log(b) - log (a))
   * @param logA Logarithm of value A.
   * @param logB Logarithm of value B.
   * @return Logarithm of value A plus value B.
   */
  def logSum(logA: Double, logB: Double) : Double = {
    if (logA > logB) {
      logA + Math.log(1 + Math.exp(logB - logA))
    } else {
      logB + Math.log(1 + Math.exp(logA - logB))
    }
  }

}
