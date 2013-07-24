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
    val net = PTGraph(FlowGraph(nop()))
    assert(net.places.size === 1)
    assert(net.transitions.size === 1)
    assert(net.edges.size === 1)
    assert(net.inputsOf(net.transitions.head).exists(_.from == net.places.head))
  }

  it should "have an initial marking" in {
    val net = PTGraph(FlowGraph(nop()))
    assert(net.places.size === 1)
    assert(net.transitions.size === 1)
    assert(net.edges.size === 1)
    assert(net.inputsOf(net.transitions.head).exists(_.from == net.places.head))
    assert(net.initialMarking.size === 1)
    assert(net.initialMarking.contains(net.places.head))
  }

  it should "allow the creation of pipelines" in {
    val flow = nop(0)~>nop(1)~>nop(2)
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 3)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 5)
  }

  it should "allow the creation of conditional outputs" in {
    val flow = nop(0)~>nop(1)~>filter(_ == true)~>nop(2)
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 3)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 5)
    assert(net.edges.collect{ case edge @ PTEdge(_,_,Some(_)) => edge }.size === 1 )
  }

  it should "allow the creation of transforms" in {
    val flow = nop(0)~>map(s => s)~>nop(1)
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 3)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 5)
  }

  it should "allow the creation of parallel syncs" in {
    val flow1 = nop(0) ~> nop(1) ~> merge ~> nop(3)
    val flow2 = nop(0) ~> nop(2) ~> merge ~> nop(3)
    val net = PTGraph(FlowGraph(flow1,flow2))
    assert(net.places.size === 5)
    assert(net.transitions.size === 4)
    assert(net.edges.size === 9)
  }

}

