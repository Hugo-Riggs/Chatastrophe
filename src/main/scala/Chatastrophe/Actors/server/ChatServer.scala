package Chatastrophe.Actors.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}

case object Shutdown

object ChatServer {
  val system = ActorSystem("ChatastropheServer")
  val actor = system.actorOf(Props[ChatServer])
  val connections = collection.mutable.Map[InetSocketAddress, ActorRef]()
}


class ChatServer extends Actor {

  import Tcp._
  import context.system

  private val server = self
  private val ip = java.net.InetAddress.getLocalHost
  private val port = 6666
  IO(Tcp) ! Bind(self, new InetSocketAddress(ip, port))

  def receive = {
    case b @ Bound(localAddress) => // do some logging or setup ...
      println("TCP manager bound to our IP="+ip+" with port="+port)

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val connection = sender()
      println(remote + " connected") // @LogIt
      println("loading " + server + " reference into handler")
      val handler = context.actorOf(Props(new ChatHandler(connection, remote, server)), name = "chatHandler")
      connection ! Register(handler)

    case Shutdown =>
      ChatServer.connections.clear()
      IO(Tcp) ! Unbind
      context.stop(self)

    case a: Any =>
      println("Server does not understand. " + a.toString)
  }

}
