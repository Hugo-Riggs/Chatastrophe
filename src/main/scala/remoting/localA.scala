package remoting
/*
* The Client
* when client actor receives a send message, it tells the server to receive that message.
* First a Join on the server must be preformed.
 */
import akka.actor._
import akka.stream.scaladsl._
import scala.concurrent.Promise


object log { // A log of communications
  var textLog: String = "";   var keepTextLog: Boolean = true
  def addToTextLog(text: String) = textLog += text;   def getTextLog: String = textLog
}

object localA {
  def props: Props = Props(new localA)
}

class localA extends Actor {

  var server = List.empty[ActorSelection]
  var gui = List.empty[ActorRef]

  def receive = {
    case Join(address, withName) if server.isEmpty => server = List(context.actorSelection("akka.tcp://ChatastropheRemoteActorSys@"+address+"/user/remoteActor")); server(0) ! UserConnected(withName, self)
    case SendMessage(text) => server(0) ! ReceiveMessage(text)
    case ReceiveMessage(text) => println("localActor received: " + text); if(log.keepTextLog) {log.addToTextLog(text)};
      if(!gui.isEmpty) {gui(0) ! ReceiveMessage(text)} //if(doGuiHook) {guiHook(text)}
    case Disconnect(name) => server(0) ! Disconnect(name) ; server = List.empty[ActorSelection]
    case InformClientOfGUI(clientGUIactr: ActorRef) => if (gui.isEmpty) {gui = List(clientGUIactr)} else {println("GUI is already set")}
  }

}