package websockets

import scala.language.implicitConversions

package object sessionEventHandling {
  implicit def sessionEventToChatMessage(event: IncomingMessage): UserMessage =
    UserMessage(event.sender, event.message)
}