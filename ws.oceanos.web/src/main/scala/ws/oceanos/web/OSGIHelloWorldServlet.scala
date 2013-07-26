package ws.oceanos.web

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.felix.scr.annotations._
import org.osgi.service.component.ComponentContext
import org.osgi.service.http.HttpService
import akka.actor.ActorSystem
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Await
import java.util.logging.Logger
import ws.oceanos.core.flow.{Registry, FlowContext}

/*
 Small OSGI example, accessing the core from another bundle and
 servicing request on the HTTP Service
 */

@Component(immediate=true)
class OSGIHelloWorldServlet extends HttpServlet with FlowContext {
  private final val log = Logger.getLogger( getClass.getName )

  val path = "/"

  @Reference
  override val registry: Registry = null

  @Reference
  private val httpService: HttpService = null

  @Reference
  private val system: ActorSystem = null

  @Activate
  def activate(ctx: ComponentContext ) {
    httpService.registerServlet(path, this, null, null)
    log.info("OceanOS Hello World Servlet registered at http://localhost:8080/")
  }

  @Deactivate
  def deactivate(ctx: ComponentContext) {
    httpService.unregister(path)
    log.info("OS HTTP unregistered")
  }

  @Override
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.setContentType("text/plain; charset=utf-8")

    val service = flow( n("urn:hello") ~> filter(_ => true) ~> n("urn:world") )

    val main = system.actorOf(service, "Main")

    implicit val timeout = Timeout(3.seconds)
    implicit val ec = system.dispatcher

    val future = main ? "OSGI"
    val result = Await.result(future, timeout.duration).asInstanceOf[String]

    resp.getWriter.println(result)

  }


}
