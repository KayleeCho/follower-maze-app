package maze.server

import maze.model._
import maze.servers.EventServer
import maze.service.MessageRouter
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

import scala.collection.mutable


class EventServerSpec extends Specification with Mockito {


  trait Context extends Scope {
    val messageService = mock[MessageRouter]
    val testQueue = mutable.Queue.empty[ParseError]
    val DLQ = new DeadLetterQueueManager[ParseError]{
      override def enqueue(message: ParseError): Unit = {
        testQueue.enqueue(message)
      }
    }
    val eventServer = new EventServer(messageService, DLQ)
  }

  "EventServer" >> {
    "when it is valid message, it sends a message " in new Context {
      val validMessage = Broadcast(542532L, "542532|B")

      eventServer.updateEventQueue((validMessage.seqNo - 1L, Map.empty))(validMessage.payload)
      there was one(messageService).sendMessages(validMessage)
    }

    "does not distribute a message if it is not a valid one" in new Context {
      val inValidMessage = Broadcast(321L, "malformedpayload")

      eventServer.updateEventQueue((inValidMessage.seqNo - 1L, Map.empty))(inValidMessage.payload)
      testQueue.dequeue() ==== ParseError("malformedpayload")
      there was no(messageService).sendMessages(inValidMessage)
    }
  }
}