package remoting

/*
* A client establishes connection to a server with akka actor remoting.
*
* localActor (Client connection)
* remoteActor (Server)
*
* Setting up remote actor communication
* HELPFUL LINKS:
* AKKA API: http://doc.akka.io/api/akka/2.4/?_ga=1.235847814.628462600.1470328858#akka.stream.stage.Context
* SCALAFX API: http://www.scalafx.org/api/8.0/index.html#scalafx.application.JFXApp
* might be helpful in communicating with gui:
* http://stackoverflow.com/questions/20828726/javafx2-or-scalafx-akka
*
* EXAMPLE: http://stackoverflow.com/questions/15547090/akka-bindexception-when-trying-to-connect-to-remote-actor-address-already-in-us
* REMOTING WITH ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC2/scala/remoting.html#remote-sample-scala
* ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC1/scala/actors.html#creating-actors
 */

import org.scalatest.FunSuite
import akka.actor._

import akka.dispatch.{DispatcherPrerequisites, ExecutorServiceFactory, ExecutorServiceConfigurator}
import com.typesafe.config.Config
import java.util.concurrent.{ExecutorService, AbstractExecutorService, ThreadFactory, TimeUnit}
import java.util.Collections
import javax.swing.SwingUtilities
import javafx.application.Platform



class SetSuite extends FunSuite {

  remoteInit.init   // Start the server actor

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE
  val localActor = system.actorOf(localA.props, name="localActr")                 // Start the client

  //---------------------------------------------------------------------------------------------------------
  // ScalaFX implementation
  // Start the client GUI
  //val GUIactor = system.actorOf(clientGUI_actor.props(localActor), name = "guiActor")
  //GUIactor ! StartGUI(Array("Chatastrophe"), localActor)

  // Try Akka actors on JavaFX EDT
  //val javaFxActor = system.actorOf(Props[JavaFxActor].withDispatcher("javafx-dispatcher"), "javaFxActor")
  // javaFxActor ! "TEST"

  //localActor ! InformClientOfGUI(GUIactor)
  //---------------------------------------------------------------------------------------------------------

  //---------------------------------------------------------------------------------------------------------
  // Scala-swing implementation
  GuiProgramOne.main(Array("test"))
  //val frameActor = system.actorOf(Props[FrameActor].withDispatcher("swing-dispatcher"), "frame-actor")
  //frameActor ! "test message"


   //---------------------------------------------------------------------------------------------------------
  localActor ! Join("127.0.0.1:2552", "Junkrat")
  localActor ! SendMessage("sent through test code")

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