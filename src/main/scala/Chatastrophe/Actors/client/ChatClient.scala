package Chatastrophe.Actors.client

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.typesafe.config._

object ChatClient {
  val config = ConfigFactory.load()
  val system = ActorSystem("Client", config.getConfig("clientApp"))

  private def listener(username: String) =
    system.actorOf(Props(classOf[ClientListener], username), "listener")

  private def props(username: ByteString, remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[ChatClient],username,  remote, replies)

  def createClientConnection(username: String, chatSockAdd: InetSocketAddress):ActorRef =
    system.actorOf(props(ByteString(username), chatSockAdd, listener(username)))
}

class ChatClient(
        username: ByteString,
        chatSockAdd: InetSocketAddress,
        listener: ActorRef)
  extends Actor   {

  import akka.io.Tcp._
  import context.system
  import Chatastrophe.Protocol._

  IO(Tcp) ! Connect(chatSockAdd)

  val spacer = ByteString(": ")

  def receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>		        // on connecting to server
      listener ! c					                        // inform client's listener
      val connection = sender()			             	  // connection to server (sender is server's response)
      connection ! Register(self)			              // register with the server, our actor
      connection ! Write(username)                  // register username with server
      context become {		                          // Behaviour after connection	established:
        case data: ByteString =>                    // An outgoing message from the client
          connection ! Write(data)
          listener ! PlaceName

        case CommandFailed(w: Write) =>
          listener ! "write failed"

        case Received(data: ByteString) =>          // Message from the server is passed to the listener for handling
          listener ! data                           //  (print out OR Chatastrophe.gui display)

        case "close" =>
          connection ! Write(ByteString(" disconnected."))
          connection ! Close

        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}

