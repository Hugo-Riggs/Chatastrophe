package websockets

import akka.http.scaladsl.model.ws.BinaryMessage
import akka.stream.scaladsl.Sink
import org.scalatest.{ Matchers, WordSpec }

object Server extends App {
  //#websocket-example-using-core
  import akka.actor.ActorSystem
  import akka.stream.ActorMaterializer
  import akka.stream.scaladsl.{ Source, Flow }
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model.ws.UpgradeToWebSocket
  import akka.http.scaladsl.model.ws.{ TextMessage, Message }
  import akka.http.scaladsl.model.{ HttpResponse, Uri, HttpRequest }
  import akka.http.scaladsl.model.HttpMethods._


  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  //#websocket-handler
  val greeterWebSocketService =
  Flow[Message]
    .mapConcat {
      case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }
  //#websocket-handler

  //#websocket-request-handling
  val requestHandler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/greeter"), _, _, _) =>
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => upgrade.handleMessages(greeterWebSocketService)
        case None          => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }
  //#websocket-request-handling
import ConnectionService._
  val bindingFuture =
      Http().bindAndHandle(ConnectionService.route, interface = "localhost", port=8080)    //Http().bindAndHandleSync(requestHandler, interface = "localhost", port = 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  Console.readLine()

  import system.dispatcher // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

