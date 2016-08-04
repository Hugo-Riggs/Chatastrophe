package websockets
// API:
// http://doc.akka.io/api/akka/2.4
// Introducing akka
// https://www.youtube.com/watch?v=UY3fuHebRMI
import akka.http.scaladsl.model.ws.BinaryMessage
import akka.stream.scaladsl.Sink
import org.scalatest.{Matchers, WordSpec}

object Server {
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

  var bindingFuture : scala.concurrent.Future[akka.http.scaladsl.Http.ServerBinding] = startServer

    def startServer = {

      import ConnectionService._

      bindingFuture =
        Http().bindAndHandle(ConnectionService.route, interface = "localhost", port = 8080) //Http().bindAndHandleSync(requestHandler, interface = "localhost", port = 8080)
      bindingFuture
      //println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
      //Console.readLine()
    }

    def stopServer = {

      import system.dispatcher // for the future transformations
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    }



}

