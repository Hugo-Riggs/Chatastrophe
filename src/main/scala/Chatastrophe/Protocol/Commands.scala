package Chatastrophe.Protocol

case class UpdatePeers(
  connections:
  collection.mutable
  .Map[java.net.InetSocketAddress, akka.actor.ActorRef]
)
case object Shutdown
case object GetUserName
case class UserName(name: akka.util.ByteString, remote: java.net.InetSocketAddress)
case object RepeatedUsername
