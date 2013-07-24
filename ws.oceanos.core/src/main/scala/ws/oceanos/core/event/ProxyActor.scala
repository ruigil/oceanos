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
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import akka.actor.AllForOneStrategy


class ProxyActor(serviceProps: Props) extends Actor with ActorLogging {
  import context._
  import EventProcessorState._

  implicit val timeout = Timeout(5 seconds)
  val service = context.actorOf(serviceProps)

  var count = 0

  def receive = accept

  def accept: Receive = {
    case Fire(messages) =>
      val future = service ? (if (messages.length==1) messages.head else messages)
      val requester = sender
      future onComplete { result =>
        //become(accept)
        requester ! result
      }
      //become(running(future))
    case m => log.info(s"message [$m] not recognized")
  }

  /* something is wrong with this. for some reason it deadlocks.
  def running(future: Future[Any]): Receive = {
    case Cancel =>
      future.failed
      become(accept)
  }
  */

  override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
    case _: IllegalStateException => Resume
    case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }

}



