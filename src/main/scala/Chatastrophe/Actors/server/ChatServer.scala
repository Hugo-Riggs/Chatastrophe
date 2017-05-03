package Chatastrophe.Actors.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.io.{IO, Tcp}
import akka.pattern.ask
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps
import Chatastrophe.Protocol._
import akka.util.Timeout

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._


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
  val connections = collection.mutable.Map[ InetSocketAddress, ActorRef]()
  val usernames = collection.mutable.Map[ akka.util.ByteString, InetSocketAddress]()
  private var socketActor: Option[ActorRef] = None
  private val server = self
  private val ip  = ChatServer.getIpAddress
  private val port = 6666

  IO(Tcp) ! Bind(server, new InetSocketAddress(ip, port))

  override def preStart() = {
    log.debug("Starting Chatastrophe Server...")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  def receive = {
    case b @ Bound(localAddress) =>  // setup and log
      socketActor = Some(sender)  // server's socket internals actor
      log.info("clients can join now")

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val connection = sender()
      log.info("Remote connection from "+remote )
      val handler = context.actorOf(
        Props( new ChatHandlerWithConnections(connection, remote) ) )
      connection ! Register(handler)

      context.watch(handler) // for death handling
      connections += remote -> connection // map to connection
      handlers += remote -> handler // map to its handler

      // Let other peers know of the new connection (by interaction with their handlers)
      val it = handlers.values.toIterator
      var counter = 0
      while(it.nonEmpty){
        counter += 1
        val next = it.next
        log.info("sending new peer information to handler " + next.path)
        context.actorSelection(next.path) ! UpdatePeers(connections)
      }
      log.info("sent out " + counter + " updates for peers")

    case Shutdown =>
      log.info("Server shutting down...")
      socketActor.foreach(_ ! Unbind)
      connections.clear()
      context.stop(self)
      System.exit(1)

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

    case UserName(username, remote) =>
      if(!usernames.contains(username)) usernames += username -> remote
      else sender() ! RepeatedUsername

    case x => log.warning("Received unknown message: {}", x)
  }
}
