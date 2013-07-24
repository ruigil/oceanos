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



