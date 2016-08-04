package websockets

import akka.actor.ActorRef

case class UserMessage(sender: String, text: String)

object SystemMessage {
  def apply(text: String) = UserMessage("System", text)
}

sealed trait sessionEvent

case class UserJoined(name: String, userActor: ActorRef) extends sessionEvent

case class UserLeft(name: String) extends sessionEvent

case class IncomingMessage(sender: String, message: String) extends sessionEvent

