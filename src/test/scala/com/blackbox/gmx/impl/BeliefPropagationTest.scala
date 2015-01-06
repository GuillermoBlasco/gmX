package com.blackbox.gmx.impl

import com.blackbox.gmx.ClusterGraph
import com.blackbox.gmx.model.{Factor, Variable}
import org.apache.spark.{SparkContext, SparkConf}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created by guillermoblascojimenez on 17/12/14.
  */
@RunWith(classOf[JUnitRunner])
class BeliefPropagationTest extends FlatSpec {

  val error = 0.000000001
  
  val conf = new SparkConf()
    .setAppName("TestGmx")
    .setMaster("local[1]")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .set("spark.ui.enabled", "false")

  val a = Variable("a", 2)
  val b = Variable("b", 2)
  val c = Variable("c", 2)
  val d = Variable("d", 2)
  val e = Variable("e", 2)

   "Belief Propagation" should "compute marginals properly for chains" in {
     val sc: SparkContext = new SparkContext(conf)

     // FACTORS
     val phi1 = Factor(a)
     phi1(Map(a -> 0)) = 0.2
     phi1(Map(a -> 1)) = 0.8
     val phi2 = Factor(a, b)
     phi2(Map(a -> 0, b -> 0)) = 0.1
     phi2(Map(a -> 1, b -> 0)) = 0.9
     phi2(Map(a -> 0, b -> 1)) = 0.7
     phi2(Map(a -> 1, b -> 1)) = 0.3
     val phi3 = Factor(b, c)
     phi3(Map(c -> 0, b -> 0)) = 0.9
     phi3(Map(c -> 1, b -> 0)) = 0.1
     phi3(Map(c -> 0, b -> 1)) = 0.75
     phi3(Map(c -> 1, b -> 1)) = 0.25

     val clusters = Map[Set[Variable], Set[Factor]](
       Set(a) -> Set(phi1),
       Set(a, b) -> Set(phi2),
       Set(b,c) -> Set(phi3)
     )

     val edges = Set[(Set[Variable], Set[Variable])](
       (Set(a)    , Set(a, b)),
       (Set(a,b), Set(b, c))
     )

     val clusterGraph = ClusterGraph(clusters, edges, sc)
     val calibrated = clusterGraph.calibrated(100, 0.00000001)

     assert (Set(a, b, c) equals calibrated.variables)

     var psi1 = calibrated.factors.find(factor => phi1.scope().equals(factor.scope())).get
     var psi2 = calibrated.factors.find(factor => phi2.scope().equals(factor.scope())).get
     var psi3 = calibrated.factors.find(factor => phi3.scope().equals(factor.scope())).get

     assert (psi1 != null)
     assert (psi2 != null)
     assert (psi3 != null)

     psi1 = psi1.normalized()
     psi2 = psi2.normalized()
     psi3 = psi3.normalized()

     assert(Math.abs(psi1(Map(a -> 0)) - 0.14285714285714285) < error)
     assert(Math.abs(psi1(Map(a -> 1)) - 0.8571428571428572) < error)

     assert(Math.abs(psi2(Map(a -> 0, b -> 0)) - 0.017857142857142856) < error)
     assert(Math.abs(psi2(Map(a -> 1, b -> 0)) - 0.6428571428571429) < error)
     assert(Math.abs(psi2(Map(a -> 0, b -> 1)) - 0.125) < error)
     assert(Math.abs(psi2(Map(a -> 1, b -> 1)) - 0.21428571428571425) < error)

     assert(Math.abs(psi3(Map(b -> 0, c -> 0)) - 0.5946428571428573) < error)
     assert(Math.abs(psi3(Map(b -> 1, c -> 0)) - 0.2544642857142857) < error)
     assert(Math.abs(psi3(Map(b -> 0, c -> 1)) - 0.06607142857142857) < error)
     assert(Math.abs(psi3(Map(b -> 1, c -> 1)) - 0.08482142857142856) < error)

     sc.stop()
   }

  "Belief Propagation" should "compute marginals properly for trees" in {
    val sc: SparkContext = new SparkContext(conf)

    // FACTORS
    val phi1 = Factor(a)
    phi1(Map(a -> 0)) = 0.6
    phi1(Map(a -> 1)) = 0.4
    val phi2 = Factor(b)
    phi2(Map(b -> 0)) = 0.2
    phi2(Map(b -> 1)) = 0.8
    val phi3 = Factor(a, b)
    phi3(Map(a -> 0, b -> 0)) = 0.1
    phi3(Map(a -> 1, b -> 0)) = 0.9
    phi3(Map(a -> 0, b -> 1)) = 0.7
    phi3(Map(a -> 1, b -> 1)) = 0.3
    val phi4 = Factor(b, c)
    phi4(Map(c -> 0, b -> 0)) = 0.9
    phi4(Map(c -> 1, b -> 0)) = 0.1
    phi4(Map(c -> 0, b -> 1)) = 0.75
    phi4(Map(c -> 1, b -> 1)) = 0.25
    val phi5 = Factor(e, d)
    phi5(Map(e -> 0, d -> 0)) = 0.5
    phi5(Map(e -> 1, d -> 0)) = 0.5
    phi5(Map(e -> 0, d -> 1)) = 0.9
    phi5(Map(e -> 1, d -> 1)) = 0.1
    val phi6 = Factor(b, e)
    phi6(Map(e -> 0, b -> 0)) = 0.2
    phi6(Map(e -> 1, b -> 0)) = 0.8
    phi6(Map(e -> 0, b -> 1)) = 0.05
    phi6(Map(e -> 1, b -> 1)) = 0.95

    val clusters = Map[Set[Variable], Set[Factor]](
      Set(a) -> Set(phi1),
      Set(b) -> Set(phi2),
      Set(a, b) -> Set(phi3),
      Set(b, c) -> Set(phi4),
      Set(e, d) -> Set(phi5),
      Set(b, e) -> Set(phi6)
    )

    val edges = Set[(Set[Variable], Set[Variable])](
      (Set(a)    , Set(a, b)), // intersection: a
      (Set(b)    , Set(a, b)), // intersection: b
      (Set(a, b) , Set(b, c)), // intersection: b
      (Set(b, c) , Set(b, e)), // intersection: b
      (Set(b, e) , Set(e, d))  // intersection: e
    )

    val clusterGraph = ClusterGraph(clusters, edges, sc)
    val calibrated = clusterGraph.calibrated(100, 0.0000001)

    assert (Set(a, b, c, d, e) equals calibrated.variables)

    var psi1 = calibrated.factors.find(factor => phi1.scope().equals(factor.scope())).get
    var psi2 = calibrated.factors.find(factor => phi2.scope().equals(factor.scope())).get
    var psi3 = calibrated.factors.find(factor => phi3.scope().equals(factor.scope())).get
    var psi4 = calibrated.factors.find(factor => phi4.scope().equals(factor.scope())).get
    var psi5 = calibrated.factors.find(factor => phi5.scope().equals(factor.scope())).get
    var psi6 = calibrated.factors.find(factor => phi6.scope().equals(factor.scope())).get

    assert (psi1 != null)
    assert (psi2 != null)
    assert (psi3 != null)
    assert (psi4 != null)
    assert (psi5 != null)
    assert (psi6 != null)

    psi1 = psi1.normalized()
    psi2 = psi2.normalized()
    psi3 = psi3.normalized()
    psi4 = psi4.normalized()
    psi5 = psi5.normalized()
    psi6 = psi6.normalized()


    assert(Math.abs(psi1(Map(a -> 0)) - 0.6586741889985895) < error)
    assert(Math.abs(psi1(Map(a -> 1)) - 0.3413258110014105) < error)

    assert(Math.abs(psi2(Map(b -> 0)) - 0.18758815232722154) < error)
    assert(Math.abs(psi2(Map(b -> 1)) - 0.8124118476727785) < error)

    assert(Math.abs(psi3(Map(a -> 0, b -> 0)) - 0.026798307475317355) < error)
    assert(Math.abs(psi3(Map(a -> 1, b -> 0)) - 0.16078984485190417) < error)
    assert(Math.abs(psi3(Map(a -> 0, b -> 1)) - 0.6318758815232721) < error)
    assert(Math.abs(psi3(Map(a -> 1, b -> 1)) - 0.18053596614950637) < error)

    assert(Math.abs(psi4(Map(b -> 0, c -> 0)) - 0.1688293370944994) < error)
    assert(Math.abs(psi4(Map(b -> 1, c -> 0)) - 0.6093088857545839) < error)
    assert(Math.abs(psi4(Map(b -> 0, c -> 1)) - 0.018758815232722156) < error)
    assert(Math.abs(psi4(Map(b -> 1, c -> 1)) - 0.20310296191819463) < error)

    assert(Math.abs(psi5(Map(d -> 0, e -> 0)) - 0.056417489421720736) < error)
    assert(Math.abs(psi5(Map(d -> 1, e -> 0)) - 0.10155148095909734) < error)
    assert(Math.abs(psi5(Map(d -> 0, e -> 1)) - 0.7016925246826515) < error)
    assert(Math.abs(psi5(Map(d -> 1, e -> 1)) - 0.14033850493653033) < error)

    assert(Math.abs(psi6(Map(b -> 0, e -> 0)) - 0.06911142454160793) < error)
    assert(Math.abs(psi6(Map(b -> 1, e -> 0)) - 0.08885754583921013) < error)
    assert(Math.abs(psi6(Map(b -> 0, e -> 1)) - 0.11847672778561362) < error)
    assert(Math.abs(psi6(Map(b -> 1, e -> 1)) - 0.7235543018335683) < error)

    sc.stop()
  }

 }
