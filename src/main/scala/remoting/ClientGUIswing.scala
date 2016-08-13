package remoting
/*
import scala.swing._
import akka.actor._
import event.ButtonClicked

trait GUIProgressEventHandler {
  def handleGuiProgressEvent(event: GuiEvent)
}

case class GuiProgressEvent(val percentage: Int) extends GuiEvent
case class GuiReceiveMessage(val msg: String) extends GuiEvent
object ProcessingFinished extends GuiEvent

abstract class GuiEvent

class UI extends SimpleSwingApplication with GUIProgressEventHandler {

  lazy val processItButton = new Button { text = "test button" }
  lazy val sendButton = new Button { text = "Send" }  // Send message button
  lazy val progressBar = new ProgressBar() {min=0; max=100}
  lazy val messageLabel = new Label { text = "" }
  lazy val messageComponent = new TextField{ text = "" }

  def top = new MainFrame {
    title = "GUI Program #1"
    preferredSize = new Dimension(600, 600)
    background = java.awt.Color.blue

      contents = new BoxPanel(Orientation.Vertical) {
        contents += processItButton
        contents += progressBar
        contents += messageComponent
        contents += sendButton
      }

    val workerActor = createActorSystemWithWorkerActor()

    listenTo(processItButton)

    reactions += {
      case ButtonClicked(b) => {
        processItButton.enabled = false
        processItButton.text = "processing"
        workerActor ! "ReceiveText1"
      }
      case
    }
  }



  def handleGuiProgressEvent(event: GuiEvent) {
    event match {
      case progress: GuiProgressEvent => Swing.onEDT {
        progressBar.value = progress.percentage
      }
      case msg: GuiReceiveMessage => Swing.onEDT {
        messageComponent.text = msg.toString
      }
      case ProcessingFinished => Swing.onEDT {
        processItButton.text = "Process it"
        processItButton.enabled = true
      }
    }
  }


  def createActorSystemWithWorkerActor(): ActorRef = {
      import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
    def system = ActorSystem("ActorSystem", ConfigFactory.load("client"))

    val guiUpdateActor = system.actorOf(
      Props[GUIUpdateActor](new GUIUpdateActor(this)), name = "guiUpdateActor")// pass EDT to class

    val workerActor = system.actorOf(
      Props[WorkerActor](new WorkerActor(guiUpdateActor)), name = "workerActor")

    workerActor

  }
}

class GUIUpdateActor(val gui: GUIProgressEventHandler) extends Actor {
  def receive = {
    case event: GuiEvent => gui.handleGuiProgressEvent(event)     // Actor processes received gui events through EDT
  }
}

class WorkerActor(val guiUpdateActor: ActorRef) extends Actor {
  def receive = {
    case "Start" => {
      for (percentDone <- 0 to 100) {
        Thread.sleep(50)
        guiUpdateActor ! GuiProgressEvent(percentDone)
      }
    }
    case "ReceiveText" => {
      guiUpdateActor ! GuiReceiveMessage("Hugo's message")
    }
    case "ReceiveText1" => {
      guiUpdateActor ! GuiReceiveMessage("Hugo's message")
    }

      guiUpdateActor ! ProcessingFinished
  }
}

object GuiProgramOne {
  def main(args: Array[String]) {
    val ui = new UI
    ui.top.visible = true
    println("end of main function")
  }
}*/
