/*
 * BSD License
 *
 * Copyright (c) 2015, University of Barcelona
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ub.guillermoblascojimenez.gmx.model

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

/**
 * Created by guillermoblascojimenez on 17/12/14.
 */
@RunWith(classOf[JUnitRunner])
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
