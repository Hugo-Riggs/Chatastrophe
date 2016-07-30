package websockets
//see http://doc.akka.io/docs/akka/2.4.8/scala/http/routing-dsl/websocket-support.html#server-side-websocket-support-scala
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

object ConnectionService {

  def route(implicit actorSystem: ActorSystem, materializer: Materializer): Route =
    pathPrefix("ws-chat" / IntNumber) { Id =>
    parameter('name) { userName =>
      handleWebSocketMessages(SessionManager.findOrCreate(Id).webSocketFlow(userName))
    }
  }

}
