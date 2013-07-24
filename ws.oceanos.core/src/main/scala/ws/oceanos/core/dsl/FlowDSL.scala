package ws.oceanos.core.dsl

import akka.actor.Props

trait FlowDSL extends Component with Element {
  def register(uri: String, props: Props) = FlowRegistry.props(uri) = props
}