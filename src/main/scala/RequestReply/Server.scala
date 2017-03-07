package RequestReply

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.{IO, Tcp}

object Server {
  val system = ActorSystem("ChatastropheServer")
  val actor = system.actorOf(Props[ServerActor])
}

class ServerActor extends Actor {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("10.250.27.30", 6666))

  def receive = {
    case b @ Bound(localAddress) =>
      println("TCP manager bound the to our IP")
    // do some logging or setup ...

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      println("user connected " + c.toString)
      val connection = sender()
      val handler = context.actorOf(Props(new SimpleEchoHandler(sender, remote)))
      connection ! Register(handler)

  }

}


