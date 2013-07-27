package ws.oceanos.core.event.test

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Success

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
class Forward(actor: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  implicit val timeout = Timeout(2.seconds)
  def receive: Receive = {
    case s =>
      val client = sender
      actor ? s onComplete {
        case Success(x) => client ! x
        case _ => log.info("fwd failure")
      }
  }
}