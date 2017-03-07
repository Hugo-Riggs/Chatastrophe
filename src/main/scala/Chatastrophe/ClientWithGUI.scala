package Chatastrophe

/***
* Simple GUI client Chatastrophe.interface for the chatastrophe akka actor IM software.
*/

import akka.actor._
import gui.GUIscalaFXinitializer

import scala.language.postfixOps

object ClientWithGUI extends App {

  import com.typesafe.config.ConfigFactory
  import gui.GUIscalaFXinitializer                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

  // Start the client
  import RequestReply.Client
  // TODO gui needs to modify client address and name later on.
  //val client = system.actorOf(Client.props("",""), name="guiActor")

  // ScalaFX implementation with ScalaFXML
  //val GUI = new GUIscalaFXinitializer(client, system)
  //GUI.main(Array(""))
}