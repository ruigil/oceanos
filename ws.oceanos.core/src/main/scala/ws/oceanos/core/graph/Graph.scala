package ws.oceanos.core.graph

trait Graph[N, E <: Edge[N]] {
  type ConcreteGraph <: Graph[N,E]

  val nodes: Set[N] = Set.empty[N]
  val edges: List[E] = List.empty[E]
  def + (node: N): ConcreteGraph = copy(nodes + node, edges)
  def + (edge: E): ConcreteGraph = {
    // no duplicate edges
    if (edges.forall(e => e.n1 != edge.n1 || e.n2 != edge.n2)){
      copy(nodes + edge.n1 + edge.n2, edge :: edges)
    } else copy(nodes, edges)
  }
  def successors(node: N) = edges.collect{ case e: Edge[N] if e.n1 == node => e.n2 }
  def predecessors(node: N) = edges.collect{ case e: Edge[N] if e.n2 == node => e.n1 }
  def neighbors(node: N) = successors(node) ::: predecessors(node)
  def map( f: ConcreteGraph => ConcreteGraph): ConcreteGraph = f(copy(nodes,edges))
  def copy(nodes: Set[N], edges: List[E]): ConcreteGraph
  override def toString = "Graph[\nNodes[\n"+ (nodes mkString "\n")+"\n]\nEdges[\n"+(edges mkString "\n")+"\n]\n]"
}

trait DiGraph[N, E <: DiEdge[N]] extends Graph[N,E] {
  type ConcreteGraph <: DiGraph[N,E]
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
