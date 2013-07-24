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
package ws.oceanos.core.graph

trait Graph[N, E <: Edge[N], T <: Graph[N,E,T] ] {

  val nodes: Set[N] = Set.empty[N]
  val edges: List[E] = List.empty[E]
  def + (node: N): T = copy(nodes + node, edges)
  def + (edge: E): T = {
    // no duplicate edges
    if (edges.forall(e => e.n1 != edge.n1 || e.n2 != edge.n2)){
      copy(nodes + edge.n1 + edge.n2, edge :: edges)
    } else copy(nodes, edges)
  }
  def successors(node: N) = edges.collect{ case e: Edge[N] if e.n1 == node => e.n2 }
  def predecessors(node: N) = edges.collect{ case e: Edge[N] if e.n2 == node => e.n1 }
  def neighbors(node: N) = successors(node) ::: predecessors(node)
  def map( f: T => T): T = f(copy(nodes,edges))
  def copy(nodes: Set[N], edges: List[E]): T
  override def toString =
    "Graph[\nNodes[\n"+ (nodes mkString "\n")+"\n]\nEdges[\n"+(edges mkString "\n")+"\n]\n]"
}

trait DiGraph[N, E <: DiEdge[N], T <: DiGraph[N,E,T]] extends Graph[N,E,T] {
  def sources: List[N] = nodes.filter( n => edges.forall(e => e.to != n ) ).toList
  def sinks: List[N] = nodes.filter( n => edges.forall(e => e.from != n ) ).toList
}

trait Edge[N] {
  def n1: N
  def n2: N
}

class DiEdge[N](val from: N, val to: N) extends Edge[N] {
  def n1 = from
  def n2 = to
}
