package websockets

import akka.actor.ActorSystem

object SessionManager {
  var sessions: Map[Int, session] = Map.empty[Int, session]

  def  findOrCreate(number: Int)(implicit actorSystem: ActorSystem): session =
      sessions.getOrElse(number, createNewSession(number))

  private def createNewSession(number: Int)(implicit actorSystem: ActorSystem):session = {
    val a_session = session(number)
    sessions += number -> a_session
    a_session
  }

}