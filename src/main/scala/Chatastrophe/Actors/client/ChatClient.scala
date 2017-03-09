package Chatastrophe.Actors.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString


object ChatClient {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[ChatClient], remote, replies)

  private val system = ActorSystem("Client")
  private val listener = system.actorOf(Props[ClientListener], "listener")

  def createClientConnection(chatSockAdd: InetSocketAddress):ActorRef =
    system.actorOf(props(chatSockAdd, listener))
}


class ChatClient(chatSockAdd: InetSocketAddress, listener: ActorRef)
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
      println("registering our actor with server")
      connection ! Register(self)			              // register with the server, our actor
      context become {		                          // Behaviour after connection	established:
        case data: ByteString =>                    // An outgoing message from the client
          connection ! Write(data)
        case CommandFailed(w: Write) =>             // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>                      // Message from the server
          listener ! data                           // is passed to the listener for handling (print out, Chatastrophe.gui display)
        case "close" =>
          println("client closed connection")
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}

