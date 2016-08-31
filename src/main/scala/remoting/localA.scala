package remoting

/*
* The Client
* when client actor receives a send message, it tells the server to receive that message.
* First a Join on the server must be preformed.
 */

import akka.actor._
import scala.language.postfixOps

object client extends App {
  //remoteInit.init   // Start the server actor

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

  val localActor = system.actorOf(localA.props, name="localActr")                 // Start the client
  //localActor ! Join("127.0.0.1:2552", "Junkrat")                                  // Connect

  //val GUI = new GUIscalaFXinitializer(localActor, system)  // ScalaFX implementation with ScalaFXML
  //GUI.main(Array(""))
}

object localA {
  def props: Props = Props(new localA)
}


class localA extends Actor {
  var server = List.empty[ActorSelection]
  var gui = List.empty[ActorRef]
  var guiM = List.empty[ActorRef]
  var ourName = ""
  var logReceived = ""

  def receive = {
    case Join(address, withName) if server.isEmpty =>
      server = List(context.actorSelection("akka.tcp://ChatastropheRemoteActorSys@"+address+"/user/remoteActor"))
      ourName=withName
      server(0) ! UserConnected(withName, self)

    case SendMessage(text) =>
      if (!server.isEmpty) server(0) ! ReceiveMessage(ourName+": "+text+"\n") else println("first join the server")

    case ReceiveMessage(text) =>
      logReceived = text
      if(!gui.isEmpty) gui(0) ! ReceiveMessage(text) else println("GUI IS NOT SET")
      if(!guiM.isEmpty) guiM(0) ! Unlock(text) else println("GUI MEDIATOR IS NOT SET")

    case Disconnect(name) =>
      if(!server.isEmpty) server(0) ! Disconnect(name) else println("SERVER NOT SET")
      server = List.empty[ActorSelection]
      if(!guiM.isEmpty) guiM(0) ! "Kill" else println("GUI MEDIATOR IS NOT SET")
      self ! PoisonPill

    case Poll =>
      if(!server.isEmpty) server(0) ! Poll else println("SERVER NOT SET")


      // These casses could have some useful implementation purposes
      // However right now they are not very functional in this program.
    case GUI_Request =>
      sender ! logReceived

    case PassGUIsysActr(actorRef) =>
      gui = List(actorRef)
      sender ! "OK"

    case Waiting => println("CLIENT RECEIVED WAITING")

    case PassMediator(m) => guiM = List(m)

    case "keepAlive" =>
      sender ! "OK"

    case "GUIissuesDisconnect" =>
      self ! Disconnect(ourName)  // As if client had requested disconnect
  }
}