package remoting
/*
* The Server
* received messages are broadcasted to clients.
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

case class WriteToLog(text: String)
case object Poll
case object ReadFromLog

class LogActor extends Actor {
  private var log: String = ""

  private def write(text: String): Unit = { log += text + "\n" }

  def receive = {
    case WriteToLog(text) => write(text)
    case ReadFromLog => sender ! log
    case any: Any => println("unhandled message received by logging actor (" + any + ". . .")
  }
}

class remoteA extends Actor {
  // Message of the day
  val messageOfTheDay = "MOTD: akka actors used for network communications"

  // A log actor from which clients may poll
  val logActor = context.actorOf(Props(classOf[LogActor]))

  // A hash of connected clients
  var  connections = Map.empty[String, ActorRef]

  def receive = {
    case UserConnected(user, actorRef) =>
      connections += user -> actorRef
      sender ! ReceiveMessage(messageOfTheDay)
      println("user: " + user + " joined.");

    case ReceiveMessage(text) =>
      logActor ! WriteToLog(text)
      self ! Poll
      //logActor ! ReadFromLog
    //logActor.write(text)
      /*connections foreach { e =>
        val(usrStr, actr) = e
        actr ! logActor.get//ReceiveMessage(text)
        actr ! logActor ! ReadFromLog*/

    case Disconnect(user) =>
      sender() ! ReceiveMessage("bye")
      connections -= user
      self ! ReceiveMessage("user " + user + " disconneccted")
    //connections = connections filterKeys(_ != user)

    case Poll  =>
      implicit val ec = ExecutionContext.global
      implicit val timeout = Timeout(5 seconds)
      val f = (logActor ? ReadFromLog).mapTo[String]
      val p = Promise[String]()

      p completeWith f

      p.future onSuccess {
        case s =>
        connections foreach { e =>
          val(u, a) = e
          println("\n\nPOLL RETURNING " + s+"\n" + " btw connections length is " + connections.size)
          a ! ReceiveMessage(s)
        }
      }

     /*
        val fu: Future[String] = (logActor ? ReadFromLog).mapTo[String]
        val s: String = fu.value.get.get
        connections foreach { e =>
          val(u, a) = e
          a ! ReceiveMessage(s)
        }*/

    case any: Any => println("received " + any); sender ! ReceiveMessage("unsupported message (" + any + ")")
  }

}