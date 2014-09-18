package com.blackbox.gmx

import com.blackbox.gmx.model.VariableOrFactor
import org.apache.spark.graphx._

/**
 * Created by guillermoblascojimenez on 17/09/14.
 */
class GMX {

  def countVariables(graph:Graph[VariableOrFactor, Nothing]) : Long = {
    graph.vertices.filter({case (id: Long, vof:VariableOrFactor) => vof.isVariable}).count()
  }

  def countFactors(graph: Graph[VariableOrFactor, Nothing]) : Long = {
    graph.vertices.filter({case (id: Long, vof:VariableOrFactor) => vof.isFactor}).count()
  }

  def setEvidence(graph: Graph[VariableOrFactor, Nothing], variableNodeId: Long, evidence: Int) : Graph[VariableOrFactor, Nothing] = {
    throw new UnsupportedOperationException();
  }

}
