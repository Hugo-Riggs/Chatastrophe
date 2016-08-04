package websockets

import org.scalatest.FunSuite


class SetSuite extends FunSuite {

  import Server._ // as connection is declared as variable it is created on Server import

  // Start The server
   // startServer
    println("Server started. . . ")

  // Client request on the IP/Port
  import akka.actor.ActorSystem
  import akka.Done
  import akka.http.scaladsl.Http
  import akka.stream.ActorMaterializer
  import akka.stream.scaladsl._
  import akka.http.scaladsl.model._
  import akka.http.scaladsl.model.ws._

  import scala.concurrent.Future

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  // Future[Done] is the materialized value of Sink.foreach,
  // emitted when the stream completes
  val incoming: Sink[Message, Future[Done]] =
  Sink.foreach[Message] {
    case message: TextMessage.Strict =>
      println(message.text)
  }

  // send this as a message over the WebSocket
  val outgoing = Source.single(TextMessage("hello world!"))

  // flow to use (note: not re-usable!)
  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("ws://localhost:8080/ws-chat"))

  // the materialized value is a tuple with
  // upgradeResponse is a Future[WebSocketUpgradeResponse] that
  // completes or fails when the connection succeeds or fails
  // and closed is a Future[Done] with the stream completion from the incoming sink
  val (upgradeResponse, closed) =
  outgoing
    .viaMat(webSocketFlow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
    .toMat(incoming)(Keep.both) // also keep the Future[Done]
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

 println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
 Console.readLine()

  stopServer
  println("Server stopped. . . ")
}
