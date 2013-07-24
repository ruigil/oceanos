package ws.oceanos.core.dsl

import akka.actor.{Props, Actor}
import scala.reflect.ClassTag
import ws.oceanos.core.event.{ProxyActor, TransformActor, EventProcessorActor}
import ws.oceanos.core.services.Echo
import ws.oceanos.core.graph.{FlowGraph, PTGraph}

/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
trait Component extends Flow  {
  val properties: Props = Props.empty
  val id: String = ""

  def flow(flows: Flow*): Component = new ComplexService(flows)

  def n(uri: String, id: Int = 0): Component = {
    val s = FlowRegistry.props.get(uri)
    if (s.isDefined) SimpleService(s.get,s"$uri:$id")
    else throw new Exception("Service not Found")
  }

  def nop: Component = n("nop")

  def map(f: Any => Any): Component = new Transform(f)

  implicit def processor2props(flow: Component): Props = flow.properties
}

case class SimpleService(serviceProps: Props, override val id: String) extends Component {
  override val properties = Props(new ProxyActor(serviceProps))
}

class ComplexService(flows: Seq[Flow]) extends Component {
  override val properties = Props(new EventProcessorActor(PTGraph(FlowGraph(flows:_*))))
    .withMailbox("os-event-processor-mailbox")
  override val id = FlowRegistry.nextId("Flow")
}

class Transform(function: Any => Any) extends Component {
  override val properties = Props(new TransformActor(function))
  override val id = FlowRegistry.nextId("Transform")
}
