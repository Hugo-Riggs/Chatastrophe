package io.akka.http.websockets.services

import akka.http.scaladsl.server.Route

trait WebService {

  def route: Route

}