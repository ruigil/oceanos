/*
 * Copyright 2013 Rui Gil.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.oceanos.core.graph.test

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import ws.oceanos.core.graph.{DiEdge, Graph}

@RunWith(classOf[JUnitRunner])
class GraphTest extends FlatSpec {

  class TestGraph(override val nodes: Set[TestNode] = Set.empty[TestNode],
                  override val edges: List[TestEdge] = List.empty[TestEdge])
    extends Graph[TestNode,TestEdge,TestGraph] {

    def copy(nodes: Set[TestNode], edges: List[TestEdge]) = new TestGraph(nodes,edges)
  }
  
  
  class TestNode
  
  class TestEdge(from: TestNode, to: TestNode) extends DiEdge[TestNode](from,to)
  
  "A Graph" should "have empty nodes and edges when created" in {
    val graph = new TestGraph
    assert(graph.nodes.size === 0)
    assert(graph.edges.size === 0)
  } 
  
  it should "allow to add nodes" in {
    val graph = new TestGraph
    val node = new TestNode
    val g2 = graph + node
    assert(g2.nodes.size === 1)
  }
  
  it should "not allow to have duplicated nodes" in {
    val graph = new TestGraph
    val node = new TestNode
    val g2 = (graph + node) + node
    assert(g2.nodes.size === 1)
  }

  it should "allow to add edges" in {
    val graph = new TestGraph
    val node1 = new TestNode
    val node2 = new TestNode
    val edge = new TestEdge(node1,node2)
    val g2 = graph + edge
    assert(g2.nodes.size === 2)
    assert(g2.edges.size === 1)
  }
  
  it should "not have duplicate edges" in {
    val graph = new TestGraph
    val node1 = new TestNode
    val node2 = new TestNode
    val edge1 = new TestEdge(node1,node2)
    val edge2 = new TestEdge(node1,node2)
    val result = (graph + edge1) + edge2
    assert(result.nodes.size === 2)
    assert(result.edges.size === 1)
  }

  it should "allow nodes with several outputs" in {
    val graph = new TestGraph
    val node1 = new TestNode
    val node2 = new TestNode
    val node3 = new TestNode
    val edge1 = new TestEdge(node1,node2)
    val edge2 = new TestEdge(node1,node3)
    val result = (graph + edge1) + edge2
    assert(result.nodes.size === 3)
    assert(result.edges.size === 2)
  }

  it should "allow nodes with several inputs" in {
    val graph = new TestGraph
    val node1 = new TestNode
    val node2 = new TestNode
    val node3 = new TestNode
    val edge1 = new TestEdge(node1,node2)
    val edge2 = new TestEdge(node3,node2)
    val result = (graph + edge1) + edge2
    assert(result.nodes.size === 3)
    assert(result.edges.size === 2)
  }

  it should "be able to retrieve the successors of a node" in {
    val graph = new TestGraph
    val node1 = new TestNode
    val node2 = new TestNode
    val node3 = new TestNode
    val edge1 = new TestEdge(node1,node2)
    val edge2 = new TestEdge(node1,node3)
    val result = (graph + edge1) + edge2
    assert(result.nodes.size === 3)
    assert(result.edges.size === 2)
    assert(result.successors(node1).contains(node2))
    assert(result.successors(node1).contains(node3))
  }

  it should "be able to retrieve the predecessors of a node" in {
    val graph = new TestGraph
    val node1 = new TestNode
    val node2 = new TestNode
    val node3 = new TestNode
    val edge1 = new TestEdge(node1,node2)
    val edge2 = new TestEdge(node3,node2)
    val result = (graph + edge1) + edge2
    assert(result.nodes.size === 3)
    assert(result.edges.size === 2)
    assert(result.predecessors(node2).contains(node1))
    assert(result.predecessors(node2).contains(node3))
  }

}

