package remoting
/*
* Two actors: remoteA, a server class, and LogActor which keeps chat history.
 */

import akka.actor._

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise, ExecutionContext }



object remoteInit extends App {
  def init{
    val system = ActorSystem("ChatastropheRemoteActorSys")
    val remoteActor = system.actorOf(remoteA.props, "remoteActor")
    println("remote actor up. . . ")
  }
}


/*
* LogActor is used to keep a chat sessions, chat history.
 */

case class WriteToLog(text: String)
case object ReadFromLog
case object DidNewWriteOccur

class LogActor extends Actor {
  private var log: String = ""

  private def write(text: String): Unit =  log += text + "\n"

  def receive = {
    case WriteToLog(text) => write(text)
    case ReadFromLog => sender ! log
    case GUI_Request => sender ! log
  }
}


/*
 * Remote actor is our server, clients actors may connect to it.
 */
case class UserConnected(user: String, actorRef: ActorRef)
case class Join(address: String, withName: String)
case class SendMessage(text: String)
case class ReceiveMessage(text: String)
case class Disconnect(user: String)
case class BroadcastIncoming(text: String)

case object GUI_Request
case object Broadcast
case object Poll

object remoteA {
  def props = Props(new remoteA)
}

class remoteA extends Actor {
  implicit val ec = ExecutionContext.global
  implicit val timeout = Timeout(5 seconds)

  // A log actor from which clients may poll
  val logActor = context.actorOf(Props(classOf[LogActor]))

  // A hash of connected clients
  var  connections = Map.empty[String, ActorRef]

  // Upon receiveing a message
  def receive = {
    case UserConnected(user, actorRef) =>   // A user joins
      connections += user -> actorRef
      logActor ! WriteToLog("user: " + user + " joined.")
      self ! Broadcast

    case ReceiveMessage(text) =>
      logActor ! WriteToLog(text)
      self ! BroadcastIncoming(text)
      //self ! Broadcast

    case Broadcast  =>   // Send latest chat messages to all users
      val f = (logActor ? ReadFromLog).mapTo[String]
      val p = Promise[String]()
      p completeWith f
      p.future onSuccess {
        case s =>
        connections foreach { e =>
          val(user, actorRef) = e
          actorRef ! ReceiveMessage(s)
        }
      }

    case BroadcastIncoming(text) =>
      connections foreach { e =>
      val(user, actorRef) = e
      actorRef ! ReceiveMessage(text)
      }

    case Poll => {
      val f = (logActor ? ReadFromLog).mapTo[String]
      val p = Promise[String]()
      p completeWith f
      p.future onSuccess {
        case s =>
          sender ! ReceiveMessage(s)
      }
   }

    case Disconnect(user) =>    //  A client has disconnectd
      sender() ! ReceiveMessage("bye")
      connections -= user
      self ! ReceiveMessage("user " + user + " disconneccted")

    case GUI_Request =>
      logActor forward GUI_Request

    case any: Any => println("received " + any); sender ! ReceiveMessage("unsupported message (" + any + ")")
  }

}