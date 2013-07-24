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
