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

trait Element extends Flow {

  def filter(predicate: Any => Boolean): Element = Condition(predicate)

  def in: Element  = InMarker(FlowRegistry.nextId("In"))

  def out: Element  = OutMarker(FlowRegistry.nextId("Out"))

  def merge: Element  = ParallelSync(FlowRegistry.nextId("Init"))
}


case class Condition(predicate: Any => Boolean) extends Element

case class InMarker(id: String) extends Element

case class OutMarker(id: String) extends Element

// TODO: This should be configured wih a merge strategy
case class ParallelSync(id: String) extends Element

