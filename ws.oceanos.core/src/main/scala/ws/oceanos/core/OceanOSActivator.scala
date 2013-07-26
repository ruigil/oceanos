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

import org.osgi.framework._
import akka.osgi.ActorSystemActivator
import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, Config}

class OceanOSActivator extends ActorSystemActivator {

  // TODO: it makes no sense that the core has osgi dependencies
  // this activator must be outsourced to another module, that bridges
  // that capability
  def configure(context: BundleContext, system: ActorSystem): Unit = {
    registerService(context,system)
  }

  override def getActorSystemConfiguration(context: BundleContext): Config = {
    ConfigFactory.parseString(
      """
        | os-event-processor-mailbox {
        |    mailbox-type = "akka.dispatch.UnboundedDequeBasedMailbox"
        |  }
      """.stripMargin)
  }

  override def getActorSystemName(context: BundleContext): String = "OceanOS"

}