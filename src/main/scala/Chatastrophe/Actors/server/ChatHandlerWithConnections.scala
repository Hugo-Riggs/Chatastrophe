package Chatastrophe.Actors.server

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.util.{ByteString}
import Chatastrophe.Protocol._

class ChatHandlerWithConnections(
    connection: ActorRef,      // Sender's actor reference
    remote: InetSocketAddress  // Address from which the connection came in.
  ) extends Actor with ActorLogging {

  import Tcp._

  context watch connection // sign death pact: this actor terminates when connection breaks

  case object Ack extends Event
  private val connections = collection.mutable.Map[ InetSocketAddress, ActorRef]()
  private var username = ByteString("???")
  private val server = context.parent

  def receive = {
    case Received(data: ByteString) =>  // If the client's handler receives a message from the client
      username = data
      server ! UserName(username, remote)
      context.become({
        case Received(data) => broadcast(data)
        context.become({
          case Received(data) => buffer(data)
          case Ack            => acknowledge()
          case PeerClosed     => closing = true
          case UpdatePeers(c)  => updatePeers(c)
        }, discardOld = false)
      case UpdatePeers(c)  => updatePeers(c)
      case PeerClosed => context stop self
      case RepeatedUsername =>
        connection ! Write(ByteString("user already exists in channel"))
        context stop self
    })
    case UpdatePeers(c)  => updatePeers(c)
    case PeerClosed => context stop self
    case RepeatedUsername =>
      connection ! Write(ByteString("user already exists in channel"))
      context stop self
  }

  private var suspended = false
  private var closing = false
  private var transferred = 0
  private val lowWatermark = 15
  private val highWatermark = 150
  private var stored = 0
  private val maxStored = 250
  private var storage = collection.mutable.ArrayBuffer[ByteString]()

  private def buffer(data: ByteString): Unit = {
    storage :+= data
    stored += data.size

    if (stored > maxStored) {
      log.warning(s"\ndrop connection to [$remote] (buffer overrun)")
      context stop self

    } else if (stored > highWatermark) {
      log.debug(s"\nsuspending reading")
      connection ! SuspendReading
      suspended = true
    }
  }

  private def acknowledge(): Unit = {
    require(storage.nonEmpty, "storage was empty")

    val size = storage(0).size
    stored -= size
    transferred += size

    storage = storage drop 1

    if (suspended && stored < lowWatermark) {
      log.debug("\nresuming reading")
      connection ! ResumeReading
      suspended = false
    }
    if (storage.isEmpty) {
      if (closing) context stop self
      else context.unbecome()
    } else broadcast(storage(0)) //connection ! Write(storage(0), Ack)
  }

  private def broadcast(data: ByteString) = {
      buffer(data)                                  // Buffer the message, in the handler
      val userMessage = ByteString(username.decodeString("UTF-8")+": "+data.decodeString("UTF-8"))
      log.info(userMessage.decodeString("UTF-8"))
      connections.values.foreach(
        c => if(this.connection != c)
          c ! Write(userMessage, Ack) )
  }

  private def updatePeers(connections : collection.mutable.Map[InetSocketAddress, ActorRef] ) ={
          log.info("\nUpdating connections in "
            + username.decodeString("UTF-8") + "'s handler to:\n" + connections.mkString("\n"))
          this.connections.clear()
          this.connections++=connections
  }

}
