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

trait FlowContext extends Service with Element {

  val registry: Registry = FlowRegistry

  def register(uri: String, props: Props) = registry.register(uri, props)

  def n(uri: String, id: Int = 0): Service = {
    val s = registry.get(uri)
    if (s.isDefined) SimpleService(s.get,s"$uri:$id")
    else throw new Exception("Service not Found")
  }

  def nop(id: Int = 0): Service = n("nop",id)
}