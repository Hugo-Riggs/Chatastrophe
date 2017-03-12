package Chatastrophe.Actors.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.io.{IO, Tcp}
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps

case class UpdatePeers(connections: collection.mutable.Map[InetSocketAddress, ActorRef])
case object Shutdown

object ChatServer {
  val config = ConfigFactory.load()
  val system = ActorSystem("ChatastropheServer", config.getConfig("serverApp"))
  val actor = system.actorOf(Props[ChatServer])

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

  val handlers = collection.mutable.Map[InetSocketAddress, ActorRef]()
  val connections = collection.mutable.Map[InetSocketAddress, ActorRef]()
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
      // Standard stuff
      val connection = sender()
      log.info(remote + " remote connection.")
      val handler = context.actorOf(
        Props( new ChatHandlerWithConnections(connection, remote) ) )
      connection ! Register(handler)
      // Chatastrophe stuff
      context.watch(handler)
      connections += remote -> connection // add connection to the connections
      handlers += remote -> handler // add the handler for the server's future reference
      // Let other peers know of the new connection + load our peers
      val it = handlers.values.toIterator
      var counter = 0
      while(it.nonEmpty){
        counter += 1
        val next = it.next
        log.info("selecting actor at " + next.path)
        context.actorSelection(next.path) ! UpdatePeers(connections)
      }
      log.info("sent out " + counter + " updates for peers")

    case Shutdown =>
      socketActor.foreach(_ ! Unbind)
      connections.clear()
      context.stop(self)

    case Terminated(handler) =>
      log.info("Terminated " + handler + "... removing them from record")
      val it = handlers.toIterator
      while(it.hasNext) {
        val next = it.next
        if(next._2==handler){
          connections-=next._1
          handlers-=next._1
        }
      }

    case x => log.warning("Received unknown message: {}", x)
  }
}
