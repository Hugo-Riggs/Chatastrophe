package io.akka.http.websockets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebsocket}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

// reference here: http://doc.akka.io/docs/akka/2.4.8/scala/http/routing-dsl/websocket-support.html#server-api
object Server extends App {

  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  val config = actorSystem.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

  val requestHandler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      req.header[UpgradeToWebsocket] match {
        case Some(upgrade) => upgrade.handleMessages(communicationWebSocketService)
        case None => HttpResponse(400, entity = "Not a valid websocket request!")
      }

    case r: HttpRequest =>
      //r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }


val binding = Http().bindAndHandleSync(requestHandler, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")

  val communicationWebSocketService =
    Flow[Message]
        .mapConcat {
          case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
          case bm: BinaryMessage =>
            bm.dataStream.runWith(Sink.ignore)
            Nil
        }


}