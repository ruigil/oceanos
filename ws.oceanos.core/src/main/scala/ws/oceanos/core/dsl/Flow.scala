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
package ws.oceanos.core.dsl

import akka.actor.Props

trait Flow {
  def ~>(to: Flow): Flow  = FlowAssoc(this,to)
  def to(to: Flow): Flow  = FlowAssoc(this,to)
}

case class FlowAssoc(from: Flow, to: Flow) extends Flow

object FlowRegistry {
  import collection._
  val props = mutable.Map("nop" -> Props.empty)
  var id = 0
  def nextId(prefix: String): String = { id = id + 1; prefix + id }
}
