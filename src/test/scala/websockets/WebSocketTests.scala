package websockets

import org.scalatest.FunSuite

class SetSuite extends FunSuite {

  // Start The server
  Server("start"); println("Server started. . .")

  import akka.actor.ActorSystem
  implicit val actorSystem = ActorSystem("akka-system")
  val client = Client("Junkrat", "localhost", 8080)

  // Stop server after "quit" enter
  var in = ""; do{ in=Console.readLine() }while(in != "quit")
  Server("stop"); println("Server stopped. . . ")

}
