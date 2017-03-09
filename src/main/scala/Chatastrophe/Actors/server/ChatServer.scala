package Chatastrophe.Actors.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}

case object Shutdown

object ChatServer {
  val system = ActorSystem("ChatastropheServer")
  val actor = system.actorOf(Props[ChatServer])
  val connections = collection.mutable.Map[InetSocketAddress, ActorRef]()
}


class ChatServer extends Actor with ActorLogging {

  import Tcp._
  import context.system
  import ChatServer.connections

  private var socketActor: Option[ActorRef] = None

  override def preStart() = {
    log.debug("Starting Chatastrophe Server...")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  private val server = self
  private val ip = java.net.InetAddress.getLocalHost
  private val port = 6666
  IO(Tcp) ! Bind(server, new InetSocketAddress(ip, port))

  def receive = {
    case b @ Bound(localAddress) =>  // setup and log
      socketActor = Some(sender)
      log.info("TCP manager bound to our IP="+ip+" with port="+port)

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val connection = sender()
      log.info(remote + " remote connection\nloading " + server + " reference into their handler")
      // Maybe substitute `server` for `connections` and would connections update
      // in the handler if it is updated by the server actor?
      // val handler = context.actorOf(Props(new ChatHandler(connection, remote, server)), name = "chatHandler")
      val handler = context.actorOf(
        Props(
          new ChatHandlerWithConnections(connection, remote)
        ) , name = "chatHandlerWithConnections")
      //  connections += remote -> handler // add handler to the connections
      connections += remote -> connection // add connection to the connections
      connection ! Register(handler)

    case Shutdown =>
      socketActor.foreach(_ ! Unbind)
      IO(Tcp) ! Unbind
      ChatServer.connections.clear()
      context.stop(self)

    case x => log.warning("Received unkown message: {}", x)
  }
}
