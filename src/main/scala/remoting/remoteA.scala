package remoting
/*
* The Server
* received messages are broadcasted to clients.
 */

import akka.actor._

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

class remoteA extends Actor {

  val messageOfTheDay = "MOTD: akka actors used for network communications"

  var  connections = Map.empty[String, ActorRef]
  def receive = {
    case UserConnected(user, actorRef) => connections += user -> actorRef; println("user: " + user + " joined.");
      sender() ! ReceiveMessage(messageOfTheDay)
    case ReceiveMessage(text) => connections foreach { e =>
      val(s, a) = e
      a ! ReceiveMessage(text)
    }
    case Disconnect(user) => sender() ! ReceiveMessage("bye")
      connections -= user
      //connections = connections filterKeys(_ != user)
      self ! ReceiveMessage("user " + user + " disconneccted")
    case any: Any => println("received " + any); sender() ! ReceiveMessage("unsupported message")
  }

}