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

import akka.actor._
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import ws.oceanos.core.graph.{Place, PTEdge, PTGraph}
import akka.actor.OneForOneStrategy
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object EventProcessorActor {
  case class RequestMsg(messages: Any)
  case class ReplyMsg(messages: Any)
  case class Finish(message: Any)
  case class In(messages: Any)
  case class Out(messages: Any)
  case class Work(message: Any, ref: ActorRef)

  sealed abstract class Event(name: String = "Event", time: Long = System.currentTimeMillis())
  case class Init(service: ActorRef, message: Any) extends Event(name = "Init")
  case class Reply(service: ActorRef, message: Any) extends Event(name = "Reply")
  case class Request(service: ActorRef, message: Any) extends Event(name = "Request")
  case class Resume(service: ActorRef, message: Any) extends Event(name = "Resume")
  case class Stop(service: ActorRef, message: Any) extends Event(name = "Stop")

  object ServiceState extends Enumeration {
    val Idle, Running = Value
  }

  class Service(var state: ServiceState.Value, val inputs: List[Input], val outputs: List[Output])
  class Input(val cond: Option[Any=>Boolean], val queue: mutable.Queue[Any])
  class Output(val cond: Option[Any=>Boolean], val queue: mutable.Queue[Any])
}


class EventProcessorActor(graph: PTGraph) extends Actor with ActorLogging {
  import context._
  import EventProcessorActor._

  val logQueue = ListBuffer[Event]()
  val workQueue = mutable.Queue.empty[Any]

  val queues = graph.places.map( p => (p,mutable.Queue.empty[Any])).toMap
  val initial = graph.initial.map(p => queues(p))
  val terminal = graph.terminal.map(p => queues(p))

  val services = (for {
    transition <- graph.transitions
    ref = context.actorOf(transition.service.properties,transition.service.id)
    inputs = for (PTEdge(place:Place,_,cond) <- graph.inputsOf(transition))
             yield new Input(cond,queues(place))

    outputs = for (PTEdge(_,place:Place,cond) <- graph.outputsOf(transition))
              yield new Output(cond,queues(place))

  } yield (ref,new Service(ServiceState.Idle,inputs,outputs))).toMap


  def receive = init


  def init: Receive = {
    case message =>
      process(Init(sender,message))
      become(running(sender))
  }
  
  def running(client: ActorRef): Receive = {
    case ReplyMsg(message) =>
      process( Reply(sender,message ) )
    case Out(message) =>
      client ! message
      become(paused)
    case Finish(message) =>
      client ! message
      if (workQueue.size > 0) {
        become(init)
        val Work(msg,ref) = workQueue.dequeue()
        self.tell(msg,ref)
      }

     case msg => workQueue.enqueue(Work(msg,sender))
  }

  def paused: Receive = {
    case ReplyMsg(message) =>
      process( Reply(sender,message ) )

    case message =>
      process( Resume(sender,message) )
      become(running(sender))
  }


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
    //case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }


  private def process(event: Event): Unit = {

    logQueue += event
    //log.info(s"log[$event]")
    event match {
      case Init(ref,message) =>
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
        self ! Finish(if (result.size == 1) result.head else result)
        terminal.foreach(_ clear())
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
        val message = service.inputs.map(_.queue.front)
        process(Request(ref, if (message.size==1) message.head else message) )
      }

      if (service.state == ServiceState.Running && !active) service.state = ServiceState.Idle
    }
  }

 
}