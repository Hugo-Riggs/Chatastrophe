
package remoting

import scalafx.scene.control.{ Button, ListView, TextField, TextArea }
import scalafx.beans.property.StringProperty
import scalafx.beans.binding.StringExpression
import scalafx.event.ActionEvent
import scalafxml.core.macros.sfxml
import akka.actor.{ ActorSystem, Props, Actor, Inbox, ActorRef }

case class PassMediator(mediator: ActorRef)
case class PassGUIsysActr(g: ActorRef)
case class PassClientActor(c: ActorRef)
case class Unlock(msg: String)
case object LockMediator
case object UnlockMediator
case object Waiting

/*
* The mediator actor passes messages between
* the underlying client and the GUI. It makes
* use of a Locked and Unlocked set of states.
* All blocking occurs on this actor which
* allows uninterupted use of the GUI and client.
*/

object Mediator {
  def props: Props = Props(new Mediator)
}

class Mediator extends Actor {
  import context._
  var client = List.empty[ActorRef]
  var gui = List.empty[ActorRef]

  // Become with Receive states, enable FSM
  def Unlocked: Receive = {
    case UnlockMediator => println("MEDIATOR ALREADY UNLOCKED")
    case LockMediator => become(Locked)
    case _ => println("UNKOWN MESSAGE IN MEDIATOR UNLOCKED STATE")
  }

  def Locked: Receive = {
    case LockMediator => println("MEDIATOR ALREADY LOCKED")
    case Unlock(msg) => gui(0) ! ReceiveMessage(msg); become(Unlocked)
    case Waiting => client(0) ! Waiting
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


@sfxml
class SimplePresenter (
                            private val ourMessage: TextField,
                            private val msgArea: TextArea,
                            private val sys: ActorSystem,
                            private val clientActor: ActorRef
                     ) {

  import akka.util.Timeout
  import akka.pattern.ask
  import scala.concurrent.duration._
  import scala.concurrent.{ Await, ExecutionContext, Future, Promise }

  implicit val ec = ExecutionContext.global
  implicit val timeout = Timeout(30 seconds)

 val mediator = sys.actorOf(Mediator.props, name="mediatorActor")

  val i = Inbox.create(sys)
  i watch mediator
  i send(mediator, PassClientActor(clientActor))
  i send(mediator, PassGUIsysActr(i.getRef))
  i send(clientActor, PassMediator(mediator))
  i send(mediator, LockMediator)

 // TODO: Handle the i.receive's timeout gracefully, currently timeout causes program to stop working, and
 // that is why it is set at 1 hour.
  // Recursively update messages in GUI
  def fn: Unit = {
    i send(mediator, LockMediator)
    mediator ! Waiting
    val r: Future[ReceiveMessage] = Future(i.receive(1 hour).asInstanceOf[ReceiveMessage])
    val p = Promise[ReceiveMessage]()
    p completeWith r
    p.future onSuccess {
      case x => println(x); fn; msgArea.text = msgArea.text.value + x.text //msgArea.text = x.text// for reading whole log
    }
  }

  fn

  def onSend(event: ActionEvent) {
    i send(clientActor, SendMessage(ourMessage.text.value))
    ourMessage.text = ""
  }

  // TODO: Add more buttons/GUI features in general (timestamps, connect interface . . .).

}