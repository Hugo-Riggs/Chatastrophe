/***
* Chatastrophe chat service test suite
*
* A client establishes connection to a server with akka actor remoting.
*
* localActor : Client connection
* remoteActor : Server
*
* Setting up remote actor communication
* Helpful links:
* AKKA API: http://doc.akka.io/api/akka/2.4/?_ga=1.235847814.628462600.1470328858#akka.stream.stage.Context
* ScalaFX API: http://www.scalafx.org/api/8.0/index.html#scalafx.application.JFXApp
*
* The dependency I am using, on-top of ScalaFX: http://vigoo.github.io/posts/2014-01-12-scalafx-with-fxml.html
*
* Might be helpful in communicating with gui:
* http://stackoverflow.com/questions/20828726/javafx2-or-scalafx-akka
* https://groups.google.com/forum/#!topic/akka-user/jvBo7TPo1DY
*
* akka actor connection example: http://stackoverflow.com/questions/15547090/akka-bindexception-when-trying-to-connect-to-remote-actor-address-already-in-us
*
* Remoting with akka actors: http://doc.akka.io/docs/akka/2.4.9-RC2/scala/remoting.html#remote-sample-scala
* Creating actors: http://doc.akka.io/docs/akka/2.4.9-RC1/scala/actors.html#creating-actors
*
* read about serialization warnings: http://doc.akka.io/docs/akka/2.4.0/scala/serialization.html
*/

import Chatastrophe.Server
import Chatastrophe.interface._
import RequestReply._

/*
import org.scalatest.Matchers
import  org.scalatest.FlatSpec

class ChatastropheSpec extends FlatSpec with Matchers {

    "a Chatastrophe  server" should
      "create controls" in {
      val unit = Server.start
      Unit should be(unit)
    }

}
*/


import org.scalatest.FunSuite

class remotingTest extends FunSuite {

  test("server print help message") {
    Server.help
    akka.io.
  }

  test("start server"){
    Server.start
  }

  test("connect with client"){
  //  ClientWithCLI.main(Array[String]("testUser", "192.168.1.100:6452"))
    //ClientWithCLI.sendMessage("test message helo!")
  }

  test("stop server"){
    Server.stop
  }

}
