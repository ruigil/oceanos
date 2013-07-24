package ws.oceanos.core.graph

import ws.oceanos.core.dsl._

class FlowGraph extends DiGraph[Flow,FlowEdge] {

  type ConcreteGraph = FlowGraph

  def in: Set[Component] =
    for {
      init <- nodes collect { case i: InMarker => i }
      service <- successors(init).collect { case s: Component => s }
    } yield service

  def out: Set[Component] =
    for {
      out <- nodes collect { case i: OutMarker => i }
      service <- predecessors(out).collect { case s: Component => s }
    } yield service

  def copy(ns: Set[Flow], es: List[FlowEdge]) = new FlowGraph {
    override val nodes = ns
    override val edges = es
  }
}

case class FlowEdge(f: Flow, t: Flow) extends DiEdge[Flow](f,t)

object FlowGraph {

  def apply(flows: Flow*): FlowGraph = {

    val graph = flows.foldLeft(new FlowGraph)( (g,f) => f match {
      case edge: FlowAssoc =>
        edgeList(edge).foldLeft(g)( (g,e) => g + FlowEdge(e._1,e._2))
      case flow: Flow => g + flow
    })

    graph

  }

  private def edgeList(flow: Flow): List[(Flow,Flow)] = {

    def unwrap(flow: Flow): List[Flow] = flow match {
      case FlowAssoc(from,to) => unwrap(from) ::: unwrap(to)
      case s => s :: Nil
    }

    val elements = unwrap(flow)
    elements zip (elements drop 1)
  }
}