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

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.Await


class ServerTest extends FunSuite {



  test("host server"){
    try{
      println("Starting server...")
      val serverReference: ActorRef = ChatServer.actor
      println("Server up for 30 seconds")

      import concurrent.ExecutionContext.Implicits.global
      val fut = Future {
        Thread.sleep(1000 * 30)
      }

      val x = Await.result(fut, 1.minute)

      fut.onComplete(U =>
        serverReference ! Shutdown
      )

      println("Server offline..")
    } catch {
      case e: Exception => println(e)
    }

  }

}
