package interface

import akka.actor._
import remoting.RemoteA

// A main object to launch the server with.
object ServerCLI extends App {

    val context = ActorSystem("ChatastropheRemoteActorSys")
    val remoteActor = context.actorOf(RemoteA.props, "remoteActor")

    println("remote actor up. . . ")
    readLine

}
