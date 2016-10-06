package interface

import java.net
import java.net.InetAddress

import akka.actor._
import com.typesafe.config.ConfigFactory
import remoting.RemoteA

// A main object to launch the server with.
object ServerCLI extends App {

    val context =
        ActorSystem("ChatastropheRemoteActorSys",
            ConfigFactory.parseString("akka.remote.netty.hostname="+InetAddress.getLocalHost)
              .withFallback(ConfigFactory.load()))

    val remoteActor = context.actorOf(RemoteA.props, "remoteActor")

    println("remote actor up. . . ")
    readLine

}