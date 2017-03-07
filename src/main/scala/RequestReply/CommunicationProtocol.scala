/***
  * The trait CommunicationProtocol defines methods and
  * signals which cooperate to form the basis of
  * Chatastrophe's request reply behavior.
  */

package RequestReply

import java.net.InetSocketAddress

import akka.actor.{ActorRef, PoisonPill}
import akka.io.{IO, Udp}
import akka.util.ByteString

import scala.concurrent.{Future, Promise}

trait CommunicationProtocol {

  /***
 * ChatLogger is used to keep a chat sessions, chat history.
 */
  object ChatLogger  {
    import concurrent.ExecutionContext.Implicits.global
      // private:
    private val buf = collection.mutable.ArrayBuffer.empty[String]
    private lazy val log = Future[String] { buf.toArray.mkString("\n") }
      // public:
    def write(text: String): Unit =  buf += text
    def read: Future[ByteString] = Future { ByteString(buf.toArray.mkString("\n")) }
  }

  sealed abstract class protocol
  case class Connect(username: String, socketAddress: InetSocketAddress, actorRef: akka.actor.ActorRef) extends protocol
  case class Disconnect(username: String, actorRef: ActorRef) extends protocol



}
