package RequestReply

/***
 * Two actors: remoteA, a server class, and LogActor which keeps chat history.
 */

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Udp}
import akka.util.{ByteString, Timeout}
import gui.util.GUI_Request

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Promise}

/***
 * Remote actor (RemoteA) is our server, clients actors may connect to it.
 * The communication protocol is matched through a RemoteCommand abstract case class.
 */

 sealed abstract class RemoteCommand
  case class BroadcastIncoming(text: String) extends RemoteCommand

  case object Broadcast extends RemoteCommand
  case object Poll extends RemoteCommand

object RemoteA {
  implicit val ec = ExecutionContext.global    // needed for futures (concurrency)
  implicit val timeout = Timeout(5.seconds)    // a timeout for networking latency manegment
  def props = Props(new ChatastropheServer)               // actor configuration object
}

class ChatastropheServer extends Actor
with CommunicationProtocol {
  import RemoteA._        // Bring in scope, necessary values from the companion object.

  // A mutable collection of connected users
  var connections =
    collection.mutable.Map[(String, ActorRef), InetSocketAddress]() // A map of connected clients


  def SendMessage(msg: String, socketAddress: InetSocketAddress) = {
    Udp.Send(ByteString(msg), socketAddress)
  }

  //  When a connect is received with an
  //  user name which does not exist
  //  in connections, then the user
  //  is allowed to connect.
  def addUser(user: String, actorRef: ActorRef, socketAddress: InetSocketAddress) = {
      connections += (user, actorRef) -> socketAddress
      val p = Promise[ByteString]()
      p.completeWith(ChatLogger.read)
      p.future onSuccess {
        case s => Udp.Send(s, socketAddress)
      }
  }


  //  When a connect is received by the server
  //  but the user name string associated with
  //  the new actor reference already exists
  //  in connections.
  def rejectUser(socketAddress: InetSocketAddress) ={
    SendMessage("User with this name already exists in channel.\n", socketAddress)
  }


  /// UDP hook
  import context.system   // bring in system context
  IO(Udp) ! Udp.SimpleSender

 def ready(send: ActorRef): Receive = {
   case msg: String =>
     connections.values.foreach( client =>
       send ! Udp.Send(ByteString(msg), client)
     )
 }

  def receive = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  // end UDP hook

    // Server actor's end of line communication
    case communication: protocol => communication match {
      case Connect(address, user, actorRef) =>  // A user joins


      case Disconnect(user, actorRef) =>    //  Signal received from a client when they wish to disconnect from the server.
        connections.remove(user, actorRef)
    }

//    case command: RemoteCommand => command match {
//
//      case Broadcast  =>   // Send latest chat messages to all users. TODO: Perhaps obsolete
//        val f = (logActor ? ReadFromLog).mapTo[String]
//        val p = Promise[String]()
//        p completeWith f
//        p.future onSuccess {
//          case s =>
//            connections foreach { e =>
//              val(user, actorRef) = e
//              actorRef ! ReceiveMessage(s)
//            }
//        }
//
//      case BroadcastIncoming(text) =>  // Fan incoming messages to all clients.
//        connections foreach {
//          client  =>
//            val(user, actorRef) = client
//            actorRef ! ReceiveMessage(text)
//        }
//
//      case Poll =>  // Send chat log to single client.
//        val f = (logActor ? ReadFromLog).mapTo[String]
//        val p = Promise[String]()
//        p completeWith f
//        p.future onSuccess {
//          case s =>
//            sender ! ReceiveMessage(s)
//        }
//
//      case GUI_Request =>
//        logActor forward GUI_Request
//    }
//
//    // For messages outside of protocols
//    case DeadLetter(msg, from, to) =>
//      println("Received dead letter LocalA")
//
//    case any: Any => println("received " + any); sender ! ReceiveMessage("unsupported message (" + any + ")")
 }
}
