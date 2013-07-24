package ws.oceanos.core

//import org.apache.felix.scr.annotations.Properties
//import org.apache.felix.scr.annotations.Reference
//import org.apache.felix.scr.annotations.Service
import org.apache.felix.scr.annotations.Component
import akka.actor.{Props, ActorSystem}
import ws.oceanos.core.services._
import ws.oceanos.core.dsl.FlowDSL


@Component(immediate=true)
object Main extends App with FlowDSL {
  implicit val system = ActorSystem("OceanOS")

  sys.addShutdownHook(system.shutdown())

  register("os://hello", Props(classOf[Echo],"Hello"))
  register("os://world", Props(classOf[Echo],"World"))

  val service = flow( n("os://hello") ~> filter(_ == true) ~> n("os://world") )

  val main = system.actorOf(service, "Main")

}
