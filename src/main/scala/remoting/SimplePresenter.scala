
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
case class GUImessage(sendMessage: SendMessage)
case object Request


@sfxml
class SimplePresenter (
                            private val ourMessage: TextField,
                            private val msgArea: TextArea,
                            private val actor: PassClientActor,
                            private val clientActor: ActorRef
                     ) {

  import scala.concurrent.duration._
  import akka.util.Timeout
  import akka.pattern.ask
  import akka.actor._
  import scala.concurrent.{Await, ExecutionContext, Future, Promise}

  var switchBool: Boolean = false
  implicit val ec = ExecutionContext.global
  implicit val timeout = Timeout(5 seconds)

  def recReadLog(msgs: String): Unit = {
   def process(msgs: String, acc: String = msgs): Unit ={
     val test = msgs.eq(acc)
     test match {
      case false  => msgArea.text = msgs
      case  true => {
        val f = (clientActor ? Request).mapTo[String]
        val p = Promise[String]()
        p completeWith f
        p.future onSuccess  {
          case s => process(s, acc)
        }
      }
    }
  }
   process(msgs)
  }

  def onSend(event: ActionEvent) {                    // message algorithm
    // Start(0): by sending message to server
    val f = (clientActor ? GUImessage(SendMessage(ourMessage.text.value))).mapTo[String]
    val p = Promise[String]()

    p completeWith f

    p.future onSuccess {
      case s => {println("gui got its future"); recReadLog(s) } //(6): update clients text log : END
    }

    ourMessage.text = ""
  }


}