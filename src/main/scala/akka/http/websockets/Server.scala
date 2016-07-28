package akka.http.websockets
// Two-way communication	Flow.fromSinkAndSource, or Flow.map for a request-response protocol
// Nice documentation page: http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/index.html#akka.http.scaladsl.model.ws.package
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.testkit._

import scala.concurrent._

object myImplicits {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
}

object Server extends App with WebService {
  import myImplicits._
  val (host, port) = ("localhost", 8080)
/*
* Using route
* - get
* - flow for echo service
 */

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("Echo: " + txt)
    case _ => TextMessage("unsupported message type")
  }

  val serverSource = Http().bindAndHandle(route, host, port)

   override def route: Route = path("") {
     get {
        handleWebSocketMessages(echoService)//complete("test")
      }
   }
/*
  // tests:
  // create a testing probe representing the client-side
  val wsClient = WSProbe()

  // WS creates a WebSocket request for testing
  WS("", wsClient.flow) ~> echoService ~>
    check {
      // check response for WS Upgrade headers
      isWebSocketUpgrade shouldEqual true

      // manually run a WS conversation
      wsClient.sendMessage("Peter")
      wsClient.expectMessage("Peter")

      wsClient.sendMessage(BinaryMessage(ByteString("abcdef")))
      wsClient.expectNoMessage(100.millis)

      wsClient.sendMessage("John")
      wsClient.expectMessage("John")

      wsClient.sendCompletion()
      wsClient.expectCompletion()
    }
*/

/*
* Using request mapped to response handler
*
*/
  /*
  val serverSource = Http().bind(host, port)
  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity = "Chatastrophe http response: ")

    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }


  // use with Http().bind()
  serverSource
    .runForeach { connection =>
      println("Accepted new connection from " + connection.remoteAddress)
      connection handleWithSyncHandler requestHandler
    }
*/
}

trait WebService {
  def route: Route
}
