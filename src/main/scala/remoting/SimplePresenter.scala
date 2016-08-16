
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

object Mediator {
  def props: Props = Props(new Mediator)
}

class Mediator extends Actor {
  import context._
  var client = List.empty[ActorRef]
  var gui = List.empty[ActorRef]

///////////////////////////////////////

  def Unlocked: Receive = {
    case UnlockMediator => println("MEDIATOR ALREADY UNLOCKED")
    case LockMediator => become(Locked)
    case _ => println("UNKOWN MESSAGE IN MEDIATOR UNLOCKED STATE")
  }

  def Locked: Receive = {
    case LockMediator => println("MEDIATOR ALREADY LOCKED")
    case Unlock(msg) => gui(0) ! ReceiveMessage(msg); become(Unlocked)
    case Waiting => client(0) ! Waiting
    case _ => println("UNKOWN MESSAGE IN MEDIATOR LOCKED STATE")
  }

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
  implicit val timeout = Timeout(5 seconds)

 val mediator = sys.actorOf(Mediator.props, name="mediatorActor")

  val i = Inbox.create(sys)
  //i watch clientActor
  i watch mediator
  i send(mediator, PassClientActor(clientActor))
  i send(mediator, PassGUIsysActr(i.getRef))
  i send(clientActor, PassMediator(mediator))
  //i send(clientActor, PassMediator(mediator))
    i send(mediator, LockMediator)

  def fn: Unit = {
    i send(mediator, LockMediator)
    //val f = mediator ? Waiting // causes progarm to block
    mediator ! Waiting
    val r: Future[ReceiveMessage] = Future(i.receive(10 seconds).asInstanceOf[ReceiveMessage])
    //val r = Await.result(future, timeout.duration).asInstanceOf[String]
    val p = Promise[ReceiveMessage]()
    p completeWith r
    p.future onSuccess {
      case x => println(x); fn; msgArea.text = x.text
    }
    /*
    r match {
      case ReceiveMessage(text) =>  msgArea.text = text
      case _ => println("GUI RECEIVING : " + r)
    }*/
  }

  fn

  def RecFunc = {

    def loop: Unit = {
      println("INNER LOOP")
      val f = i.receive(5 seconds)
      f match {
        case ReceiveMessage(text) => { msgArea.text = text; i send(clientActor, "keepAlive"); loop }
        case "OK" => { msgArea.text = "We Connected"; i send(clientActor, "keepAlive"); loop }
        case _ => msgArea.text = "ERROR: unhandled message"
      }
    }
    loop
  }

  //RecFunc

  def onSend(event: ActionEvent) {
    i send(clientActor, SendMessage(ourMessage.text.value))
    //clientActor ! SendMessage(ourMessage.text.value)
    ourMessage.text = ""
  }


/*
  def RecUpdateMsgs = {
    implicit val ec = ExecutionContext.global
    implicit val timeout = Timeout(5 seconds)

    def loop(f: Future[String]): Unit ={
      val p = Promise[String]()
      p completeWith f
      p.future onSuccess { case s =>
        msgArea.text = s
  //      loop((clientActor ? GUI_Request).mapTo[String])
      }
    }

    loop((clientActor ? GUI_Request).mapTo[String])
  }

  //RecUpdateMsgs // Users far too much CPU
*/


}