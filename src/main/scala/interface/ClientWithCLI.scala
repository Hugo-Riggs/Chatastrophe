package interface


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
  import remoting.LocalA
  val localActor = system.actorOf(LocalA.props, name="localActr")

  // Connect
  localActor ! remoting.CommunicationProtocol.Connect(addressPort, userName, localActor)                                        

  // Communication loop
  import remoting.CommunicationProtocol.{Disconnect, SendMessage}
  def getInput(line: String): Unit = {
    if(line == "exit")
      localActor ! Disconnect(userName) 
    else{
      localActor ! SendMessage(line)
      getInput(readLine)
    }
  }

  getInput(readLine)
}
