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
  var ourName = ""
  var logReceived = ""

  def receive = {
    case Join(address, withName) if server.isEmpty =>
      server = List(context.actorSelection("akka.tcp://ChatastropheRemoteActorSys@"+address+"/user/remoteActor"))
      ourName=withName
      server(0) ! UserConnected(withName, self)

    case SendMessage(text) =>
      if (server.isEmpty) {println("first join the server")}                  // message algorithm
      else { println("step 0: send message to server");server(0) ! ReceiveMessage(ourName+": "+text) ; self ! Poll}     // Start(a0)(0): by sending message to server

    case ReceiveMessage(text) =>                                              // (a0)(4): A client receives latest log
      println("step 4: localActor received: " + text)
      logReceived = text; println("step 5: log update on client " + logReceived) // (a0)(5): update client's log to latest log

    case Disconnect(name) =>
      server(0) ! Disconnect(name)
      server = List.empty[ActorSelection]
      self ! Kill

    case Poll =>
      server(0) ! Poll

    //case Request =>
    case GUImessage(sendMessage) =>
      // (a0)(6) allow GUI to ask for client's text log

      implicit val ec = ExecutionContext.global
      implicit val timeout = Timeout(5 seconds)

      self ! sendMessage
//      println("localA bang sendMessage")
      sender ! logReceived              // this returns our log right after sending message, without waiting for our log to be updated
 //     println("GUI bang logReceived")

    case Request =>
      sender ! logReceived

  }


}