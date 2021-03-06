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

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import akka.actor.AllForOneStrategy


class ProxyActor(serviceProps: Props) extends Actor with ActorLogging {
  import context._
  import EventProcessorActor._

  val service = context.actorOf(serviceProps)

  def receive = request

  def request: Receive = {
    case RequestMsg(message) =>
      service ! message
      become(reply)
  }
  def reply: Receive = {
    case message =>
      parent ! ReplyMsg(message)
      become(request)
  }

  override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
    //case _: IllegalStateException => Resume
    //case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }

}



