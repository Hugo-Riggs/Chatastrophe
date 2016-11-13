package interface

/***
  *
  * TODO: decouple server from actor, or server is actor...
  * TODO: client ! SendMsg(name, Msg) => server ! ReceiveMsg(name+msg)
  */

import akka.actor._
import com.typesafe.config.ConfigFactory
import remoting.RemoteA

// A main object to launch the server with.
object Server {

  var remoteActor: ActorRef = _
  val version = "1.0"
  val helpString = "Help:" +
  "\nCommands\n" +
  "exit\tShuts down the server, all connected clients are disconnected.\n"


  def help = Console.println("Chatastrophe " + version + "\nenter h for help\n"+helpString)


  def stop = remoteActor ! Kill ; println("remote actor killed")


  //def isActiveAndOnline = remoteActor.


  def startWithLoopedInputMode : Unit = {
        start
        print("Chatastrophe " + version + "\nenter h for help")
        def inputLoop(s: String): Unit = {
            s match {
                case "exit" => stop
                case "stop" => stop
                case "h" => help
            }
          inputLoop(scala.io.StdIn.readLine())
        }
      inputLoop("h")
  }


  def start  = {
    val conf = ConfigFactory.load
    val port = conf.getInt("akka.remote.netty.tcp.port")
    val ipAdd = java.net.InetAddress.getLocalHost

    val context =
      ActorSystem("ChatastropheRemoteActorSys",
      ConfigFactory.parseString("akka.remote.netty.hostname="+ipAdd)
      .withFallback(ConfigFactory.load()))


    def startRemoteActor: ActorRef = {
      println("remote actor starting at "+ipAdd+":"+port+". . . ")
      context.actorOf(RemoteA.props, "remoteActor")
    }

    remoteActor = startRemoteActor 
    //val remoteActor = startRemoteActor
  }

}
