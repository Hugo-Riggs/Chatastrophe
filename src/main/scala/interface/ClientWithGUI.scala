package interface

/***
* Simple GUI client interface for the chatastrophe akka actor IM software.
*/

import akka.actor._
import scala.language.postfixOps

object ClientWithGUIGui extends App {

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

  // Start the client
  import remoting.LocalA
  val localActor = system.actorOf(LocalA.props, name="localActr")                 

  // ScalaFX implementation with ScalaFXML
  import gui.GUIscalaFXinitializer
  val GUI = new GUIscalaFXinitializer(localActor, system)  
  GUI.main(Array(""))
}
