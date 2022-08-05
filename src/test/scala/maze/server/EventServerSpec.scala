package maze.server

import maze.model._
import maze.servers.EventServer
import maze.service.MessagingService
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

class EventServerSpec extends Specification with Mockito {


  trait Context extends Scope {
    val messageService = mock[MessagingService]
    val eventServer = new EventServer(messageService)
  }

  "EventServer" >> {
    "distributes a message if it gets a valid one" in new Context {
      val validMessage = Broadcast(542532L, "542532|B")

      eventServer.updateEventQueue((validMessage.seqNo - 1L, Map.empty))(validMessage.payload)
      there was one(messageService).sendMessages(validMessage)
    }

    "does not distribute a message if it is not a valid one" in new Context {
      val inValidMessage = Broadcast(542532L, "malformedpayload")

      eventServer.updateEventQueue((inValidMessage.seqNo - 1L, Map.empty))(inValidMessage.payload)
      there was no(messageService).sendMessages(inValidMessage)
    }
  }
}