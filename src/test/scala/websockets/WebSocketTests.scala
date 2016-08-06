package websockets

import org.scalatest.FunSuite

class SetSuite extends FunSuite {

  import Server._ // as connection is declared as variable it is created on Server import

  // Start The server
  println("Starting server. . . ")
  serverFactory("start")
  println("Server started. . . ")
  println(s"Server online at http://localhost:8080/")

  val client = new Client
  client.connect("localhost", 8080)


  // Stop server after pressing enter
   Console.readLine()
   serverFactory("stop")
  println("Server stopped. . . ")
}
