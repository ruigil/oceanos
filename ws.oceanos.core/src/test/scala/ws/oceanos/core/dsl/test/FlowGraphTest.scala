package ws.oceanos.core.dsl.test

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import ws.oceanos.core.dsl.FlowDSL
import ws.oceanos.core.graph.FlowGraph


@RunWith(classOf[JUnitRunner])
class FlowGraphTest extends FlatSpec with ShouldMatchers with FlowDSL {

  "A FlowGraph" should "have at minimum one node when created" in {
    val graph = FlowGraph(nop())
    assert(graph.nodes.size === 1)
  }

  it should "allow the creation of graphs without edges" in {
    val graph = FlowGraph(nop(0),nop(1),nop(2))
    assert(graph.nodes.size === 3)
    assert(graph.edges.size === 0)
  }

  it should "allow the creation of graphs with one edge" in {
    val graph = FlowGraph(nop(0)~>nop(1))
    assert(graph.nodes.size === 2)
    assert(graph.edges.size === 1)
  }

  it should "allow the creation of graphs with many edges" in {
    val f1 = nop(0)
    val f2 = nop(1)
    val graph = FlowGraph(f1~>f2, f2~>f1)
    assert(graph.nodes.size === 2)
    assert(graph.edges.size === 2)
    assert(graph.nodes.contains(f1))
    assert(graph.nodes.contains(f2))
  }

  it should "be able to return the targets for a node" in {
    val f1 = nop(0)
    val f2 = nop(1)
    val f3 = nop(2)
    val graph = FlowGraph(f1~>f2, f2~>f1, f1~>f3)
    assert(graph.nodes.size === 3)
    assert(graph.edges.size === 3)
    assert(graph.successors(f1).contains(f2))
    assert(graph.successors(f1).contains(f3))
    assert(graph.successors(f2).contains(f1))
  }

  it should "be able to return the initial services marked by in" in {
    val f3 = nop(0)
    val graph = FlowGraph(in~>f3, in~>f3, f3~>nop(1))
    assert(graph.in.size === 1)
    assert(graph.in.contains(f3))
  }

  it should "be able to have hyper edges" in {
    val graph = FlowGraph(nop(0)~>nop(1)~>nop(2), nop(2)~>nop(1)~>nop(0))
    assert(graph.nodes.size === 3)
    assert(graph.edges.size === 4)
  }

  it should "be able to reuse hyper edges" in {
    val f3 = nop(2)
    val he1 = nop(0)~>nop(1)~>f3
    val he2 = nop(3)~>he1
    val f4 = nop(4)
    val he3 = he2~>f4
    val graph = FlowGraph(he2,nop(5)~>he3)
    assert(graph.nodes.size === 6)
    assert(graph.edges.size === 5)
    assert(graph.successors(f3).contains(f4))
  }

  it should "be able to have conditions" in {
    val graph = FlowGraph(nop(0)~>filter(_ == true)~>nop(1))
    assert(graph.nodes.size === 3)
    assert(graph.edges.size === 2)
  }

  it should "be able to have maps" in {
    val graph = FlowGraph(nop(0)~>map(s => s)~>nop(1))
    assert(graph.nodes.size === 3)
    assert(graph.edges.size === 2)
  }

  it should "be able to have parallel syncs" in {
    val graph = FlowGraph(nop(0)~>merge~>nop(1))
    assert(graph.nodes.size === 3)
    assert(graph.edges.size === 2)
  }

}

