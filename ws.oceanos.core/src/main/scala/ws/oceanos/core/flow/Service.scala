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
package ws.oceanos.core.flow

import akka.actor.Props
import ws.oceanos.core.event.{OutInActor, ProxyActor, TransformActor, EventProcessorActor}
import ws.oceanos.core.graph.{FlowGraph, PTGraph}

trait Service extends Flow  {
  val properties: Props = Props.empty
  val id: String = ""

  def flow(flows: Flow*): Service = new ComplexService(flows)

  def map(f: Any => Any): Service = new Transform(f)

  def outin: Service = new OutIn

  implicit def component2props(flow: Service): Props = flow.properties
}

case class SimpleService(serviceProps: Props, override val id: String) extends Service {
  override val properties = Props(new ProxyActor(serviceProps))
}

class ComplexService(flows: Seq[Flow]) extends Service {
  override val properties = Props(new EventProcessorActor(PTGraph(FlowGraph(flows:_*))))
    .withMailbox("os-event-processor-mailbox")
  override val id = FlowRegistry.nextId("Flow")
}

class Transform(function: Any => Any) extends Service {
  override val properties = Props(new TransformActor(function))
  override val id = FlowRegistry.nextId("Transform")
}

class OutIn extends Service {
  override val properties = Props(new OutInActor)
  override val id = FlowRegistry.nextId("OutIn")
}
