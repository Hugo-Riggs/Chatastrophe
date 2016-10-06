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
  localActor ! remoting.CommunicationProtocol.Connect(addressPort, userName, localActor)                                        
  
  // Start communication loop
  import remoting.CommunicationProtocol.{Disconnect, SendMessage}

 /* // Get the systems console
  val standardIn = System.console()

  def getInput(line: String): Unit = {
    if(line == "exit")
      localActor ! Disconnect(userName) 
    else{
      localActor ! SendMessage(line)
      val nextMessage: String = standardIn.readPassword() mkString;
      getInput(nextMessage)
    }
  }

  getInput("")*/


  def unixCommand(s: String*): String = s.!!

  unixCommand("clear")

  for (ln <- io.Source.stdin.getLines){
    if (ln == "exit") 
      localActor ! Disconnect(userName)
    else 
      localActor ! SendMessage( userName + ": " + ln )
  }
  

}
