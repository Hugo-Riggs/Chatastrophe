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

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 0))

  def receive = {
    case b @ Bound(localAddress) =>
      println("Bound the local address")
    // do some logging or setup ...

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender()
      connection ! Register(handler)

    case c @ Connect(remoteAddress, localAddress, options, timeout, pullMode) =>
      println("connect?")
  }

}


