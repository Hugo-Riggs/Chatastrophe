/***
  * This class handles client response,
  * it is attached to the client
  * as a listener actor, to which the lower
  * level connection api communicates requests
  * and responses.
  */

package Chatastrophe.Actors.client

import akka.actor.Actor
import akka.io.Tcp._


class ClientListener extends Actor {

  def receive = {
    case data: akka.util.ByteString =>  // A chat response from server
      println(data.decodeString("UTF-8"))
    case Received(data) =>              // Not sure when or if this ever gets called...
      sender() ! Write(data)
    case PeerClosed     => context stop self
    case strResp: String => println(strResp)
  }

}
