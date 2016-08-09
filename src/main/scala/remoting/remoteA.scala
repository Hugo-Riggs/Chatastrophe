package remoting

import akka.actor._

object remoteInit {
  def init{
    val system = ActorSystem("remoteActorSystem")
    val remoteActor = system.actorOf(remoteA.props, "remoteActor")
    println("remote actor up. . . ")
    remoteActor ! "local test message"
  }
}

object remoteA {
  def props = Props(new remoteA)
}

class remoteA extends Actor {

  def receive = {
    case text: String => println("remote actor received: " + text)
    case any: Any => println("received " + any)
  }

}