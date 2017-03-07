package Chatastrophe

/***
  * This is the command line interface for the server.
  *
  * TODO: decouple server from actor, or server is actor...
  * TODO: client ! SendMsg(name, Msg) => server ! ReceiveMsg(name+msg)
  */

import akka.actor._
import com.typesafe.config.ConfigFactory
import RequestReply.RemoteA

// A main object to launch the server with.
object Server {

  private var remoteActor: ActorRef = _
  private val version = "1.0"
  private val helpString = "Help:" +
  "\nCommands\n" +
  "exit\tShuts down the server, all connected clients are disconnected.\n"


  def help() = Console.println("Chatastrophe " + version + "\nenter h for help\n"+helpString)

  def stop() = remoteActor ! Kill ; println("remote actor killed")

  def startWithLoopedInputMode(): Unit = {
        start()
        print("Chatastrophe " + version + "\nenter h for help")
        def inputLoop(s: String): Unit = {
            s match {
                case "exit" => stop()
                case "stop" => stop()
                case "h" => help()
            }
          inputLoop(scala.io.StdIn.readLine())
        }
      inputLoop("h")
  }

  // Starts the server
  def start()  = {
    // Load from resources
    val conf = ConfigFactory.load
    val port = conf.getInt("akka.remote.netty.tcp.port")
    val ipAdd = java.net.InetAddress.getLocalHost

    // Akka actor context
    val context =
      ActorSystem("ChatastropheRemoteActorSys",
      ConfigFactory.parseString("akka.remote.netty.hostname="+ipAdd)
      .withFallback(ConfigFactory.load()))

    // Create the akka actor
    def startRemoteActor: ActorRef = {
      println("remote actor starting at "+ipAdd+":"+port+". . . ")
      context.actorOf(RemoteA.props, "remoteActor")
    }

    remoteActor = startRemoteActor 
  }

}
