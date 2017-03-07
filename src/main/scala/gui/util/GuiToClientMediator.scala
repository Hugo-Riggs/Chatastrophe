package gui.util

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.util.ByteString


/***
* The mediator actor passes messages between
* the underlying client and the GUI. It makes
* use of a Locked and Unlocked set of states.
* All blocking occurs on this actor which
* allows un-interrupted use of the GUI and client.
* Hugo Riggs
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

  import GuiToClientMediator._
  import context._


  // Use of akka's `become` with different `Receive` states, enable a FSM
  def Unlocked: Receive = {
    case command: MediatorCommand => command match {
      case LockMediator => become(Locked)
      case UnlockMediator => println("Mediator already unlocked.")
      case Waiting => println("Mediator was unlocked when it received Waiting.")
      case Message(msg) => println("Mediator did not receive message in locked state: " + msg)
    }
    case "Kill" =>
      System.console.printf("Mediator received kill signal while unlocked" )
      //System.exit(1) TODO these system exit's got commented out see if this works.
      gui.head ! PoisonPill // Kill GUI's actor
      self ! PoisonPill
    case _ => println("Unknown message in mediator unlocked state")
  }

  def Locked: Receive = {
    case command: MediatorCommand => command match {
      case LockMediator => println("Mediator already locked")
      case UnlockMediator => become(Unlocked)
      case Message(msg) => gui.head ! ByteString(msg); self ! UnlockMediator
      case Waiting => client.head ! Waiting
    }
    case "Kill" =>
      println("Mediator received kill signal while locked" )
      //System.exit(1) TODO see prev
      gui.head ! PoisonPill // Kill GUI's actor
      self ! PoisonPill
    case x: Any => println("Unkown message in mediator locked state " + x)
  }
  // end of simple two state machine

  def receive: Receive = {
    case PassClientActor(c) =>  client = List(c) //client = List(c)  c :: client
    case PassGUIsysActr(g) => gui = List(g)  // g :: gui
    case LockMediator => become(Locked)
    case _ => println("Unkown message in mediator")
  }
}
