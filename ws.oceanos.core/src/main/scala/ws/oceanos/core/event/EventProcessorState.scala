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
package ws.oceanos.core.event

import akka.actor.{ActorContext, ActorRef}
import ws.oceanos.core.graph.{PTEdge, Place, PTGraph}
import collection._

object EventProcessorState {
  case class RequestMsg(messages: Any)
  case class ReplyMsg(messages: Any)
  case class Finished(messages: Any)
  case class In(messages: Any)
  case class Out(messages: Any)

  class Event(name: String, time: Long = System.currentTimeMillis())
  case class Init(service: ActorRef, message: Any) extends Event("Init")
  case class Reply(service: ActorRef, message: Any) extends Event("Reply")
  case class Request(service: ActorRef, message: Any) extends Event("Request")
  case class Resume(service: ActorRef, message: Any) extends Event("Resume")
  case class Stop(service: ActorRef, message: Any) extends Event("Stop")

  object ServiceState extends Enumeration {
    val Idle, Running, Active = Value
  }

  class Service(var state: ServiceState.Value, val inputs: List[Input], val outputs: List[Output])
  class Input(val cond: Option[Any=>Boolean], val queue: mutable.Queue[Any])
  class Output(val cond: Option[Any=>Boolean], val queue: mutable.Queue[Any])
}


class EventProcessorState(graph:PTGraph, context: ActorContext) {
  import EventProcessorState._

  var log: List[Event] = Nil


  val queues = graph.places.map( p => (p,mutable.Queue.empty[Any])).toMap
  val initial = graph.initial.map(p => queues(p))
  val terminal = graph.terminal.map(p => queues(p))

  val services = (for {
    transition <- graph.transitions
    ref = context.actorOf(transition.service.properties,transition.service.id)
    inputs = for (PTEdge(place:Place,_,cond) <- graph.inputsOf(transition)) yield new Input(cond,queues(place))
    outputs = for (PTEdge(_,place:Place,cond) <- graph.outputsOf(transition)) yield new Output(cond,queues(place))
  } yield (ref,new Service(ServiceState.Idle,inputs,outputs))).toMap


  def process(event: Event): Unit = {

    log = event +: log
    //println(s"log[$event]")
    event match {
      case Init(ref,message) =>
        queues.values.foreach(_ clear())
        initial.foreach(_ enqueue message)
        if (hasNext) next()
        else process(Stop(ref,message))
      case Reply(ref,message) =>
        if (services(ref).state == ServiceState.Running) {
          services(ref).state = ServiceState.Idle
          services(ref).inputs.foreach(_.queue.dequeue())
          services(ref).outputs
            .filter(_.cond.getOrElse((a:Any) => true)(message))
            .foreach(_.queue enqueue message)
        }
        if (hasNext) next()
        else process(Stop(ref,message))
      case Request(ref,message) =>
        services(ref).state = ServiceState.Running
        ref.tell(RequestMsg(message),context.self)
      case Resume(ref,message) =>
        val running = services.collect {
          case (rRef,service) if service.state == ServiceState.Running => rRef
        }
        running.foreach( a => a.tell(In(message),context.self) )
      case Stop(ref,message) =>
        val result = terminal.flatMap(_ headOption)
        context.self ! Finished(if (result.size == 1) result.head else result)
    }
  }

  // start active services

  // have we reach an end state ?
  private def hasNext: Boolean = terminal.forall(_.isEmpty)


  private def next() {
    // start active transitions
    // update service state, active, running, idle
    services.foreach { case (ref,service) =>
      val active = service.inputs.forall(_.queue.size > 0)

      if (service.state == ServiceState.Idle && active) {
        service.state = ServiceState.Active
        val message = service.inputs.map(_.queue.front)

        process(Request(ref, if (message.size==1) message.head else message) )
      }

      if (service.state == ServiceState.Running && !active) service.state = ServiceState.Idle
    }
  }

  override def toString: String = log mkString "\n"

}
