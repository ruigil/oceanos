package ws.oceanos.core.event

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import akka.actor.AllForOneStrategy
import scala.concurrent.Future
import scala.util.Success


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
        //log.info(s"proxy count reply $request")
        //become(accept)
        requester ! result
      }
      //become(running(future))
    case m => log.info(s"message [$m] not recognized")
  }

  def running(future: Future[Any]): Receive = {
    case Cancel =>
      future.failed
      become(accept)
  }

  override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
    case _: IllegalStateException => Resume
    case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }

}



