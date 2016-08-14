package remoting
/*
* Two actors: remoteA, a server class, and LogActor which keeps chat history.
*
 */

import akka.actor._

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise, ExecutionContext }

// All case classes for networking events.
// Even those used by client exclusively.
case class UserConnected(user: String, actorRef: ActorRef)
case class Join(address: String, withName: String)
case class SendMessage(text: String)
case class ReceiveMessage(text: String)
case class Disconnect(user: String)


object remoteInit extends App {
  def init{
    val system = ActorSystem("ChatastropheRemoteActorSys")
    val remoteActor = system.actorOf(remoteA.props, "remoteActor")
    println("remote actor up. . . ")
  }
}

object remoteA {
  def props = Props(new remoteA)
}

/*
* LogActor is used to keep a chat sessions, chat history.
*
 */

case class WriteToLog(text: String)
case object Poll
case object ReadFromLog

class LogActor extends Actor {
  private var log: String = ""

  private def write(text: String): Unit = { log += text + "\n" }

  def receive = {
    case WriteToLog(text) => { println(text); write(text) }
    case ReadFromLog => sender ! log
    case any: Any => println("unhandled message received by logging actor (" + any + ". . .")
  }
}

/*
 * Remote actor is our server, clients actors may connect to it.
 */

class remoteA extends Actor {
  // Message of the day
  val messageOfTheDay = "MOTD: akka actors used for network communications"

  // A log actor from which clients may poll
  val logActor = context.actorOf(Props(classOf[LogActor]))

  // A hash of connected clients
  var  connections = Map.empty[String, ActorRef]

  // Upon receiveing a message
  def receive = {
    case UserConnected(user, actorRef) =>   // A user joins
      connections += user -> actorRef
      logActor ! WriteToLog("user: " + user + " joined.")
      //sender ! ReceiveMessage(messageOfTheDay)

    case ReceiveMessage(text) =>    // Client has sent a chat message to server, server receives message (a0)(1)
      logActor ! WriteToLog(text)   // (a0)(2) write to log
      println("step 2: write to log")
      self ! Poll                   // (a0)(3) send log to all clients
      println("step 3: send log to all clients")

    case Poll  =>   // Send latest chat messages to all users
      implicit val ec = ExecutionContext.global
      implicit val timeout = Timeout(5 seconds)
      val f = (logActor ? ReadFromLog).mapTo[String]
      val p = Promise[String]()

      p completeWith f

      p.future onSuccess {
        case s =>
        connections foreach { e =>
          val(u, a) = e
          println("poll returning " + s)
          a ! ReceiveMessage(s)
        }
      }

    case Disconnect(user) =>    //  A client has disconnectd
      sender() ! ReceiveMessage("bye")
      connections -= user
      self ! ReceiveMessage("user " + user + " disconneccted")

    case any: Any => println("received " + any); sender ! ReceiveMessage("unsupported message (" + any + ")")
  }

}