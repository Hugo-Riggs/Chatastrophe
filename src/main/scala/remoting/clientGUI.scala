import akka.actor.{ActorRef}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color._


object clientGUI extends JFXApp {
	stage = new JFXApp.PrimaryStage {
		title.value = "Chatastrophe"
		width = 600
		height = 450
		scene = new Scene {
			fill = LightGreen	
		}
	}
}
