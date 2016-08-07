package websockets

/*
*
* A chat session which can hold multiple users and traffics messages from users.
* needs a sink and source, and to make use of a session actor which also communicates with user actors.
*
 */

import akka.actor.ActorSystem
import akka.actor.Props
import akka.{ Done, NotUsed }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.impl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import scala.concurrent.Future

class session(id: Int, actorSystem: ActorSystem){
  // useful but old example
  // https://github.com/ScalaConsultants/websocket-akka-http/blob/master/src/main/scala/io/scalac/akka/http/websockets/chat/ChatRoom.scala


 // websocket flow for messages through system
  private[this] val sessionActor = actorSystem.actorOf(Props(classOf[sessionActor], id))


  def webSocketFlow(user: String): Flow[Message, Message, _] =
    Flow[Message]
    .mapConcat {
      case tm: TextMessage => TextMessage(tm.textStream) :: Nil
      case _ => TextMessage("unsupported type") :: Nil
    }


  def sendMessage(message: UserMessage): Unit = sessionActor ! message
}

object session {
  def apply(id: Int)(implicit actorSystem: ActorSystem) = new session(id, actorSystem) // implicit forces parameter occurence
}
