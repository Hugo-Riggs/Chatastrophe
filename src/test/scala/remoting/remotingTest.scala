package remoting

/*
* Setting up remote actor communication
* HELPFUL LINKS:
* API: http://doc.akka.io/api/akka/2.4/?_ga=1.235847814.628462600.1470328858#akka.stream.stage.Context
* EXAMPLE: http://stackoverflow.com/questions/15547090/akka-bindexception-when-trying-to-connect-to-remote-actor-address-already-in-us
* REMOTING WITH ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC2/scala/remoting.html#remote-sample-scala
* ACTORS: http://doc.akka.io/docs/akka/2.4.9-RC1/scala/actors.html#creating-actors
 */

import org.scalatest.FunSuite
import akka.actor._

class SetSuite extends FunSuite {

  remoteInit.init

  import com.typesafe.config.ConfigFactory                                          // NEEDED FOR TEST ON LOCAL MACHINE
  val system = ActorSystem("localActorSystem", ConfigFactory.load("client"))        // NEEDED FOR TEST ON LOCAL MACHINE
  val localActor = system.actorOf(localA.props, name="localActr")
  localActor ! "Pretty Awesome stuff"

  readLine

}