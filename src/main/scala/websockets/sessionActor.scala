package websockets

import akka.actor.{Actor, ActorRef}

class sessionActor(sessionId: Int) extends Actor {
  var users: Map[String, ActorRef] = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case UserJoined(name, actorRef) =>
      users += name -> actorRef
      broadcast(SystemMessage(s"User $name joined channel . . . "))
      println(s"User $name joined channel[$sessionId]")

    case UserLeft(name) =>
      println(s"User $name left channel[$sessionId]")
      broadcast(SystemMessage(s"User $name left channel[$sessionId]"))
      users -= name

    case msg: UserMessage =>
      broadcast(msg)
  }

  def broadcast(message: UserMessage): Unit = users.values.foreach(_ ! message)

}
