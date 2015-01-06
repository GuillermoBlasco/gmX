package com.blackbox.gmx.test

import com.blackbox.gmx.model.{Factor, Variable}
import org.scalatest.FlatSpec

/**
 * Created by guillermoblascojimenez on 17/12/14.
 */
class ArrayFactorTest extends FlatSpec {

  "An ArrayFactor" should "create uniform factors" in {
    val a = Variable("a", 2)
    val phi = Factor.uniform(Set(a))
    assert(phi(Map(a -> 0)) == phi(Map(a -> 1)))
  }

  it should "be invariant when multiplied by an uniform factor and normalized" in {
    val e = 0.000000001
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    var phi = Factor(Set(a, b))
    phi(Map(a ->0, b->1)) = 0.1
    phi(Map(a ->0, b->0)) = 0.2
    phi(Map(a ->1, b->1)) = 0.3
    phi(Map(a ->1, b->0)) = 0.9
    phi = phi.normalized()
    val u = Factor.uniform(Set(a, b))
    val psi = (phi * u).normalized()
    assert(Math.abs(phi(Map(a -> 0, b -> 0)) - psi(Map(a -> 0, b->0))) < e)
    assert(Math.abs(phi(Map(a -> 0, b -> 1)) - psi(Map(a -> 0, b->1))) < e)
    assert(Math.abs(phi(Map(a -> 1, b -> 0)) - psi(Map(a -> 1, b->0))) < e)
    assert(Math.abs(phi(Map(a -> 1, b -> 1)) - psi(Map(a -> 1, b->1))) < e)
  }

  it should "be multiplied properly against a constant" in {
    val e = 0.000000001
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    var phi = Factor(Set(a, b))
    phi(Map(a ->0, b->0)) = 0.2
    phi(Map(a ->0, b->1)) = 0.1
    phi(Map(a ->1, b->0)) = 0.9
    phi(Map(a ->1, b->1)) = 0.3


    val r = phi * 5
    assert(Math.abs(r(Map(a->0, b->0)) - 1.0) < e)
    assert(Math.abs(r(Map(a->0, b->1)) - 0.5) < e)
    assert(Math.abs(r(Map(a->1, b->0)) - 4.5) < e)
    assert(Math.abs(r(Map(a->1, b->1)) - 1.5) < e)

  }

  it should "be multiplied properly against other factor with same scope" in {
    val e = 0.000000001
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    var phi = Factor(Set(a, b))
    phi(Map(a ->0, b->0)) = 0.2
    phi(Map(a ->0, b->1)) = 0.1
    phi(Map(a ->1, b->0)) = 0.9
    phi(Map(a ->1, b->1)) = 0.3

    var psi = Factor(Set(a, b))
    psi(Map(a ->0, b->0)) = 5
    psi(Map(a ->0, b->1)) = 2
    psi(Map(a ->1, b->0)) = 7
    psi(Map(a ->1, b->1)) = 4

    val r = phi * psi
    assert(Math.abs(r(Map(a->0, b->0)) - 1.0) < e)
    assert(Math.abs(r(Map(a->0, b->1)) - 0.2) < e)
    assert(Math.abs(r(Map(a->1, b->0)) - 6.3) < e)
    assert(Math.abs(r(Map(a->1, b->1)) - 1.2) < e)

  }

  it should "be multiplied properly against other factor with different scope" in {
    val e = 0.000000001
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    var phi = Factor(Set(a, b))
    phi(Map(a ->0, b->0)) = 0.2
    phi(Map(a ->0, b->1)) = 0.1
    phi(Map(a ->1, b->0)) = 0.9
    phi(Map(a ->1, b->1)) = 0.3

    var psi = Factor(Set(a))
    psi(Map(a ->0)) = 5
    psi(Map(a ->1)) = 2

    val r = phi * psi
    assert(Math.abs(r(Map(a->0, b->0)) - 1.0) < e)
    assert(Math.abs(r(Map(a->0, b->1)) - 0.5) < e)
    assert(Math.abs(r(Map(a->1, b->0)) - 1.8) < e)
    assert(Math.abs(r(Map(a->1, b->1)) - 0.6) < e)

  }
  it should "be multiplied properly against other factor with different scope 2" in {
    val e = 0.000000001
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    var phi = Factor(Set(b))
    phi(Map(b->0)) = 0.1
    phi(Map(b->1)) = 0.4

    var psi = Factor(Set(a))
    psi(Map(a ->0)) = 0.6
    psi(Map(a ->1)) = 0.4

    val r = phi * psi
    assert(Math.abs(r(Map(a->0, b->0)) - 0.6*0.1) < e)
    assert(Math.abs(r(Map(a->0, b->1)) - 0.6*0.4) < e)
    assert(Math.abs(r(Map(a->1, b->0)) - 0.4*0.1) < e)
    assert(Math.abs(r(Map(a->1, b->1)) - 0.4*0.4) < e)

  }


  it should "be properly marginalized" in {
    val e = 0.000000001
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    var phi = Factor(Set(a, b))
    phi(Map(a ->0, b->0)) = 0.2
    phi(Map(a ->0, b->1)) = 0.1
    phi(Map(a ->1, b->0)) = 0.9
    phi(Map(a ->1, b->1)) = 0.3

    val r = phi.marginalize(Set(a))
    assert(Math.abs(r(Map(b->0)) - 1.1) < e)
    assert(Math.abs(r(Map(b->1)) - 0.4) < e)

  }

  it should "have a consistent z value" in {
    val e = 0.000000001
    val a = Variable("a", 2)
    val b = Variable("b", 2)
    var phi = Factor(Set(a, b))
    phi(Map(a ->0, b->0)) = 0.2
    phi(Map(a ->0, b->1)) = 0.1
    phi(Map(a ->1, b->0)) = 0.9
    phi(Map(a ->1, b->1)) = 0.3

    val r = phi.z()
    assert(Math.abs(r - 1.5) < e)

  }

}
