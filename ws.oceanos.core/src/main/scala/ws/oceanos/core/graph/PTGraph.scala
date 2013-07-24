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
package ws.oceanos.core.graph

import ws.oceanos.core.dsl._

class PTGraph extends DiGraph[PT,PTEdge,PTGraph] {

  def copy(ns: Set[PT], es: List[PTEdge]) = new PTGraph {
    override val nodes = ns
    override val edges = es
  }

  def places: Set[Place] = nodes collect { case p:Place => p }

  def transitions: Set[Transition] = nodes collect { case t:Transition => t }

  def inputsOf(transition: Transition): List[PTEdge] =
    for {
      edge @ PTEdge(place:Place,trans,_) <- edges
      if trans == transition
    } yield edge

  def outputsOf(transition: Transition): List[PTEdge] =
    for {
      edge @ PTEdge(trans,place:Place,_) <- edges
      if trans == transition
    } yield edge

  lazy val initialMarking: List[Place] = {
    // if an initial marking was specified, use it
    // or else search for places with no inputs
    val init = places.filter{_.placeType == PlaceType.In}
    if (!init.isEmpty) init.toList
    else sources.collect { case p:Place => p }
  }

}

trait PT

object PlaceType extends Enumeration {
  type PlaceType = Value
  val In, Out, Mid = Value
}
import PlaceType._

case class Place(name: String, placeType: PlaceType = Mid) extends PT
case class Transition(service: Component) extends PT

case class PTEdge(f: PT, t: PT, predicate: Option[Any=>Boolean] = None) extends DiEdge[PT](f,t)


object PTGraph {
  import collection._
  private val serviceMap = mutable.Map.empty[Component,PTEdge]

  def apply(flowGraph: FlowGraph): PTGraph = {

    val net = new PTGraph

    net.
      map(createPT(_,flowGraph)).
      map(defineOutputs(_,flowGraph))

  }

  private def createPT(net: PTGraph, flowGraph: FlowGraph) = {
    val services = flowGraph.nodes collect { case s: Component => s }
    val init = flowGraph.in

    services.foldLeft(net) { (n,service) =>
      val placeType = if (init contains service) In else Mid
      val edge = PTEdge( Place(service.id, placeType), Transition(service) )
      serviceMap(service) = edge
      n + edge
    }
  }

  private def defineOutputs(net: PTGraph, flowGraph: FlowGraph) = {
    flowGraph.edges.foldLeft(net) { (n,e) => e match {
        // service outputs
        case FlowEdge(source: Component, target: Component) =>
          n + PTEdge(serviceMap(source).to, serviceMap(target).from)
        // condition outputs
        case FlowEdge(source: Component, condition: Condition) =>
          val targets = flowGraph.successors(condition) collect {case s: Component => s}
          targets.foldLeft(n) { (n,target) =>
            n + PTEdge(serviceMap(source).to, serviceMap(target).from, Some(condition.predicate) )
          }
        // parallel sync
        case FlowEdge(source: Component, sync: ParallelSync) =>
          val targets = flowGraph.successors(sync) collect {case s: Component => s}
          targets.foldLeft(n) { (n,target) =>
              if (n.edges.exists( e => e.to == serviceMap(target).from)) {
                val place = Place(FlowRegistry.nextId(target.id), Mid)
                n + PTEdge(serviceMap(source).to, place) + PTEdge(place, serviceMap(target).to)
              }
              else n + PTEdge(serviceMap(source).to, serviceMap(target).from)
          }
        // out marker
        case FlowEdge(source: Component, out: OutMarker) =>
          val place = Place(FlowRegistry.nextId("Out"), Out)
          n + PTEdge(serviceMap(source).to, place)

        case _ => n
      }
    }
  }

}

