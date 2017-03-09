import java.net.InetSocketAddress
import Chatastrophe.Actors.client.ChatClient
import akka.util.ByteString
import org.scalatest.FunSuite


class ClientStayOnTest extends FunSuite {

  test("connect to server and message server, and allow continuous messaging") {
    println("Enter ip")
    val ip = scala.io.StdIn.readLine()
    val inetSocketAddress = new InetSocketAddress(ip, 6666)
    val client = ChatClient.createClientConnection(inetSocketAddress)

    def inputLines() = {
      def shouldContinue(str: String) = str!="quit"
      var str = ""
      while(shouldContinue(str)) {
        str = scala.io.StdIn.readLine()
        client ! ByteString(str)
      }
    }

    inputLines()
  }

}
