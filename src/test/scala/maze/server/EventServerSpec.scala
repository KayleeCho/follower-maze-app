package maze.server

import maze.model._
import maze.servers.MessageReceiver
import maze.service.MessagingService
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

class EventServerSpec extends Specification with Mockito {


  trait Context extends Scope {
    val messageService = mock[MessagingService]
    val eventServer = new MessageReceiver(messageService)
  }

  "EventServer" >> {
    "distributes a message if it gets a valid one" in new Context {
      val validMessage = Follow(123L, 123L, 444L, "testpayload")

      eventServer.getEventQueueStatus((validMessage.seqNo - 1L, Map.empty))(validMessage.payload)
      there was one(messageService).sendMessages(validMessage)
    }

  }

}