package remoting

/*
* localActor (Client connection)
* remoteActor (Server)
*
* Setting up remote actor communication
* HELPFUL LINKS:
* AKKA API: http://doc.akka.io/api/akka/2.4/?_ga=1.235847814.628462600.1470328858#akka.stream.stage.Context
* SCALAFX API: http://www.scalafx.org/api/8.0/index.html#scalafx.application.JFXApp
* EXAMPLE: http://stackoverflow.com/questions/15547090/akka-bindexception-when-trying-to-connect-to-remote-actor-address-already-in-us
* REMOTING WITH ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC2/scala/remoting.html#remote-sample-scala
* ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC1/scala/actors.html#creating-actors
 */

import org.scalatest.FunSuite
import akka.actor._

class SetSuite extends FunSuite {

  remoteInit.init   // Start the server actor

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE
  val localActor = system.actorOf(localA.props, name="localActr")                 // Start the client

  // Start the client GUI
  val GUIactor = system.actorOf(clientGUI_actor.props(localActor), name = "guiActor")
  GUIactor ! StartGUI(Array("Chatastrophe"), localActor)

  // Already works, working on GUI integration now.
  /*
  val userName = "Hugo"

  localActor ! Join("127.0.0.1:2552", userName)
  localActor ! SendMessage("Pretty Awesome stuff")  // send first message to server

  localActor ! Join("127.0.0.1:2552", "Hugo2")  // Try joining from the same client a second time... (should do nothing)

  localActor ! SendMessage("yea we are on a rolll") // send second message

  localActor ! Disconnect(userName)
*/
  readLine

}