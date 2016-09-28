package remoting


// Communication protocol
object CommunicationProtocol {

  sealed abstract class Comms
  case class Connect(address: String, name: String, actorRef: akka.actor.ActorRef) extends Comms 
  case class SendMessage(text: String) extends Comms
  case class ReceiveMessage(text: String) extends Comms
  case class Disconnect(user: String) extends Comms
}
