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
* ScalaSwing API: http://www.scala-lang.org/api/2.11.2/scala-swing/#package
* SCALAFX API: http://www.scalafx.org/api/8.0/index.html#scalafx.application.JFXApp
* the dependency i am using: http://vigoo.github.io/posts/2014-01-12-scalafx-with-fxml.html
* might be helpful in communicating with gui:
* http://stackoverflow.com/questions/20828726/javafx2-or-scalafx-akka
* https://groups.google.com/forum/#!topic/akka-user/jvBo7TPo1DY
* EXAMPLE: http://stackoverflow.com/questions/15547090/akka-bindexception-when-trying-to-connect-to-remote-actor-address-already-in-us
* Example akka actors and swing interaction: http://stackoverflow.com/questions/15203758/asynchronous-ui-update-with-swing
* REMOTING WITH ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC2/scala/remoting.html#remote-sample-scala
* ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC1/scala/actors.html#creating-actors
*
* read about serialization warnings: http://doc.akka.io/docs/akka/2.4.0/scala/serialization.html
 */

import org.scalatest.FunSuite
import akka.actor._


class SetSuite extends FunSuite {
<<<<<<< HEAD
=======

>>>>>>> 7c746020fe45881afd55353adda3b533fdb26a9b
  remoteInit.init   // Start the server actor

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE

<<<<<<< HEAD
  val localActor = system.actorOf(localA.props, name="localActr")                 // Start the client
  localActor ! Join("127.0.0.1:2552", "Junkrat")                                  // Connect
=======
  localActor ! Join("127.0.0.1:2552", "Junkrat")  //localActor ! Join("192.168.1.101:2552", "Junkrat")
    //localActor ! Join("10.250.27.26:2552", "Desktop")  //localActor ! Join("192.168.1.101:2552", "Junkrat")

  //---------------------------------------------------------------------------------------------------------
  // ScalaFX implementation with ScalaFXML
>>>>>>> 7c746020fe45881afd55353adda3b533fdb26a9b

  val GUI = new GUIscalaFXinitializer(localActor, system)  // ScalaFX implementation with ScalaFXML
  GUI.main(Array(""))



  // Already works, working on to GUI integration now.
  /*
  val userName = "Hugo"

  localActor ! Join("127.0.0.1:2552", userName)
  localActor ! SendMessage("Pretty Awesome stuff")  // send first message to server

  localActor ! Join("127.0.0.1:2552", "Hugo2")  // Try joining from the same client a second time... (should do nothing)

  localActor ! SendMessage("yea we are on a rolll") // send second message

  localActor ! Disconnect(userName)
*/
//  readLine

}
