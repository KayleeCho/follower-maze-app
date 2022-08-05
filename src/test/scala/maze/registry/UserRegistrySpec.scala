package maze.registry

import java.io.{ByteArrayOutputStream, IOException}
import java.net.Socket

import maze.model.{DeadLetterQueueManager, DeliveryFailedMessage}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.collection.mutable

class UserRegistrySpec extends Specification with Mockito {

  trait Context extends Scope {
    val testQueue = mutable.Queue.empty[DeliveryFailedMessage]

    val testDeadLetterQueue = new DeadLetterQueueManager[DeliveryFailedMessage] {
      override def enqueue(deadLetter: DeliveryFailedMessage): Unit =
        testQueue.enqueue(deadLetter)
    }

    val clientsRegistry = new UserRegistry(testDeadLetterQueue)
    val mockGoodSocket = mock[Socket]
    mockGoodSocket.getOutputStream returns (new ByteArrayOutputStream())
    val mockBadSocket = mock[Socket]
    mockBadSocket.getOutputStream throws (new IOException("test exception"))
    val testMessage = "testMessage"
  }

  "send private message" >> {

      "when receiver is on line and sending message is sucessful, there is no message equeued in the deadletter queue" in new Context {
        clientsRegistry.add(1L, mockGoodSocket)
        clientsRegistry.send(testMessage)(1L) must not(throwAn[Exception])
        testQueue must beEmpty
      }
      "when receiver is on line  but sending is failed, there is message enqueued in dead letter queue " in new Context {
        clientsRegistry.add(1L, mockBadSocket)
        clientsRegistry.send(testMessage)(1L) must not(throwAn[Exception])
        testQueue.dequeue() ==== DeliveryFailedMessage(testMessage)
      }

      "when the receiver is not online, it equeues message in dead letter queue" in new Context {
        clientsRegistry.send(testMessage)(1L) must not(throwAn[Exception])
        testQueue.dequeue() ==== DeliveryFailedMessage(testMessage)
      }


  }

  "broadCast" >> {
    "tries to send message, if sending fails, fails silently and puts the message in the dead letter queue" in new Context {
      clientsRegistry.add(1L, mockGoodSocket)
      clientsRegistry.add(2L, mockBadSocket)
      clientsRegistry.broadcast(testMessage) must not(throwAn[Exception])
      testQueue.dequeue() ==== DeliveryFailedMessage( testMessage)
    }
  }
}