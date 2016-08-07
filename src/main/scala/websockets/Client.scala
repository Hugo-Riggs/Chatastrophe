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
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.concurrent.Future

// Client request on the IP/Port
class Client(name: String, address: String, port: Int, actorSystem: ActorSystem) {

  implicit val system = ActorSystem()
  import system.dispatcher
  implicit val materializer = ActorMaterializer()
  val MAX_FRAMES = 100
  val TIMEOUT = 5 seconds
  val PARALLELISM = 3


  /*def spam(message: String, numberOfTimes: Int = 1000) = {
    import akka.actor.Props
    import akka.actor.Actor
    import scala.util.Random
    val talkActor = actorSystem.actorOf(Props(new Actor {
      import actorSystem.dispatcher
      import scala.concurrent.duration._

      var counter: Int = 0

      override def receive: Receive = {
        case message: String =>
          counter = counter + 1
          send(s"[$name] message #$counter")
          if (counter < numberOfTimes)
            actorSystem.scheduler.scheduleOnce(rand.seconds, self, message)
      }

      def rand: Int = 1 + Random.nextInt(9) //message every 1-10 seconds
    }))
    talkActor ! message
  }*/


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


  def clientInput = {
    val message = readLine
    TextMessage(name + ": " + message)
  }


  // SOURCE AND SINK
  val flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(
      incomingFlow.toMat(incomingSink)(Keep.both),
      Source(List(clientInput))// List(TextMessage("one"), TextMessage("two"))
        .concatMat(Source.maybe[Message])(Keep.right))(Keep.right)


  val (upgradeResponse, promise) =
    Http().singleWebSocketRequest(
      WebSocketRequest("ws://"+address+":"+port+"/"),
      flow)

  promise.success(None)

}

object Client {
  def apply(name: String, address: String, port: Int)(implicit actorSystem: ActorSystem): Client = {
    new Client(name, address, port, actorSystem)
  }
}