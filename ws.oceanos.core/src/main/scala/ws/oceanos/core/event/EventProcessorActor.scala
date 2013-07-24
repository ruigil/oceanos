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
import scala.util.{Success, Failure}
import akka.actor.OneForOneStrategy
import ws.oceanos.core.graph.PTGraph


class EventProcessorActor(graph: PTGraph) extends Actor with Stash with ActorLogging {
  import context._

  val epState = new EventProcessorState(graph,context)

  var start = 0L

  //def receive = accept

  // TODO: This mechanics assumes request-reply. What about request only?
  def receive: Receive = {
    case message =>
      //start = System.currentTimeMillis()
      //println("start actor"+start)
      epState.init(message)
      //println("in"+(System.currentTimeMillis()-start))
      val client = sender
      epState.next()
      //println("next"+(System.currentTimeMillis()-start))
      become(request(client))
  }
  
  def request(client: ActorRef): Receive = {

    case Success(message) =>
      //println("success"+(System.currentTimeMillis()-start))
      epState.done(sender,message)
      //println("done"+(System.currentTimeMillis()-start))
      if (epState.hasNext) epState.next()
      else {
        //println("reply"+(System.currentTimeMillis()-start))
        //log.info(s"reply $request")
        client ! message
        become(receive)
        //println("become"+(System.currentTimeMillis()-start))
        unstashAll()
        //println("unstash"+(System.currentTimeMillis()-start))
      }

    case Failure(message) =>
      client ! message
      unstashAll()
      become(receive)

    case _ => stash()
  }


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
    case _: IllegalStateException => Resume
    case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }
 
}