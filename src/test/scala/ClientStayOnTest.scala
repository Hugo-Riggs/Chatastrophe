import java.net.InetSocketAddress
import Chatastrophe.Actors.client.ChatClient
import akka.util.ByteString
import org.scalatest.FunSuite


class ClientStayOnTest extends FunSuite {

  test("connect to server and message server, and allow continuous messaging") {
    print("Enter ip: "); val ip = scala.io.StdIn.readLine()
    print("Enter our username: "); val username = scala.io.StdIn.readLine()
    val inetSocketAddress = new InetSocketAddress(ip, 6666)
    val client = ChatClient.createClientConnection(username, inetSocketAddress)

    def inputLines() = {
      def shouldContinue(str: String) = str!="close"
      var str = ""
      while(shouldContinue(str)) {
        str = scala.io.StdIn.readLine()   // display username: message
        if(str == "close") {
          client ! "close"
        } else
          client ! ByteString(str)                  // send    username: message
      }
    }

    inputLines()
  }

}
