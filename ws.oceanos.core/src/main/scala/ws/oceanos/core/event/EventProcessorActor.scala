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
import ws.oceanos.core.graph.PTGraph
import scala.util.Success
import scala.util.Failure
import akka.actor.OneForOneStrategy


class EventProcessorActor(graph: PTGraph) extends Actor with Stash with ActorLogging {
  import context._
  import ws.oceanos.core.event.EventProcessorState._

  val epState = new EventProcessorState(graph,context)

  def receive = accept

  // TODO: This mechanics assumes request-reply. What about request only?
  def accept: Receive = {
    case message =>
      val client = sender
      become(request(client))
      epState.process(Init(client,message, System.currentTimeMillis()))
  }
  
  def request(client: ActorRef): Receive = {
    running(client) orElse {
      case Out(message) =>
        client ! message
        unstashAll()
        become(waitForIn(client))

      case _ => stash()
    }
  }

  def waitForIn(client: ActorRef): Receive = {
    running(client) orElse {
      case message =>
        epState.process(InMsg(sender,message, System.currentTimeMillis()))
        become(request(client))
    }
  }

  def running(client: ActorRef): Receive = {
    case Success(message) =>
      epState.process( Done(sender,message, System.currentTimeMillis()))
    case Failure(message) =>
      // TODO: Failure should be handled differently
      client ! message
      become(accept)
      unstashAll()
    case Finished(message) =>
      client ! message
      become(accept)
      unstashAll()
  }


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
    case _: IllegalStateException => Resume
    case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }
 
}