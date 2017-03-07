package Chatastrophe

import akka.actor._
import scala.language.postfixOps


// A main class to load the client side.
object ClientWithCLI extends App {
  require(args.length == 2, "usage: ClientWithCLI userName address:port\n two arguments required")

  val userName = args(0)
  val addressPort = args(1)

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

  // Start the client
  import RequestReply.Client
  //val client = system.actorOf(Client.props(userName, addressPort), name="clientActor")

  // Connect
  //import RequestReply.CommunicationProtocol.{Disconnect, SendMessage}

  //def exit() = client ! Disconnect(userName)

  //def sendMessage(s : String) = client ! SendMessage( userName + ": " + s )

  def startLoopedInputMode() = {
    for (ln <- io.Source.stdin.getLines){
      //if (ln == "exit") exit()
      //else sendMessage(ln)
    }
  }
  startLoopedInputMode()
}
