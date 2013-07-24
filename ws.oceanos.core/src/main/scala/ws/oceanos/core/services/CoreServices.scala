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



