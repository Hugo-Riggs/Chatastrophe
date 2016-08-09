package remoting

import akka.actor._

object localA {
  def props: Props = Props(new localA)
}

class localA extends Actor {

  def receive = {
    case text: String => {
      println("localActor sends: " + text)
      val selection = context.actorSelection("akka.tcp://remoteActorSystem@127.0.0.1:2552/user/remoteActor")
      selection ! text
    }
  }

}