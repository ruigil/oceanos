package ws.oceanos.core.event.test

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import ws.oceanos.core.dsl.FlowDSL
import ws.oceanos.core.graph.{PTEdge, FlowGraph, PTGraph}


@RunWith(classOf[JUnitRunner])
class PTGraphTest extends FlatSpec with ShouldMatchers with FlowDSL {
  import ws.oceanos.core.dsl._
  
  "A PTGraph" should "have at minimum of one place and one transition when created" in {
    val f = nop
    val net = PTGraph(FlowGraph(f))
    assert(net.places.size === 1)
    assert(net.transitions.size === 1)
    assert(net.edges.size === 1)
    assert(net.inputsOf(net.transitions.head).exists(_.from == net.places.head))
  }

  it should "have an initial marking" in {
    val f = nop
    val net = PTGraph(FlowGraph(f))
    assert(net.places.size === 1)
    assert(net.transitions.size === 1)
    assert(net.edges.size === 1)
    assert(net.inputsOf(net.transitions.head).exists(_.from == net.places.head))
    assert(net.initialMarking.size === 1)
    assert(net.initialMarking.contains(net.places.head))
  }

  it should "allow the creation of pipelines" in {
    val flow = nop~>nop~>nop
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 3)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 5)
  }

  it should "allow the creation of conditional outputs" in {
    val flow = nop~>nop~>filter(_ == true)~>nop
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 3)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 5)
    assert(net.edges.collect{ case edge @ PTEdge(_,_,Some(_)) => edge }.size === 1 )
  }

  it should "allow the creation of transforms" in {
    val flow = nop~>map(s => s)~>nop
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 3)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 5)
  }

  it should "allow the creation of parallel syncs" in {
    val f1 = nop
    val f2 = nop
    val f3 = nop
    val f4 = nop
    val flow1 = f1 ~> f2 ~> merge~>f4
    val flow2 = f1 ~> f3 ~> merge~>f4
    val net = PTGraph(FlowGraph(flow1,flow2))
    assert(net.places.size === 5)
    assert(net.transitions.size === 4)
    assert(net.edges.size === 9)
  }

}

