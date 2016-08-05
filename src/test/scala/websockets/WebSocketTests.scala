package websockets

import org.scalatest.FunSuite

class SetSuite extends FunSuite {

  import Server._ // as connection is declared as variable it is created on Server import

  // Start The server
  println("Starting server. . . ")
  serverFactory("start")
  println("Server started. . . ")
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")


  // Client request on the IP/Port
  import akka.actor.ActorSystem
  import akka.Done
  import akka.http.scaladsl.Http
  import akka.stream.ActorMaterializer
  import akka.stream.Materializer
  import akka.stream.scaladsl._
  import akka.http.scaladsl.model._
  import akka.http.scaladsl.model.ws._
  import scala.concurrent.duration._

  import scala.concurrent.Future

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

/*
  //  http://doc.akka.io/docs/akka/2.4.9-RC1/scala/stream/stream-cookbook.html#draining-a-stream-to-a-strict-collection
  val MAX_ALLOWED_SIZE = 100

  val limited: Future[Seq[String]] =
    mySource.limit(MAX_ALLOWED_SIZE).runWith(Sink.seq)

  val ignoreOverflow: Future[Seq[String]] =
    mySource.take(MAX_ALLOWED_SIZE).runWith(Sink.seq)
*/

  val incoming: Sink[Message, Future[Done]] =
  Sink.foreach[Message] {
    case message: TextMessage.Strict =>
      println("TextMessage.Strict : "  + message.text)

    case txtStream: TextMessage.Streamed =>
      println(streamHandler(txtStream))

    case any: Any =>
      println("incoming Any = " + any)
  }

  def streamHandler(txtStream: TextMessage.Streamed): String = {
    "text stream"
  }


  import akka.stream.impl._
  def returnFlow(implicit mat: Materializer): Flow[Message, Message, _] = Flow[Message]
      .collect {
          case TextMessage.Strict(msg) =>
            Future.successful(println(msg))

          case TextMessage.Streamed(stream) => { stream
            //  .limit(100) //Max frames, see what are frames
             // .completionTimeout(5 seconds)
              .runFold("")(_ + _) // Merge frames
          //        .flatMap(msg => Future.successful(msg))
            }
      }
      .mapAsync(3)(identity _)
      .map {
        case msg: String => TextMessage.Strict(msg)
      }
 /*
  {
      case msg: String => TextMessage.Strict(msg)
      //case any: Any => { println(any); TextMessage.Strict(any.toString) }
    }
*/

  val outgoing = Source.single(TextMessage("D. VA"))
  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("ws://localhost:8080/"))

  // the materialized value is a tuple with
  // upgradeResponse is a Future[WebSocketUpgradeResponse] that
  // completes or fails when the connection succeeds or fails
  // and closed is a Future[Done] with the stream completion from the incoming sink
  val (upgradeResponse, closed) =
  outgoing
    .viaMat(webSocketFlow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
    .via(returnFlow)
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


  // Stop server after pressing enter
   Console.readLine()
   serverFactory("stop")
  println("Server stopped. . . ")
}
