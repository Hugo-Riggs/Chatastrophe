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

  // Config is under src/main/resources
  val config = system.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

  // Initially our HTTP server binding is null
  var bindingFuture : scala.concurrent.Future[akka.http.scaladsl.Http.ServerBinding] = null

  // Server factory takes an argument and creates a singleton server object
  def apply(args : String) = {

    args.toLowerCase match {
      case "start" if bindingFuture == null => {
        import ConnectionService._
        bindingFuture =
          Http().bindAndHandle(ConnectionService.route, interface, port)
    }
      case "stop" => {
          import system.dispatcher // for the future transformations
          bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
      }
      case _ if bindingFuture == null => {
        println("Valid arguments are (start) and (stop).")
      }
      case _ if bindingFuture != null => {
        println("Server already started")
      }
    }

  }

}

