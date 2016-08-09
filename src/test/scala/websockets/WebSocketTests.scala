package websockets
// HELPFUL LINKS

// http://stackoverflow.com/questions/30964824/how-to-create-a-source-that-can-receive-elements-later-via-a-method-call
//  val ref = Source.actorRef[Message](Int.MaxValue, akka.stream.OverflowStrategy.dropHead)
// http://stackoverflow.com/questions/34558522/how-do-i-send-message-to-actorref-at-start-of-akka-stream-2-0-flowp

  // useful but old example
  // https://github.com/ScalaConsultants/websocket-akka-http/blob/master/src/main/scala/io/scalac/akka/http/websockets/chat/ChatRoom.scala

// helpful as well
// https://github.com/johanandren/scala-stockholm-cluster-message-broker/tree/master/src/main/scala

// Source from graph
// http://doc.akka.io/docs/akka/2.4.9-RC1/scala/stream/stream-graphs.html#constructing-sources-sinks-flows-from-partial-graphs-scala

import org.scalatest.FunSuite
/*
class SetSuite extends FunSuite {

  // Start The server
  Server("start"); println("Server started. . .")

  import akka.actor.ActorSystem
  implicit val actorSystem = ActorSystem("akka-system")
  val client = Client("Junkrat", "localhost", 8080)

  readLine
  Server("stop"); println("Server stopped. . . ")


 }*/
