/***
* A class for hosting the server for a time period.
* The server is an akka actor, it implements
* logging.
*/

import Chatastrophe.Actors.server.{ChatServer, _}
import akka.actor.ActorRef
import org.scalatest.FunSuite

class ServerStayOnTest extends FunSuite {

  test("host server"){
    try{
      val server: ActorRef = ChatServer.actor
      println("Hit enter to stop server.")
      val in = scala.io.StdIn.readLine()
      server ! Shutdown
    } catch {
      case e: Exception => println(e)
    }
  }

}
