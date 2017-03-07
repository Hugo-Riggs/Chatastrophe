package RequestReply

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

object Client {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Client], remote, replies)

  private val system = ActorSystem("Client")

  private val simplisticHandler = system.actorOf(Props[SimplisticHandler], "handler")

  def createClientConnection(remote: InetSocketAddress):ActorRef =
    system.actorOf(props(remote, simplisticHandler))
}

class Client(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      println("connect failed")
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      println("connected " + c.toString)
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          println(data.decodeString("UTF-8"))
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          println("write failed")
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>
          println(data.decodeString("UTF-8"))
          listener ! data
        case "close" =>
          println("closing connection")
          connection ! Close
        case _: ConnectionClosed =>
          println("conection was closed")
          listener ! "connection closed"
          context stop self
      }
  }
}


