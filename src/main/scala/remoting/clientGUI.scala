package remoting


import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.geometry.Insets
import scalafx.stage.Stage
import scala.reflect.runtime.universe.typeOf
import scalafxml.core.{DependenciesByType, FXMLView}
import akka.actor._

class GUIscalaFXinitializer(actorRef: ActorRef) extends JFXApp {

  val root = FXMLView(getClass.getResource("startscreen.fxml"),
    new DependenciesByType(Map(
      typeOf[ActorRef] -> actorRef)))

  stage = new JFXApp.PrimaryStage() {
    scene = new Scene(root)
  }
}









/*
// -------------------------------------------------------------------------------------------
// Actor For GUI
import akka.actor._

case class PollTextLog()
case class InformClientOfGUI(clientGUIactor: ActorRef)
case class StartGUI(args: Array[String], client: ActorRef)

object clientGUI_actor {
  def props(actorRef: ActorRef): Props = Props(new clientGUI_actor(actorRef))
}

class clientGUI_actor(client: ActorRef) extends Actor {
 var clientGUIinActor = List.empty[clientGUI]

  //val javaFxActor = context.actorOf(Props[JavaFxActor].withDispatcher("javafx-dispatcher"), "javaFxActor")
  //javaFxActor ! "to the javafx Actor!"

  def receive = {
    case PollTextLog() =>
      if(clientGUIinActor.isEmpty){println("clientGUI_actor: start gui." )}
      else {clientGUIinActor(0).textArea.text = log.textLog}
    case ReceiveMessage(text) =>
      if(clientGUIinActor.isEmpty){println("clientGUI_actor: start gui.")}
      else {println("clientGUIactor received message: " + text);clientGUIinActor(0).textArea.text=text}
    case StartGUI(args: Array[String], client: ActorRef) => {
      if(clientGUIinActor.isEmpty){
      clientGUIinActor = List(startGUI(args, client))
      clientGUIinActor(0).main(Array())
      } else {println("clientGUI_actor:  gui already started.")}
    }
  }

  def startGUI(args: Array[String], client: ActorRef): clientGUI = new clientGUI(args, client);

}

class JavaFxActor extends Actor {
  def receive = {
    case _ =>
    println("javafx actor received message")
  }
}

// -------------------------------------------------------------------------------------------

// GUI scalaFX class
class clientGUI(args: Array[String], clientActor: ActorRef) extends JFXApp {

  val textArea = new TextArea() {
    text = "AREA FOR RECEIVED TEXT MESSAGES"
  }

    stage = new JFXApp.PrimaryStage {
      title.value = args(0)
      // Size properties
      width = 600; height = 450

      scene = new Scene {
        fill = LightBlue
        content = new GridPane {
          // Positioning properties
          hgap = 10
          vgap = 10
          padding = Insets(20, 100, 10, 10)

          // add interface components here add(_, col, row)
            add(new TextField(){  // For address and port
              promptText = "example: 198.256.124.100:8080"
            },0,0)

            add( new Button("Connect") {  // Connect to supplied address/port button
              onAction = handle { clientActor ! Join("127.0.0.1:2552", "Junkrat")}
            },1,0)

            add( textArea, 2,0)

            add( new Button("poll") {  // Connect to supplied address/port button
              onAction = handle { textArea.text = log.getTextLog; println("text log " + log.getTextLog) }
            },3,0)

           add( new Button {  // Close button
              text = "close()"
              onAction = handle { stage.close()}
            },0,1)

        } // END CONTENT
      } // END SCENE
    }  // END STAGE
}

*/
