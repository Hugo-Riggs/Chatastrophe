package websockets

import akka.actor.ActorSystem

class session(id: Int, actorSystem: ActorSystem){

  import akka.actor.Props

  import akka.{ Done, NotUsed }
  import akka.http.scaladsl.Http
  import akka.stream.ActorMaterializer
  import akka.stream.scaladsl._
  import akka.stream.impl._
  import akka.http.scaladsl.model._
  import akka.http.scaladsl.model.ws.Message
  import akka.http.scaladsl.model.ws.TextMessage
//  import akka.stream.scaladsl.FlowGraph.Implicits._ // depricated?

  import scala.concurrent.Future


 // websocket flow for messages through system
  private[this] val sessionActor = actorSystem.actorOf(Props(classOf[sessionActor], id))


  def webSocketFlow(user: String): Flow[Message, Message, _] =
    Flow[Message]
    .mapConcat {
      case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
      case _: Any => Nil
    }


  /*
    Flow(ActorRefSource(bufferSize = 5, OverflowStrategy.fail, [UserMessage])) {
      implicit builder =>
        sessionSource => //it's Source from parameter

          //flow used as input, it takes Messages
          val fromWebsocket = builder.add(
            Flow[Message].collect {
              case TextMessage.Strict(txt) => IncomingMessage(user, txt)
            })

          //flow used as output, it returns Messages
          val backToWebsocket = builder.add(
            Flow[UserMessage].map {
              case UserMessage(author, text) => TextMessage(s"[$author]: $text")
            }
          )

          //send messages to the actor, if sent also UserLeft(user) before stream completes.
          val sessionActorSink = Sink.actorRef[sessionEvent](sessionActor, UserLeft(user))

          //merges both pipes
          val merge = builder.add(Merge[sessionEvent](2))

          //Materialized value of Actor who sits in the chatroom
          val actorAsSource = builder.materializedValue.map(actor => UserJoined(user, actor))

          //Message from websocket is converted into IncommingMessage and should be sent to everyone in the room
          fromWebsocket ~> merge.in(0)

          //If Source actor is just created, it should be sent as UserJoined and registered as particiant in the room
          actorAsSource ~> merge.in(1)

          //Merges both pipes above and forwards messages to session represented by sessionActor
          merge ~> sessionActorSink

          //Actor already sits in chatRoom so each message from room is used as source and pushed back into the websocket
          sessionSource ~> backToWebsocket

          // expose ports
          (fromWebsocket.inlet, backToWebsocket.outlet)
    }
*/

  def sendMessage(message: UserMessage): Unit = sessionActor ! message

}

object session {
  def apply(id: Int)(implicit actorSystem: ActorSystem) = new session(id, actorSystem) // implicit forces parameter occurence
}
