package gui

/*
* In this file, scalaFxml(https://github.com/vigoo/scalafxml) finds
* the @sfxml annotation and writes our display class via a macro.
* Most advantagous part of scalaFxml is the ability to use SceneBuilder to
* design a GUI.
*/

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{ActorRef, ActorSystem, Inbox}
import akka.util.{ByteString, Timeout}
import gui.util.GuiToClientMediator

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control.{TextArea, TextField}
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafxml.core.macros.sfxml

@sfxml
class RuntimeGUI(
                          private val ourMessage: TextArea,
                          private val msgArea: TextArea,
                          private val name: TextField,
                          private val ip: TextField,
                          private val port: TextField,
                          private val sys: ActorSystem,
                          private val clientActor: ActorRef
                     ) extends RequestReply.CommunicationProtocol {


  private var displayName = name.text.value
  implicit val ec = ExecutionContext.global
  implicit val timeout = Timeout(30 seconds)

  // Spawn a mediator in the same actor system as the localA (client).
  val mediator = sys.actorOf(GuiToClientMediator.props, name="mediatorActor")

  // Spawn an inbox actor, "gui's actor" (a way of creating an actor on the fly, in a class)
  val i = Inbox.create(sys)

  // Allow the mediator to fill our inbox, and send some initializing messages.
  i watch mediator
  i send(mediator, GuiToClientMediator.PassClientActor(clientActor))
  i send(mediator, GuiToClientMediator.PassGUIsysActr(i.getRef))
  i send(clientActor, GuiToClientMediator.PassMediator(mediator))
  i send(mediator, GuiToClientMediator.LockMediator)


  // Recursively update messages in GUI, wait up to an hour for messages.
  def fn: Unit = {
    i send(mediator, GuiToClientMediator.LockMediator)
    mediator ! GuiToClientMediator.Waiting // TODO: mediator should send this message.
    val r= Future(i.receive(1 hour).asInstanceOf[ByteString])
    val p = Promise[ByteString]()
    p completeWith r
    p.future onSuccess {
      case x =>
        println(x)
        msgArea.appendText(x.decodeString("UTF-8"))
        fn
    }
  }
  fn

  ourMessage.setWrapText(true)

  def isAppropriateFormat(str: String): Boolean =  str.length > 0 && !str.matches("[ ]+")


  def removeNewLines(str: String): String = str.replaceAll("[\\n]","")

  def sendF(msg: String): Unit = {
    i send(clientActor, ByteString(msg))
    msgArea.appendText(displayName+": "+msg+"\n")
    ourMessage.text = ""
  }

  // Sending a message by pressing the button with mouse click
  def onSend(event: ActionEvent) {

    val msg = removeNewLines(ourMessage.text.value)

    if(isAppropriateFormat(msg))
      sendF(msg)
  }

  /// Sending a message by pressing enter
  def onSendEnter(event: KeyEvent): Unit = {
    val msg = removeNewLines(ourMessage.text.value)

    if(event.getCode.getName==KeyCode.Enter.getName)
     if(isAppropriateFormat(msg))
       sendF(msg)
  }


  def onEnter(event: ActionEvent): Unit = {
    if(isAppropriateFormat(ourMessage.text.value)) {
      i send(clientActor, ByteString(ourMessage.text.value))
      ourMessage.text = ""
    }
  }


  def onJoin(event: ActionEvent): Unit = {
      val username = name.text.value
      val host = ip.text.value
      val socketAddress =
        new InetSocketAddress(
            host,
            port.text.value.toInt
        )

      i send(clientActor, Connect(username, socketAddress , clientActor) )
  }


  def onClose(event: ActionEvent): Unit = {
    i send(clientActor, "GUIissuesDisconnect")
   Platform.exit()
  }

  // TODO: Add more buttons/GUI features in general (timestamps, connect Chatastrophe.interface . . .).

}
