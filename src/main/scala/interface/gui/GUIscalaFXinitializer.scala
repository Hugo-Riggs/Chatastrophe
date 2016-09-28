package interface.gui 

// Here some java dependencies are included for GUI behavior
//import javaGuiDependencies._
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.stage.StageStyle

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scala.reflect.runtime.universe.typeOf
import scalafxml.core.{DependenciesByType, FXMLView}
import akka.actor._

class GUIscalaFXinitializer(actorRef: ActorRef, sys: ActorSystem) extends JFXApp {

  // Load SceneBuilder generated Graphical User Interface markdown code
  val root = FXMLView(getClass.getResource("clientGUI.fxml"),
    new DependenciesByType(Map(
      typeOf[ActorRef] -> actorRef,
      typeOf[ActorSystem] -> sys)))

  // Call the JavaFX main function, launching the gui
  stage = new JFXApp.PrimaryStage {
    scene = new Scene(root)
  }

  // Give some style options
  stage.initStyle(StageStyle.UNDECORATED); stage.setMinHeight(150); stage.setMinWidth(200); stage.show


  var xOffset, yOffset: Double = 0
  val ResizeSpace = -4
  val ResizeSpaceRight = 5
  val ResizeSpaceTop = -26

  root.setOnMouseDragged(new EventHandler[MouseEvent] {
   override def handle(event: MouseEvent): Unit = {

    // Drag on the condition that we are not resizing.
      // top and bottom
      if(!(yOffset > ResizeSpace) & yOffset > ResizeSpaceTop
      // right and left
      & !(xOffset > ResizeSpace) & dis1D(stage.getWidth, event.getSceneX) > ResizeSpaceRight){
         stage.setX(event.getScreenX + xOffset)
         stage.setY(event.getScreenY + yOffset)
      }
   }
  })

  def dis1D(x: Double, y: Double): Double = Math.abs( x-y )

  root.setOnMousePressed(new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent) {
      xOffset = stage.getX - event.getScreenX;
      yOffset = stage.getY - event.getScreenY;
    }
  })

  // Imported Java class allows UNDECORATED stage to be resized.
 // ResizeHelper.addResizeListener(stage)

}
