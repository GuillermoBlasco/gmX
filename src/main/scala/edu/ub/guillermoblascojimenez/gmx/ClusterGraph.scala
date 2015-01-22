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
package edu.ub.guillermoblascojimenez.gmx

import edu.ub.guillermoblascojimenez.gmx.impl.ClusterGraphImpl
import edu.ub.guillermoblascojimenez.gmx.model._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph

/**
 * Represents a cluster graph structure where vertex are set of variables and a factor with this scope
 * and edges the intersection of connected vertex.
 *
 * Ref: Probabilistic Graphical Models, Daphne Koller and Nir Friedman, Definition 10.1 (page 346)
 */
abstract class ClusterGraph protected () extends Serializable {

  /** Underlying raw graph */
  val graph: Graph[Factor, Set[Variable]]

  /** Factors contained in the graph */
  def factors() : Set[Factor]

  /** Complete set of variables of the graph */
  def variables() : Set[Variable]

  /** Calibrates the marginals of the graph */
  def calibrated(maxIters:Int = 10, epsilon:Double=0.1) : ClusterGraph

  /** Calibrates MAPs of the graph */
  def map(maxIterations :Int = 10, epsilon:Double=0.1): ClusterGraph

  /** Normalizes the cluster grpah factors */
  def normalized() : ClusterGraph

  /** Counts the number of clusters in the graph */
  def countClusters() : Long

}

object ClusterGraph {

  /**
   * Builds a Bethe Cluster Graph with given factors.
   * @param factors Factor set
   * @param sc Spark Context
   * @return Bethe Cluster Graph with given factors
   */
  def apply(factors: Set[Factor], sc : SparkContext) : ClusterGraph = {
    ClusterGraphImpl.bethe(factors, sc)
  }

  /**
   * Builds a Cluster graph with the given properties.
   * @param clusters A map where the keys are the clusters and the values are the set of factors attached to the cluster
   * @param edges Edges as a set of pairs of keys of the cluster map
   * @param sc Spark context
   * @return
   */
  def apply
    (clusters: Map[Set[Variable], Set[Factor]],
     edges: Set[(Set[Variable], Set[Variable])],
     sc: SparkContext) : ClusterGraph = {
    ClusterGraphImpl(clusters, edges, sc)
  }

}
