package Chatastrophe.Actors.client.simpleInterface

import java.net.InetSocketAddress
import Chatastrophe.Actors.client.ChatClient
import akka.util.ByteString

/** Based on the test code
  */
object SimpleInterface extends App {

  print("Enter ip: "); val ip = scala.io.StdIn.readLine()
  print("Enter our username: "); val username = scala.io.StdIn.readLine()
  val inetSocketAddress = new InetSocketAddress(ip, 6666)
  val client = ChatClient.createClientConnection(username, inetSocketAddress)

  def inputLines() = {
    def shouldContinue(str: String) = str!="close"
    var str = ""
    while(shouldContinue(str)) {
      str = scala.io.StdIn.readLine()
      if(str == "close") {
        client ! "close"
      } else
        client ! ByteString(str)
    }
  }

  inputLines()

}
