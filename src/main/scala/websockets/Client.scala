package websockets

class Client{}
/*
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
import scala.concurrent.{Promise, Future}
import scala.concurrent.duration._


object Client {
  def apply(name: String, address: String, port: Int)(implicit actorSystem: ActorSystem): Client = {
    new Client(name, address, port, actorSystem)
  }
}


// Client request on the IP/Port
class Client(name: String, address: String, port: Int, actorSystem: ActorSystem) {

  implicit val system = ActorSystem()
  import system.dispatcher
  implicit val materializer = ActorMaterializer()
  val MAX_FRAMES = 100
  val TIMEOUT = 5 seconds
  val PARALLELISM = 3
  val overflowStrategy = akka.stream.OverflowStrategy.dropHead

  def incomingSink: Sink[Message, Future[Done]] =
    Sink.foreach[Message] {
      case message: TextMessage.Strict =>
        println(message.text)
      case any: Any =>
        println("incomingSink Any (unsupported) = " + any)
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


///////////////////////////////
/*
* The challenge in between these forward slashes,
* is to create a source flow for sending messages
* to server.
*
* Previously and through examples it just sends 1 pre-programmed message.
*
 */

  /*def clientInput = {
    TextMessage(name + ": joined session")
  }

  val helloSource: Source[Message, NotUsed] =
    Source.single(TextMessage("hello world!"))

  // SOURCE AND SINK
  def flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(
      incomingFlow.toMat(incomingSink)(Keep.both),    // SINK
      Source(List(clientInput)).concatMat(Source.maybe[Message])(Keep.right)   //   SOURCE
    )(Keep.right)
*/

 def flow: Flow[Message, Message, _] = {

  import akka.actor._
  import websockets.client.wsMessageSendingActor

  val wsSource: Source[Message, ActorRef] = Source.actorRef[wsMessageSendingActor](Int.MaxValue, overflowStrategy).map(msg => TextMessage(msg.text))

   val topicConnection = context.system.actorOf(wsMessageSendingActor.props(topic))

        // Create graph of pipe to websocket
        Flow.fromGraph(GraphDSL.create(wsSource) {
          implicit builder =>{
            import akka.stream.Graph
            import GraphDSL.Implicits._
            import akka.stream.FlowShape

            val merge = b.add(Merge[Any](2))
            val toActor = b.add(Sink.actorRef(wsMessageSendingActor, PoisonPill))

            val toWebSocket = builder.add(Flow[Message].map {
              msg: Message => toActor ! msg
            })

            val transformIncoming = builder.add(incomingFlow.toMat(Message)(incomingSink))

            builder.materializedValue ~> Flow[ActorRef].map(wsMessageSendingActor.OutgoingDestination) ~> merge.in(0)
            transformIncoming ~> merge.in(1)

            merge ~> toActor

            FlowShape.of(transformIncoming.in, toWebSocket.out)
          }  // BUILDER END


        })  // FLOW FROM GRAPH END

}


//////////////////////////////////////////////

  val (upgradeResponse, future) =
    Http().singleWebSocketRequest( WebSocketRequest("ws://"+address+":"+port+"/"), flow)

//  promise.success(None)
}
*/
