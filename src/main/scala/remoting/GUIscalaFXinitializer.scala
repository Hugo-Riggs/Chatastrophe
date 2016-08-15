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

class GUIscalaFXinitializer(actorRef: ActorRef, sys: ActorSystem) extends JFXApp {

  val root = FXMLView(getClass.getResource("startscreen.fxml"),
    new DependenciesByType(Map(
      typeOf[ActorRef] -> actorRef,
      typeOf[ActorSystem] -> sys)))

  stage = new JFXApp.PrimaryStage {
    scene = new Scene(root)
  }
}
