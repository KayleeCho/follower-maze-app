package maze.registry

import java.io.{ByteArrayOutputStream, IOException}
import java.net.Socket

import maze.model.{DeadLetterQueueManager, DeliveryError, MessageError}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.collection.mutable

class UserRegistrySpec extends Specification with Mockito {

  trait Context extends Scope {
    val testQueue = mutable.Queue.empty[MessageError]

    val testDeadLetterQueue = new DeadLetterQueueManager[DeliveryError] {
      override def enqueue(deadLetter: DeliveryError): Unit =
        testQueue.enqueue(deadLetter)
    }

    val clientsRegistry = new UserRegistry(testDeadLetterQueue)
    val successSocket = mock[Socket]
    successSocket.getOutputStream returns (new ByteArrayOutputStream())
    val errorSocket = mock[Socket]
    errorSocket.getOutputStream throws (new IOException("test exception"))
    val testMessage = "testMessage"
  }

  "send private message" >> {

      "when receiver is on line and sending message is sucessful, there is no message equeued in the deadletter queue" in new Context {
        clientsRegistry.add(1L, successSocket)
        clientsRegistry.send(testMessage)(1L) must not(throwAn[Exception])
        testQueue must beEmpty
      }
      "when receiver is on line  but sending is failed, there is message enqueued in dead letter queue " in new Context {
        clientsRegistry.add(1L, errorSocket)
        clientsRegistry.send(testMessage)(1L) must not(throwAn[Exception])
        testQueue.dequeue() ==== DeliveryError(1L, testMessage)
      }

      "when the receiver is not online, it equeues message in dead letter queue" in new Context {
        clientsRegistry.send(testMessage)(1L) must not(throwAn[Exception])
        testQueue.dequeue() ==== DeliveryError(1L, testMessage)
      }


  }

  "broadCast" >> {
    "when sending fails, it puts the message in the dead letter queue and fail silently" in new Context {
      clientsRegistry.add(1L, successSocket)
      clientsRegistry.add(2L, errorSocket)
      clientsRegistry.broadcast(testMessage) must not(throwAn[Exception])
      testQueue.dequeue() ==== DeliveryError( 2L, testMessage)
    }
  }
}