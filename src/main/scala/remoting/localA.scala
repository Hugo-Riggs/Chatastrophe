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

/*
object log { // A log of communications
  var textLog: String = "";   var keepTextLog: Boolean = true
  def addToTextLog(text: String) = textLog += text;   def getTextLog: String = textLog
}*/

object localA {
  def props: Props = Props(new localA)
}

class localA extends Actor {

  var server = List.empty[ActorSelection]
  var gui = List.empty[ActorRef]
  var ourName = ""

  def receive = {
    case Join(address, withName) if server.isEmpty =>
      server = List(context.actorSelection("akka.tcp://ChatastropheRemoteActorSys@"+address+"/user/remoteActor"))
      ourName=withName
      server(0) ! UserConnected(withName, self)

    case SendMessage(text) =>
      if (server.isEmpty) {println("first join the server")}
      else {server(0) ! ReceiveMessage(ourName+": "+text)}


    case ReceiveMessage(text) =>
    println("localActor received: " + text)
    text

      //if(log.keepTextLog) {log.addToTextLog(text)}
      //if(!gui.isEmpty) {println("localActor sending message to GUIactor:")
        //gui(0) ! ReceiveMessage(text)}

    case Disconnect(name) =>
      server(0) ! Disconnect(name)
      server = List.empty[ActorSelection]
      self ! Kill


    case Poll =>
    server(0) ! Poll

      /*
    import scala.concurrent.{Await, ExecutionContext, Future, Promise}
    implicit val ec = ExecutionContext.global
    implicit val timeout = Timeout(5 seconds)
    val f = (clientActor ? Poll).mapTo[String]
    val p = Promise[String]()

    p completeWith f

    p.future onSuccess {
      case s => msgArea.text = msgArea.text.value + s
    }*/


    case "hello" =>
      sender ! "ask returns hello"

  }



}