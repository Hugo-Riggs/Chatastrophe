package remoting

/*
* The Client
* when client actor receives a send message, it tells the server to receive that message.
* First a Join on the server must be preformed.
 */

import akka.actor._
import scala.language.postfixOps

object main extends App {
  remoteInit.init   // Start the server actor

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

  val localActor = system.actorOf(localA.props, name="localActr")                 // Start the client
  localActor ! Join("127.0.0.1:2552", "Junkrat")                                  // Connect

  val GUI = new GUIscalaFXinitializer(localActor, system)  // ScalaFX implementation with ScalaFXML
  GUI.main(Array(""))

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
      if (server.isEmpty) {println("first join the server")}
      else { server(0) ! ReceiveMessage(ourName+": "+text) }

    case ReceiveMessage(text) =>
      logReceived = text
      if(!gui.isEmpty) { println("CLIENT SENDING MESSAGE TO GUI " + text); gui(0) ! ReceiveMessage(text)}
      if(!guiM.isEmpty) { println("CLIENT SENDING MESSAGE TO MEDIATOR" + text); guiM(0) ! Unlock(text) }

    case Disconnect(name) =>
      server(0) ! Disconnect(name)
      server = List.empty[ActorSelection]
      self ! Kill

    case Poll =>
      server(0) ! Poll

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
  }


}