
package remoting

import scalafx.scene.control.{ Button, ListView, TextField, TextArea }
import scalafx.beans.property.StringProperty
import scalafx.beans.binding.StringExpression
import scalafx.event.ActionEvent
import scalafxml.core.macros.sfxml
import akka.actor.{ ActorSystem, Inbox, ActorRef }

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

  val i = Inbox.create(sys)
  i watch clientActor
  i send(clientActor, PassGUIsysActr(i.getRef))

  def fn = {
    val f = i.receive(5 seconds)
    f match {
      case ReceiveMessage(text) =>  msgArea.text = text
      case _ => println("GUI RECEIVING : " + f)
    }
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