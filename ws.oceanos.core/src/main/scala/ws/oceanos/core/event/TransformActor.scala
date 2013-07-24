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
import ws.oceanos.core.event.EventProcessorState.Fire
import scala.util.Success

class TransformActor(function: Any => Any) extends Actor  {

  def receive: Receive = {
    case Fire(messages) =>
      val requester = sender
      messages.map(function).foreach(m => requester ! Success(m))
  }

}


