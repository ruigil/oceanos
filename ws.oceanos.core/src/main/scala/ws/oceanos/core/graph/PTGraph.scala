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

import ws.oceanos.core.flow._

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

  def initial: Set[Place] = places.collect{ case p @ Place(_,PlaceType.I) => p }

  def terminal: Set[Place] = places.collect{ case p @ Place(_,PlaceType.O) => p }

}

trait PT

object PlaceType extends Enumeration {
  type PlaceType = Value
  val I, O, N = Value
}
import PlaceType._

case class Place(name: String, placeType: PlaceType = N) extends PT
case class Transition(service: Service) extends PT

case class PTEdge(f: PT, t: PT, predicate: Option[Any=>Boolean] = None) extends DiEdge[PT](f,t)


object PTGraph {
  import collection._
  private val serviceMap = mutable.Map.empty[Service,PTEdge]

  def apply(flowGraph: FlowGraph): PTGraph = {

    val net = new PTGraph

    net.
      map(createPT(_,flowGraph)).
      map(modifyNDC(_,flowGraph)).
      map(defineOutputs(_,flowGraph))

  }

  private def createPT(net: PTGraph, flowGraph: FlowGraph) = {
    val services = flowGraph.nodes collect { case s: Service => s }
    val in = flowGraph.in
    val out = flowGraph.out

    services.foldLeft(net) { (n,service) =>
      val placeType = if (in contains service) I else N
      val inputEdge = PTEdge( Place(service.id, placeType), Transition(service) )
      serviceMap(service) = inputEdge
      if (out contains service) {
        n + inputEdge + PTEdge( Transition(service), Place(service.id, O) )
      } else n + inputEdge
    }
  }

  private def modifyNDC(net: PTGraph, flowGraph: FlowGraph) = {
    flowGraph.edges.foldLeft(net) { (n,e) => e match {
      case FlowEdge(source: NonDetChoice, target: Service) =>
        val targets = flowGraph.successors(source) collect {case s: Service => s}
        val place = Place(source.id, N)
        val removeOld = targets.foldLeft(n) { (n,target) =>
          val oldPlace = serviceMap(target).from
          n - oldPlace
        }
        targets.foldLeft(removeOld) { (n,target) =>
          val newInput = PTEdge(place, serviceMap(target).to)
          serviceMap(target) = newInput
          n + newInput
        }
      case _ => n
      }
    }
  }

  private def defineOutputs(net: PTGraph, flowGraph: FlowGraph) = {
    flowGraph.edges.foldLeft(net) { (n,e) => e match {
        // service outputs
        case FlowEdge(source: Service, target: Service) =>
          n + PTEdge(serviceMap(source).to, serviceMap(target).from)
        // condition outputs
        case FlowEdge(source: Service, condition: Condition) =>
          val targets = flowGraph.successors(condition) collect {case s: Service => s}
          targets.foldLeft(n) { (n,target) =>
            n + PTEdge(serviceMap(source).to, serviceMap(target).from, Some(condition.predicate) )
          }
        // parallel sync
        case FlowEdge(source: Service, sync: ParallelSync) =>
          val targets = flowGraph.successors(sync) collect {case s: Service => s}
          targets.foldLeft(n) { (n,target) =>
              if (n.edges.exists( e => e.to == serviceMap(target).from)) {
                val place = Place(FlowRegistry.nextId(target.id), N)
                n + PTEdge(serviceMap(source).to, place) + PTEdge(place, serviceMap(target).to)
              }
              else n + PTEdge(serviceMap(source).to, serviceMap(target).from)
          }
        case FlowEdge(source: Service, ndc: NonDetChoice) =>
          n + PTEdge(serviceMap(source).to, Place(ndc.id,N))

        case _ => n
      }
    }
  }

}

