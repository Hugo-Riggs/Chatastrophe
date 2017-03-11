package Chatastrophe.Actors.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps

case object Shutdown

object ChatServer {

  val config = ConfigFactory.load()
  val system = ActorSystem("Client", config.getConfig("serverApp"))

  //val system = ActorSystem("ChatastropheServer")
  val actor = system.actorOf(Props[ChatServer])
  val connections = collection.mutable.Map[InetSocketAddress, ActorRef]()



  // helpful function from stack overflow
  def getIpAddress: String = {
    import collection.JavaConverters._
    import java.net._
    val enumeration = NetworkInterface.getNetworkInterfaces.asScala.toSeq

    val ipAddresses = enumeration.flatMap(p =>
      p.getInetAddresses.asScala.toSeq
    )

    val address = ipAddresses.find { address =>
      val host = address.getHostAddress
      host.contains(".") && !address.isLoopbackAddress
    }.getOrElse(InetAddress.getLocalHost)

    address.getHostAddress
  }
}


class ChatServer extends Actor with ActorLogging {

  import Tcp._
  import context.system
  import ChatServer.connections   // Import this record, so we can add and subtract new connections.

  private var socketActor: Option[ActorRef] = None

  override def preStart() = {
    log.debug("Starting Chatastrophe Server...")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  private val server = self
  //  private val ip = java.net.InetAddress.getLocalHost
  private val ip  = ChatServer.getIpAddress
  private val port = 6666
  IO(Tcp) ! Bind(server, new InetSocketAddress(ip, port))

  def receive = {
    case b @ Bound(localAddress) =>  // setup and log
      socketActor = Some(sender)
      log.info("TCP manager bound to our IP="+ip+" with port="+port)

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val connection = sender()
        log.info(remote + " remote connection.")
        val handler = context.actorOf(
          Props( new ChatHandlerWithConnections(connection, remote) ) )
        connections += remote -> connection // add connection to the connections
        connection ! Register(handler)

    case Shutdown =>
      socketActor.foreach(_ ! Unbind)
      ChatServer.connections.clear()
      context.stop(self)

    case x => log.warning("Received unknown message: {}", x)
  }
}
