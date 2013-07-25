package ws.oceanos.web

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.felix.scr.annotations._
import org.osgi.service.component.ComponentContext
import org.osgi.service.http.HttpService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import ws.oceanos.core.dsl.FlowDSL
import ws.oceanos.core.services.Echo
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Await

/*
 Small OSGI example, accessing the core from another bundle and
 servicing request on the HTTP Service
 */

@Component(immediate=true)
class OSGIHelloWorldServlet extends HttpServlet with FlowDSL {
  val log: Logger = LoggerFactory.getLogger(getClass)

  val path = "/"

  @Reference
  private val httpService: HttpService = null

  @Reference
  private val system: ActorSystem = null

  @Activate
  def activate(ctx: ComponentContext ) {
    httpService.registerServlet(path, this, null, null)

    log.info("{} registered", getClass.getSimpleName)
  }

  @Deactivate
  def deactivate(ctx: ComponentContext) {
    httpService.unregister(path)
    log.info("{} unregistered", getClass.getSimpleName)
  }

  @Override
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.setContentType("text/plain")
    resp.setCharacterEncoding("UTF-8")

    register("urn:hello", Props(classOf[Echo]," Hello"))
    register("urn:world", Props(classOf[Echo]," World"))

    val service = flow( n("urn:hello") ~> filter(_ => true) ~> n("urn:world") )

    val main = system.actorOf(service, "Main")

    implicit val timeout = Timeout(3.seconds)
    implicit val ec = system.dispatcher

    val future = main ? "OSGI"
    val result = Await.result(future, timeout.duration).asInstanceOf[String]

    resp.getWriter.println(result)

  }


}
