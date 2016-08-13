
package remoting

import scalafx.scene.control.{Button, ListView, TextField, TextArea}
import scalafx.beans.property.StringProperty
import scalafx.beans.binding.StringExpression
import scalafx.event.ActionEvent
import scalafxml.core.macros.sfxml
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Future

case class PassClientActor(val actor: ActorRef)


@sfxml
class SimplePresenter (
                            private val ourMessage: TextField,
                            private val msgArea: TextArea,
                            private val actor: PassClientActor,
                            private val clientActor: ActorRef
                     ) {

/*
  implicit val timeout = Timeout(5 seconds)
  val f: Future[ReceiveMessage] =
    for {
      text <- ask(localA, ReceiveMessage).mapTo[String]
    } yield ReceiveMessage(text)

  msgArea.text = f
  */
  import scala.concurrent.duration._
  import akka.util.Timeout
  import akka.pattern.ask
  import akka.actor._
  import scala.concurrent.{Await, ExecutionContext, Future, Promise}

  implicit val timeout = Timeout(5 seconds)
  val future = clientActor ? "hello"

  var switchBool: Boolean = false

  def onSend(event: ActionEvent) {
    clientActor ! SendMessage(ourMessage.text.value)

    implicit val ec = ExecutionContext.global
    implicit val timeout = Timeout(5 seconds)
    clientActor ! Poll
     val f = (clientActor ? ReceiveMessage).mapTo[String]
  //  val f = (clientActor ? Poll).mapTo[String]
    val p = Promise[String]()

    p completeWith f

    p.future onSuccess {
      case s => msgArea.text = msgArea.text.value + s
    }

    ourMessage.text = ""

    //println("future" + future)
    //println("sending message. . ." + ourMessage.text.value)
    //Messaging through GUI with actors
    //localActor ! ourMessage.text.value + "\n"
  }

  def onUpdateMessages(event: ActionEvent) {
    println("updating message area. . . ")
    msgArea.text = "TODO: receive message from remote actor here"
  }

}