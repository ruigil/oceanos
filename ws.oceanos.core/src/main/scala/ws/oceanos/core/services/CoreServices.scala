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
package ws.oceanos.core.services

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._

// Echoes strings.
class Echo(text: String) extends Actor with ActorLogging {
  def receive: Receive = {
    case x :: xs =>
      sender ! x + xs.mkString + text
    case s =>
      sender ! s + text
  }
}


class RequestActor extends Actor {
  implicit val timeout: Timeout = 1.second // for the actor 'asks'
  import context.dispatcher
  def receive: Receive = {
    case message =>
      val client = sender
      context.parent ? message onComplete {
        client ! _
      }
  }
}



