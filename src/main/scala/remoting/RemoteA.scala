package remoting

/*
 * Two actors: remoteA, a server class, and LogActor which keeps chat history.
 */

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{ Promise, ExecutionContext }


/***
 * LogActor is used to keep a chat sessions, chat history.
 */

case class WriteToLog(text: String)
case object ReadFromLog

class LogActor extends Actor {

  private var log: String = ""

  private def write(text: String): Unit =  log += text

  def receive = {
    case WriteToLog(text) => write(text)
    case ReadFromLog => sender ! log
    case GUI_Request => sender ! log
  }
}


/***
 * Remote actor (RemoteA) is our server, clients actors may connect to it.
 * The communication protocol is matched through a RemoteCommand abstract case class.
 */

 sealed abstract class RemoteCommand
  case class BroadcastIncoming(text: String) extends RemoteCommand
  case object GUI_Request extends RemoteCommand
  case object Broadcast extends RemoteCommand
  case object Poll extends RemoteCommand

object RemoteA {

  implicit val ec = ExecutionContext.global
  implicit val timeout = Timeout(5 seconds)

  // A map of connected clients
  var connections = Map.empty[String, ActorRef] // TODO: can this be a val not var??

  def props = Props(new RemoteA)
}

class RemoteA extends Actor {

  import RemoteA._
  import CommunicationProtocol._

  // A log actor from which clients may poll
  val logActor = context.actorOf(Props(classOf[LogActor]))

  def receive = {

    case com: Comms => com match {

      case SendMessage(text) =>         // TellServer(str: String)
        println("") 

      case Connect(address, user, actorRef) =>  // A user joins 
        connections get user match { 
            case Some(s) => 
              actorRef ! ReceiveMessage("user already exists in channel\n") 
              actorRef ! PoisonPill
            case None =>
              connections += user -> actorRef  // add user to list of connected
              actorRef ! Poll  // send new client the chat messages in the channel
              val f = (logActor ? ReadFromLog).mapTo[String]
              val p = Promise[String]()
              p completeWith f
              p.future onSuccess {
                case s => actorRef ! ReceiveMessage( s )
                self ! ReceiveMessage("user: " + user + " joined.")
                actorRef ! ReceiveMessage("Hello " + user + " welcome to a Chatastrophe chat.")
              }
            }

      case ReceiveMessage(text) => // A user sent a message to the server
        logActor ! WriteToLog(text)
        self ! BroadcastIncoming(text)

      case Disconnect(user) =>    //  Signal received from a client when they wish to disconnect from the server.
        connections -= user
        self ! ReceiveMessage("user " + user + " disconneccted\n")
    }

    case command: RemoteCommand => command match {

      case Broadcast  =>   // Send latest chat messages to all users. TODO: Perhaps obsolete
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

      case BroadcastIncoming(text) =>  // Fan incoming messages to all clients. 
        connections foreach {
          client  =>
            val(user, actorRef) = client 
            actorRef ! ReceiveMessage(text)
        }

      case Poll => {  // Send chat log to single client.
        val f = (logActor ? ReadFromLog).mapTo[String]
        val p = Promise[String]()
        p completeWith f
        p.future onSuccess {
          case s =>
            sender ! ReceiveMessage(s)
        }
      }

      case GUI_Request =>
        logActor forward GUI_Request
    } 

    // For messages outside of protocols
    case DeadLetter(msg, from, to) =>
      println("Received dead letter LocalA")

    case any: Any => println("received " + any); sender ! ReceiveMessage("unsupported message (" + any + ")")
  }
}
