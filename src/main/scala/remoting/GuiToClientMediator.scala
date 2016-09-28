package remoting

import akka.actor.{Actor, ActorRef, PoisonPill, Props}


/***
* The mediator actor passes messages between
* the underlying client and the GUI. It makes
* use of a Locked and Unlocked set of states.
* All blocking occurs on this actor which
* allows un-interrupted use of the GUI and client.
*/

object GuiToClientMediator {
  
  var client = List.empty[ActorRef]
  var gui = List.empty[ActorRef]

  case class PassMediator(mediator: ActorRef)
  case class PassGUIsysActr(g: ActorRef)
  case class PassClientActor(c: ActorRef)
  sealed abstract class MediatorCommand
    case class Message(msg: String) extends MediatorCommand
    case object LockMediator extends MediatorCommand
    case object UnlockMediator extends MediatorCommand
    case object Waiting extends MediatorCommand

  def props: Props = Props(new GuiToClientMediator)
}

class GuiToClientMediator extends Actor {

  import CommunicationProtocol.ReceiveMessage
  import GuiToClientMediator._
  import context._

  // Become with Receive states, enable FSM
  def Unlocked: Receive = {
    case command: MediatorCommand => command match {
      case LockMediator => become(Locked)
      case UnlockMediator => println("MEDIATOR ALREADY UNLOCKED")
      case Waiting => println("MEDIATOR WAS UNLOCKED WHEN IT RECEIVED Waiting")
      case Message(msg) => println("MEDIATOR DID NOT RECEIVE MESSAGE IN LOCKED STATE: " + msg)
    }
    case "Kill" =>
      System.console.printf("MEDIATOR RECEIVED KILL SIGNAL WHILE UNLOCKED" )
      System.exit(1)
      gui(0) ! PoisonPill // Kill GUI's actor
      self ! PoisonPill
    case _ => println("UNKOWN MESSAGE IN MEDIATOR UNLOCKED STATE")
  }

  def Locked: Receive = {
    case command: MediatorCommand => command match {
      case LockMediator => println("MEDIATOR ALREADY LOCKED")
      case UnlockMediator => become(Unlocked)
      case Message(msg) => gui(0) ! ReceiveMessage(msg); self ! UnlockMediator
      case Waiting => client(0) ! Waiting
    }
    case "Kill" =>
      println("MEDIATOR RECEIVED KILL SIGNAL WHILE LOCKED" )
      System.exit(1)
      gui(0) ! PoisonPill // Kill GUI's actor
      self ! PoisonPill
    case x: Any => println("UNKOWN MESSAGE IN MEDIATOR LOCKED STATE " + x)
  }
  // end of simple two state machine

  def receive: Receive = {
    case PassClientActor(c) => client = List(c)
    case PassGUIsysActr(g) => gui = List(g)
    case LockMediator => become(Locked)
    case _ => println("UNKOWN MESSAGE IN MEDIATOR")
  }
}
