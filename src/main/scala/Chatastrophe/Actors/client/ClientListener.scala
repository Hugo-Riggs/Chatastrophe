/***
  * This class handles client response,
  * it is attached to the client
  * as a listener actor, to which the lower
  * level connection api communicates requests
  * and responses.
  *
  * Designed as an interface for bash..
  * terminal, cmd, any text based console.
  */

package Chatastrophe.Actors.client

import akka.actor.Actor
import akka.io.Tcp._
import akka.util.ByteString

case object RemoveName
case object PlaceName


class ClientListener(username: String) extends Actor {

  // Terminal based communication, text initialization
  private val nameLength = (username+": ").length
  private val backspace = "\b"*nameLength
  private val  verticalWhiteSpace = "\n"*20
  private val banner = "< Chatastrophe >"
  print(verticalWhiteSpace+banner+verticalWhiteSpace)
  print("to disconnect type: close\n\n")
  print(username+": ")

  def receive = {
    case data: ByteString =>                    // A chat response from server (dif. connected user)
      print(backspace)  //  self ! RemoveName   // I guess, it doesn't call `RemoveName` but `PlaceNames` does work...
      println(data.decodeString("UTF-8"))       // print message from remote
      self ! PlaceName

    case Received(data: ByteString) =>              // Not sure when or if this ever gets called...
      sender() ! Write(data)

    case PeerClosed     => context stop self

    case strResp: String => println(strResp)

    case RemoveName => print(backspace)

    case PlaceName => print(username+": ")
  }

}
