package pingPong

/*
* This code is here as an example of using the akka actor system
 */

import akka.actor._

case object PingMessage
case object PongMessage
case object StartMessage
case object StopMessage

class Ping(pong: ActorRef) extends Actor {
  var count = 0
  def incrementAndPrint { count += 1; println("ping") }
  override def receive = {
    case StartMessage =>
      incrementAndPrint
      pong ! PingMessage
    case PongMessage =>
      incrementAndPrint
      if (count < 99) {
        sender ! PingMessage
      } else{
        println("ping stopped")
        sender ! StopMessage
      }
    case _: Any => println("Got something unexpected.")

  }
}


class Pong extends Actor {
  override def receive = {
    case PingMessage =>
      println("pong")
      sender ! PongMessage
    case StopMessage =>
      println("pong stopped")
      context.stop(self)
    case _ => println("got something unexpected")
  }
}

object pingPongTest extends App {
  val system = ActorSystem("PingPongSystem")
  val pong = system.actorOf(Props[Pong], name = "pong")
  val ping = system.actorOf(Props(new Ping(pong)), name = "ping")
  //start the thing
  ping ! StartMessage
}
