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
import akka.{Done, NotUsed}
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


  def incomingSink: Sink[Message, Future[Done]] =
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
    var message, line = ""
    do {
        line = readLine
        message += line
    }while(line != ".")
     TextMessage(name + ": " + message)
  }

  // SOURCE AND SINK
  def flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(
      incomingFlow.toMat(incomingSink)(Keep.both),
      Source(List(clientInput)).alsoTo(outgoingFlow).concatMat(Source.maybe[Message])(Keep.right)
    )(Keep.right)


  import akka.stream.Graph
  def outgoingFlow: Graph[Message, NotUsed] = {
    GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
      val broadcast = b.add(Broadcast[Message](1))
      Source.single("t") ~> broadcast.in
      broadcast.out ~>
      ClosedShape
    }
  }
/*
Graph[ClientGraphShape[In, Out], NotUsed] = {
    GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._
      val priorityMerge = b.add(MergePreferred[In](1))
      val balance = b.add(Balance[In](workerCount))
      val resultsMerge = b.add(Merge[Out](workerCount))

      priorityMerge ~> balance

      for (i <- 0 until workerCount)
        balance.out(i) ~> worker ~> resultsMerge.in(i)
      GraphClientShape(
        fromServerWebsocket= priorityMerge.in(0),
        toServerWebsocket = resultsMerge.out)
    }
}
*/


  val (upgradeResponse, promise) =
    Http().singleWebSocketRequest( WebSocketRequest("ws://"+address+":"+port+"/"), flow)

  promise.success(None)
}

object Client {
  def apply(name: String, address: String, port: Int)(implicit actorSystem: ActorSystem): Client = {
    new Client(name, address, port, actorSystem)
  }
}