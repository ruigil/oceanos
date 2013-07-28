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
import org.scalatest.matchers.ShouldMatchers
import ws.oceanos.core.flow.FlowContext
import ws.oceanos.core.graph.{PTEdge, FlowGraph, PTGraph}


@RunWith(classOf[JUnitRunner])
class PTGraphTest extends FlatSpec with ShouldMatchers with FlowContext {

  "A PTGraph" should "have at minimum of two places and one transition when created" in {
    val net = PTGraph(FlowGraph(nop()))
    assert(net.places.size === 2)
    assert(net.transitions.size === 1)
    assert(net.edges.size === 2)
  }

  it should "have an initial marking and terminal marking" in {
    val net = PTGraph(FlowGraph(nop()))
    assert(net.places.size === 2)
    assert(net.transitions.size === 1)
    assert(net.edges.size === 2)
    assert(net.initial.size === 1)
    assert(net.terminal.size === 1)
  }

  it should "allow the creation of pipelines" in {
    val flow = nop(0)~>nop(1)~>nop(2)
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 4)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 6)
  }

  it should "allow the creation of conditional outputs" in {
    val flow = nop(0)~>nop(1)~>filter(_ == true)~>nop(2)
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 4)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 6)
    assert(net.edges.collect{ case edge @ PTEdge(_,_,Some(_)) => edge }.size === 1 )
  }

  it should "allow the creation of transforms" in {
    val flow = nop(0)~>map(s => s)~>nop(1)
    val net = PTGraph(FlowGraph(flow))
    assert(net.places.size === 4)
    assert(net.transitions.size === 3)
    assert(net.edges.size === 6)
  }

  it should "allow the creation of parallel syncs" in {
    val flow1 = nop(0) ~> nop(1) ~> merge ~> nop(3)
    val flow2 = nop(0) ~> nop(2) ~> merge ~> nop(3)
    val net = PTGraph(FlowGraph(flow1,flow2))
    assert(net.places.size === 6)
    assert(net.transitions.size === 4)
    assert(net.edges.size === 10)
  }

  it should "allow the creation of non deterministic choices" in {
    val flow1 = nop(0) ~> ndc() ~> nop(1) ~> nop(3)
    val flow2 = nop(0) ~> ndc() ~> nop(2) ~> nop(3)
    val net = PTGraph(FlowGraph(flow1,flow2))
    assert(net.places.size === 4)
    assert(net.transitions.size === 4)
    assert(net.edges.size === 8)
  }

}

