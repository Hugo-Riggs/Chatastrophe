package websockets

/*
*
* A chat session which can hold multiple users and traffics messages from users.
* needs a sink and source, and to make use of a session actor which also communicates with user actors.
*
* Sink[ input stream - , output materializer ]
*
* Source[ output stream + , output materializer + ]
*
* Flow[ -input stream, +output stream, +output materializer ]
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
import scala.concurrent.{ Promise, Future }
import akka.stream.scaladsl.GraphDSL

class session(id: Int, actorSystem: ActorSystem){


 // websocket flow for messages through system
  private[this] val sessionActor = actorSystem.actorOf(Props(classOf[sessionActor], id))

  def flow: Flow[Message, Message, _]  =
  Flow[Message]
    .mapConcat {
      case tm: TextMessage => TextMessage(tm.textStream) :: Nil
      case _ => TextMessage("unsupported type") :: Nil
    }

  def sink: Sink[Message, _] =  Sink.ignore

  def source: Source[Message, Promise[Option[Message]]] = Source.maybe[Message]

  def webSocketFlow(user: String): Flow[Message, Message, _] = flow

  def sendMessage(message: UserMessage): Unit = sessionActor ! message
}

object session {
  def apply(id: Int)(implicit actorSystem: ActorSystem) = new session(id, actorSystem) // implicit forces parameter occurence
}
