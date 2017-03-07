package RequestReply

import akka.actor.Actor
import akka.io.Tcp._

class SimplisticHandler extends Actor {
  def receive = {
    case Received(data) => sender() ! Write(data)
    case PeerClosed     => context stop self
  }
}


