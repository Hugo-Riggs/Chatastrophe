package remoting

/*
* The Client
* when client actor receives a send message, it tells the server to receive that message.
* First a Join on the server must be preformed.
 */

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Promise
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


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

    case GUI_Request =>
      sender ! logReceived
      //server(0) forward GUI_Request

    case PassGUIsysActr(actorRef) =>
      gui = List(actorRef)
      sender ! "OK"

    case Waiting => println("CLIENT RECEIVED WAITING")

    case PassMediator(m) => guiM = List(m)

    case "keepAlive" =>
      sender ! "OK"
  }



}