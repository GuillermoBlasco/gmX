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
package edu.ub.guillermoblascojimenez.gmx.impl

import edu.ub.guillermoblascojimenez.gmx.model.{Variable, Factor}
import org.apache.spark.Logging
import org.apache.spark.graphx.Graph


/*
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Algorithm 11.1 (page 397)
 */
object BeliefPropagation extends Logging {

  /*
   * Additional structure for BP
   */

  private class BPVertex (val factor: Factor, val incomingMessages: Factor) extends java.io.Serializable {

    def potential() : Factor = factor * incomingMessages

  }
  private object BPVertex {
    def apply(factor:Factor) : BPVertex = apply(factor.copy(), Factor.uniform(factor.scope))
    def apply(factor:Factor, potential: Factor) : BPVertex = new BPVertex(factor.copy(), potential.copy())
  }

  private def reduceDeltas(d1: Factor, d2: Factor) : Factor = d1 * d2

  private def sumProjection(f: Factor, s: Set[Variable]) : Factor = f.marginal(s)
  private def maxProjection(f: Factor, s: Set[Variable]) : Factor = f.maxMarginal(s)

  /*
   * Core BP algorithm
   */
  def apply
    (projection: (Factor, Set[Variable]) => Factor,
     maxIterations : Int,
     epsilon : Double)
    (graph : Graph[Factor, Set[Variable]])
    : Graph[Factor, Set[Variable]] = {

    assert(maxIterations > 0)
    assert(epsilon > 0.0)

    // deltas are set
    var g: Graph[BPVertex, Factor] = graph
      .mapVertices((id, f) => BPVertex(f))
      .mapEdges((edge) => Factor.uniform(edge.attr))
      .cache()

    var iteration: Int = 0
    var iterationError: Double = Double.PositiveInfinity

    do {

      val newG = iterate(projection)(g).cache()
      iterationError = calibrationError(g, newG) // this instruction materializes RDDs

      // unpersist things
      g.unpersistVertices(blocking = false)
      g.edges.unpersist(blocking = false)

      // update cached things
      g = newG
      iteration += 1

    } while (iteration < maxIterations && iterationError > epsilon)

    g.unpersistVertices(blocking = false)
    g.edges.unpersist(blocking = false)

    val outputGraph = g
      .mapVertices((id, v) => v.potential().normalized())
      .mapEdges((edge) => edge.attr.scope)

    outputGraph
  }

  private def calibrationError(g1 : Graph[BPVertex, Factor], g2 : Graph[BPVertex, Factor]) : Double = {
    g1.edges
      .innerJoin(g2.edges)((srcId, dstId, ij, ji) => Factor.distance(ij.normalized(), ji.normalized()))
      .aggregate(0.0)((e, errorEdge) => e + errorEdge.attr, _ + _)
  }

  private def iterate
    (projection: (Factor, Set[Variable]) => Factor)
    (g : Graph[BPVertex, Factor]) : Graph[BPVertex, Factor] = {
    // compute new deltas
    // for each edge i->j generate delta as
    //    i->j_potential := i_factor * i_potential / j->i_potential
    // Trick: for each edge i->j set as potential j_factor * j_potential / i->j_potential and then
    // reverse all edges
    val newDeltas = g
      .mapTriplets((triplet) => projection(triplet.dstAttr.potential() / triplet.attr, triplet.attr.scope))
      .reverse

    // Compute new potentials and put them into a new graph
    // for each node i collect incoming edges and update as:
    //    i_potential := PRODUCT [j->i_potential, for j in N(i)]
    val messages = newDeltas
      .mapReduceTriplets((triplet) => Iterator((triplet.dstId, triplet.attr)), reduceDeltas)

    // keep the factor and update messages
    val newG = newDeltas
      .outerJoinVertices(g.vertices)((id, v, bpVertex) => bpVertex.get.factor)
      .outerJoinVertices(messages)((id, factor, message) => BPVertex(factor, message.get))

     newG
  }

  def sum
    (maxIterations: Int,
     epsilon: Double)
    (graph : Graph[Factor, Set[Variable]]) : Graph[Factor, Set[Variable]] =
    apply(sumProjection, maxIterations, epsilon)(graph)

  def max
    (maxIterations: Int,
     epsilon: Double)
    (graph : Graph[Factor, Set[Variable]]) : Graph[Factor, Set[Variable]] =
    apply(maxProjection, maxIterations, epsilon)(graph)

}
