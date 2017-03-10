import java.net.InetSocketAddress
import Chatastrophe.Actors.client.ChatClient
import akka.util.ByteString
import org.scalatest.FunSuite


class ClientTest extends FunSuite {

  test("connect to server and message server") {
    print("Enter ip: "); val ip = scala.io.StdIn.readLine()
    print("Enter our username: "); val username = scala.io.StdIn.readLine()
    val inetSocketAddress = new InetSocketAddress(ip, 6666)
    val client = ChatClient.createClientConnection(username, inetSocketAddress)
    client ! ByteString("Test Message")
  }

}
