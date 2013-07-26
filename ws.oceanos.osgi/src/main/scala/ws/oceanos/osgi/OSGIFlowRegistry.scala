package ws.oceanos.osgi

import ws.oceanos.core.flow.Registry
import akka.actor.Props
import org.apache.felix.scr.annotations.{Deactivate, Activate, Service, Component}
import org.osgi.framework.BundleContext
import java.util.logging.Logger
import ws.oceanos.osgi.services.Echo

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
@Component(immediate=true)
@Service(Array(classOf[Registry]))
class OSGIFlowRegistry extends Registry {
  val log = Logger.getLogger(getClass.getName)
  import collection._

  @Activate
  def activate(context: BundleContext): Unit = {
    register("urn:hello", Props(classOf[Echo]," Hello"))
    register("urn:world", Props(classOf[Echo]," World"))
  }

  @Deactivate
  def deactivate(context: BundleContext): Unit = {
    log.info("Registry deactivated")
  }

  def get(uri: String): Option[Props] = propsMap.get(uri)
  def register(uri: String, props: Props) = propsMap(uri) = props
  private val propsMap = mutable.Map("nop" -> Props.empty)

}
