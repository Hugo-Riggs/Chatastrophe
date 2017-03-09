/***
* A class for hosting the server for a time period.
* The server is an akka actor, it implements
* logging.
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
      val server: ActorRef = ChatServer.actor
      import concurrent.ExecutionContext.Implicits.global
      val fut = Future {
        (println("Server will shutdown in 30 seconds"),Thread.sleep(1000 * 30))
      }
      val x = Await.result(fut, 1.minute)
      fut.onComplete(U =>
        (server ! Shutdown,
          println("Server offline.."))
      )
    } catch {
      case e: Exception => println(e)
    }
  }

}
