package remoting

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scala.reflect.runtime.universe.typeOf
import scalafxml.core.{DependenciesByType, FXMLView}
import akka.actor._

class GUIscalaFXinitializer(actorRef: ActorRef, sys: ActorSystem) extends JFXApp {

  val root = FXMLView(getClass.getResource("clientGUI.fxml"),
    new DependenciesByType(Map(
      typeOf[ActorRef] -> actorRef,
      typeOf[ActorSystem] -> sys)))

  stage = new JFXApp.PrimaryStage {
    scene = new Scene(root)
  }
}
