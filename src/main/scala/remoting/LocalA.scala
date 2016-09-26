package remoting

/*
* The Client
* when client actor receives a send message, it tells the server to receive that message.
* First a Connect on the server must be preformed.
 */

import akka.actor._
import scala.language.postfixOps

// A main class to load the client side.
object Client extends App {
  require(args.length == 2, "Client address:port userName")

  val addressPort = args(0)
  val userName = args(1)

  remoteInit.init   // Start the server actor

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

  val localActor = system.actorOf(LocalA.props, name="localActr")                 // Start the client
  localActor ! Connect(addressPort, userName, localActor)                                        // Connect

  def getInput(line: String): Unit = {
    if(line == "quit")
      localActor ! Disconnect(userName) 
    else{
      localActor ! SendMessage(line)
      getInput(readLine)
    }
  }

  getInput(readLine)
}

// The local actor facilitates communication with the server.
object LocalA {

  val remoteActorSysStr = "akka.tcp://ChatastropheRemoteActorSys@"
  val remoteActorRefStr = "/user/remoteActor"
  var server = List.empty[ActorSelection]
  var gui = List.empty[ActorRef]
  var guiM = List.empty[ActorRef]
  var ourName = ""
  var logReceived = ""

  def props: Props = Props(new LocalA)
}


class LocalA extends Actor {

  import LocalA._
  import GuiToClientMediator.{PassMediator, PassGUIsysActr}

  def receive = {
    case Connect(addressPort, withName, self) if server.isEmpty =>
      ourName=withName
      server = 
        List(context.actorSelection(remoteActorSysStr+addressPort+remoteActorRefStr))
      server.head ! Connect(addressPort, withName, self)

    case SendMessage(text) =>
      if (server.nonEmpty)
        server.head ! ReceiveMessage(s"${text}\n")
      else
        println("First join the server.")

    case ReceiveMessage(text) =>
      println("received " + text)
      if(guiM.nonEmpty) 
        guiM.head ! GuiToClientMediator.Message(text) 
      else 
        println(text)

    case Disconnect(name) =>
      if(server.nonEmpty){
        server.head ! Disconnect(name)
        server = List.empty[ActorSelection]
      }
      else
        println("Not currently connected to any server.")

      if(guiM.nonEmpty) guiM.head ! "Kill"

      guiM = List.empty[ActorRef]
      self ! PoisonPill

    case Poll =>
      if(server.nonEmpty) server.head ! Poll else println("SERVER NOT SET")

    case DeadLetter(msg, from, to) =>
      println("RECEIVED DEAD LETTER LocalA")
      // These cases could have some useful implementation purposes
      // However right now they are not very functional in this program.
    case GUI_Request =>
      sender ! logReceived

    case PassGUIsysActr(actorRef) =>
      gui = List(actorRef)
      sender ! "OK"

    case GuiToClientMediator.Waiting => println("CLIENT RECEIVED WAITING")

    case PassMediator(m) => guiM = List(m)

    case "keepAlive" =>
      sender ! "OK"

    case "GUIissuesDisconnect" =>
      self ! Disconnect(ourName)  // As if client had requested disconnect
  }
}
