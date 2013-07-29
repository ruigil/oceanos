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

  def receive = init


  def init: Receive = {
    case message =>
      epState.process(Init(sender,message))
      become(running(sender))
  }
  
  def running(client: ActorRef): Receive = {
    case ReplyMsg(message) =>
      epState.process( Reply(sender,message ) )
    case Finished(message) =>
      client ! message
      become(init)
      unstashAll()
    case Out(message) =>
      client ! message
      unstashAll()
      become(paused(client))

     case _ => stash()
  }

  def paused(client: ActorRef): Receive = {
    case ReplyMsg(message) =>
      epState.process( Reply(sender,message ) )

    case message =>
      epState.process( Resume(sender,message) )
      become(running(sender))
  }


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
    //case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }
 
}