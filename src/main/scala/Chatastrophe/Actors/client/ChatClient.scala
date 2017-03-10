package Chatastrophe.Actors.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.typesafe.config._

object ChatClient {

  val config = ConfigFactory.load()
  val system = ActorSystem("Client", config.getConfig("clientApp"))


  private def listener(username: String) = system.actorOf(Props(classOf[ClientListener], username), "listener")

  // An abstract constructor function
  // Create the actor in the unique `Client` actor system.
  def createClientConnection(username: String, chatSockAdd: InetSocketAddress):ActorRef =
    system.actorOf(props(ByteString(username), chatSockAdd, listener(username)))

  // Less abstract constructor, can be used to create the client as a child
  // of another actor.
  def props(username: ByteString, remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[ChatClient],username,  remote, replies)
}


class ChatClient(
        username: ByteString,
        chatSockAdd: InetSocketAddress,
        listener: ActorRef)
  extends Actor   {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Connect(chatSockAdd)

  def receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>		        // on connecting to server
      listener ! c					                        // inform client's listener
      val connection = sender()			             	  // connection to server (sender is server's response)
      connection ! Register(self)			              // register with the server, our actor
      val spacer = ByteString(": ")
      context become {		                          // Behaviour after connection	established:
        case data: ByteString =>                    // An outgoing message from the client
          connection ! Write(username++spacer++data)
          listener ! PlaceName

        case CommandFailed(w: Write) =>             // O/S buffer was full
          listener ! "write failed"

        case Received(data) =>                      // Message from the server is passed to the listener for handling
          listener ! data                           //  (print out OR Chatastrophe.gui display)

        case "close" =>
          connection ! Write(username++ByteString(" disconnected."))
          connection ! Close

        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}

