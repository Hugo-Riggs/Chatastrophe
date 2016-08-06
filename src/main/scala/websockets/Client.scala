package websockets

/*
* Class of a client side, websocket connection, for the chat service.
* Does, import akka/scala dependencies
*
* Member functions:
*   incomingFlow -> handles strict messages or streams
*   connect -> attach source, flow and sink to server
*
*   Stream modeling (SO FAR) :
*  /======================================================================================\
*    __________________                                ________________________
*   | outgoing: Source |----------------------------->| Sink @ websocketServer |
*
*    ____________________           ______________          __________________________
*   | incomingSink: Sink | <-------| incomingFlow |<-------| Source @ websocketServer |
*
* \======================================================================================/
*
 */

import akka.actor.ActorSystem
import akka.Done
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.stream.impl._
import akka.http.scaladsl.model.ws._

import scala.concurrent.duration._
import scala.concurrent.Future

// Client request on the IP/Port
class Client {

  implicit val system = ActorSystem()
  import system.dispatcher
  implicit val materializer = ActorMaterializer()
  val MAX_FRAMES = 100
  val TIMEOUT = 5 seconds
  val PARALLELISM = 3


  val incomingSink: Sink[Message, Future[Done]] =
    Sink.foreach[Message] {
      case message: TextMessage.Strict =>
        println(message.text)
      case any: Any =>
        println("incomingSink Any = " + any)
    }


  def incomingFlow(implicit mat: Materializer): Flow[Message, Message, _] = Flow[Message]
    .collect {
      case TextMessage.Strict(msg) =>
        Future.successful(msg)
      case TextMessage.Streamed(stream) => { stream
        .limit(MAX_FRAMES) //Max frames, see what are frames
        .completionTimeout(TIMEOUT)
        .runFold("")(_ + _) // Merge frames
      }
    }
    .mapAsync(PARALLELISM)(identity _)
    .map {
      case msg: String => TextMessage.Strict(msg)
    }


  def connect(address: String, port: Int) = {
    val outgoing = Source.single(TextMessage("D. VA"))
    val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("ws://"+address+":"+port+"/"))


    // the materialized value is a tuple with
    // upgradeResponse is a Future[WebSocketUpgradeResponse] that
    // completes or fails when the connection succeeds or fails
    // and closed is a Future[Done] with the stream completion from the incomingSink sink
    val (upgradeResponse, closed) =
    outgoing
      .viaMat(webSocketFlow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
      .via(incomingFlow)
      .toMat(incomingSink)(Keep.both) // also keep the Future[Done]
      .run()


    // just like a regular http request we can access response status which is available via upgrade.response.status
    // status code 101 (Switching Protocols) indicates that server support WebSockets
    val connected = upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }

    // in a real application you would not side effect here
    connected.onComplete(println)
    closed.foreach(_ => println("closed"))
  }

}
