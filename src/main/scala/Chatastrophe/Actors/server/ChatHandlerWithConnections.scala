/***
  * Handler can send messages to server
  * but server cannot send messages to handler...
  */


package Chatastrophe.Actors.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.util.ByteString


class ChatHandlerWithConnections(
    // Sender's actor reference
    connection: ActorRef,
    // Address from which the connection came in.
    remote: InetSocketAddress
  ) extends Actor with ActorLogging {

  import ChatServer.connections
  import Tcp._

  // sign death pact: this actor terminates when connection breaks
  context watch connection

  case object Ack extends Event

  def receive = {
    case Received(data) =>                          // If the client's handler receives a message from the client
      buffer(data)                                  // Buffer the message, in the handler
      connections.values.foreach(
        c => if(this.connection != c)
          c ! Write(data, Ack) )

      context.become({
        case Received(data) => buffer(data)
        case Ack            => acknowledge()
        case PeerClosed     => closing = true
      }, discardOld = false)

    case PeerClosed => context stop self

  }

  // storage omitted ...
  private var suspended = false
  private var closing = false
  private var transferred = 0
  private val lowWatermark = 10
  private val highWatermark = 100
  private var stored = 0
  private val maxStored = 200
  private var storage = collection.mutable.ArrayBuffer[ByteString]()

  private def buffer(data: ByteString): Unit = {
    storage :+= data
    stored += data.size

    if (stored > maxStored) {
      log.warning(s"drop connection to [$remote] (buffer overrun)")
      context stop self

    } else if (stored > highWatermark) {
      log.debug(s"suspending reading")
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
      log.debug("resuming reading")
      connection ! ResumeReading
      suspended = false
    }
    if (storage.isEmpty) {
      if (closing) context stop self
      else context.unbecome()
    } else connection ! Write(storage(0), Ack)
  }

  private def broadcast(data: ByteString) = {

  }
}