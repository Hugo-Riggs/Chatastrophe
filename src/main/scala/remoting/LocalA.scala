package remoting

/***
 * Local actor (LocalA) an akka actor.
 * When LocalA receives a send message, it tells the remote actor (server) to receive that message.
 *
 * This class and companion object hold state data on the connection including an actor reference to
 * the server actor.
 *
 * @TODO: include communication protocol in receive matching
 * @TODO figure out this issue of non aligning text echoing
 * @TODO Consider removing user name from local actor as we will implement its use more in interface. 
 * 
 */

import akka.actor._
import scala.language.postfixOps


// The local actor facilitates communication with the server actor.
object LocalA {

  val remoteActorSysStr = "akka.tcp://ChatastropheRemoteActorSys@"
  val remoteActorRefStr = "/user/remoteActor"
  var server = List.empty[ActorSelection]
  var gui = List.empty[ActorRef]
  var guiM = List.empty[ActorRef]
  var ourName = ""
  var logReceived = ""
  var ourLastMessage = ""

  def prepString(s: String): String = (s.trim) + "\n"

  def props: Props = Props(new LocalA)
}


class LocalA extends Actor {

  import LocalA._
  import GuiToClientMediator.{PassMediator, PassGUIsysActr}
  import CommunicationProtocol._

  def receive = {
    case Connect(addressPort, withName, self) if server.isEmpty =>
      //ourName=withName
      server = 
        List(context.actorSelection(remoteActorSysStr+addressPort+remoteActorRefStr))
      server.head ! Connect(addressPort, withName, self)

    case SendMessage(s) =>
      if (server.nonEmpty){
        val s1 = prepString( s )
        ourLastMessage = s1 
        server.head ! ReceiveMessage( s1 )  // ${ourName}: 
      }
      else
        println("First join the server.")

    case ReceiveMessage( s2 ) =>
      if(s2 != ourLastMessage){
        val s3 = prepString( s2 )
        if(guiM.nonEmpty) 
          guiM.head ! GuiToClientMediator.Message( s3 ) 
        else 
          print( s3 ) // or send back to interface for printing
      }

    case Disconnect(name) =>
      if(server.nonEmpty){
        server.head ! Disconnect(name)
        server = List.empty[ActorSelection]
      }
      else
        println("Not currently connected to any server.")

      if (guiM nonEmpty) guiM.head ! "Kill"

      guiM = List.empty[ActorRef]
      self ! PoisonPill

    case Poll =>
      if( server nonEmpty ) server.head ! Poll else println("Server not set")

    case DeadLetter(msg, from, to) =>
      println("Received dead letter LocalA")

      // These cases could have some useful implementation purposes
      // However right now they are not very functional in this program.
    case GUI_Request =>
      sender ! logReceived

    case PassGUIsysActr(actorRef) =>
      gui = List(actorRef)
      sender ! "OK"

    case GuiToClientMediator.Waiting => println("Client received waiting")

    case PassMediator(m) => guiM = List(m)

    case "keepAlive" =>
      sender ! "OK"

    case "GUIissuesDisconnect" =>
      self ! Disconnect(ourName)  // As if client had requested disconnect
  }
}
