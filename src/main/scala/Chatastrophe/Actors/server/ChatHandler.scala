/***
  * Handler can send messages to server
  * but server cannot send messages to handler...
  */


package Chatastrophe.Actors.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.util.ByteString



class ChatHandler(connection: ActorRef, remote: InetSocketAddress, server: ActorRef)
  extends Actor with ActorLogging {

  import Tcp._
  import ChatServer.connections

  // sign death pact: this actor terminates when connection breaks
  context watch connection

  case object Ack extends Event

  connections += remote -> connection

  def receive = {
    case Received(data) =>                          // If the client's handler receives a message from the client
      //println("Handler received " + data.decodeString("UTF-8"))
      buffer(data)                                  // Buffer the message, in the handler
      //connection ! Write(data, Ack)               // To the client, send this
      connections.values.foreach( connection => connection ! Write(data, Ack) )

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
