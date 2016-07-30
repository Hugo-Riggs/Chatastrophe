package websockets

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

class session(id: Int, actorSystem: ActorSystem){
 // websocket flow for messages through system
  //private[this] val sessionActor = actorSystem.actorOf(Props(classOf[sessionActor], id))


  def webSocketFlow(user: String): Flow[Message, Message, _] = ???

}

object session{
  def apply(id: Int)(implicit actorSystem: ActorSystem) = new session(id, actorSystem) // implicit forces parameter occurence
}