package interface

import akka.actor._
import scala.language.postfixOps
import scala.sys.process._


// A main class to load the client side.
object ClientWithCLI extends App {
  require(args.length == 2, "usage: ClientWithCLI userName address:port\n two arguments required")

  val userName = args(0)
  val addressPort = args(1) 
  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

  // Start the client
  import remoting.LocalA
  val localActor = system.actorOf(LocalA.props, name="localActr")

  // Connect
  try{
    localActor ! remoting.CommunicationProtocol.Connect(addressPort, userName, localActor)

    import remoting.CommunicationProtocol.{Disconnect, SendMessage}

    def systemCommand(s: String*): String = s.!!


    def exit = localActor ! Disconnect(userName)


    def sendMessage(s : String) = localActor ! SendMessage( userName + ": " + s )


    def startLoopedInputMode = {
      systemCommand("clear")

      for (ln <- io.Source.stdin.getLines){
        if (ln == "exit") exit
        else sendMessage(ln)
      }
    }

    startLoopedInputMode

  } catch { // failed to connect
    case ex: Exception => "error " + ex
  }


}
