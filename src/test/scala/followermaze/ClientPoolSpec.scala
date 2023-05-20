package followermaze

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import java.io.{ByteArrayOutputStream, IOException}
import java.net.Socket
import scala.collection.mutable

class ClientPoolSpec extends Specification with Mockito {
    trait Context extends Scope {
      val testQueue = mutable.Queue.empty[MessageError]

      val testDeadLetterQueue = new MessagingDeadLetterQueue[MessageError] {
        override def enqueue(deadLetter: MessageError): Unit =
          testQueue.enqueue(deadLetter)
      }

      val clientsRegistry = new ClientPool(testDeadLetterQueue)
      val successSocket = mock[Socket]
      successSocket.getOutputStream returns (new ByteArrayOutputStream())
      val errorSocket = mock[Socket]
      errorSocket.getOutputStream throws (new IOException("test exception"))
      val testMessage = "test"
    }

    "send private message" >> {

        "when receiver is on line and sending message is sucdessful, there is no message enqueued in the deadletter queue" in new Context {
          clientsRegistry.add(1L, successSocket)
          clientsRegistry.send(testMessage)(1L) must not(throwAn[Exception])
          testQueue must beEmpty
        }
        "when receiver is online  but sending is failed, there is message enqueued in dead letter queue " in new Context {
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
