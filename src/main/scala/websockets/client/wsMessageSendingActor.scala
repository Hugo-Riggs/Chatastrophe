package websockets.client

import websockets.Client
import akka.actor._

object wsMessageSendingActor {

  case class Message(text: String)

  case class OutgoingDestination(destination: ActorRef)

  def props(actor: String) = Props(new wsMessageSendingActor(actor))
}

class wsMessageSendingActor(actor: String) extends Actor with ActorLogging {
  import wsMessageSendingActor._

  var client = context.system.deadLetters

  def receive = {
    case Message(text) =>
      log.info("client trying to send message {}", text)

    case OutgoingDestination(destination) =>
      client = destination
      context.watch(destination)
  }

}
