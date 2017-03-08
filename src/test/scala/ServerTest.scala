/***
* We need server client tests.
*/

/*
import org.scalatest.Matchers
import  org.scalatest.FlatSpec

class ChatastropheSpec extends FlatSpec with Matchers {

    "a Chatastrophe  server" should
      "create controls" in {
      val unit = Server.start
      Unit should be(unit)
    }

}
*/

package Chatastrophe.tests

import Chatastrophe.Actors.server.ChatServer
import akka.actor.ActorRef
import org.scalatest.FunSuite
import Chatastrophe.Actors.server._

class ServerTest extends FunSuite {

  println("Starting server...")
  private val serverReference: ActorRef  = ChatServer.actor

  test("stop server"){
    println("Hit enter to shutdown server.")
    scala.io.StdIn.readLine()
    serverReference ! Shutdown
  }

}
