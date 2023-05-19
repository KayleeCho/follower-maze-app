package followermaze

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.collection.mutable


class PayloadProcessorSpec extends Specification with Mockito{
  trait Context extends Scope {
    val testQueue = mutable.Queue.empty[MessageError]

    val testDlq = new MessagingDeadLetterQueue[MessageError] {
      override def enqueue(message: MessageError): Unit = {
        testQueue.enqueue(message)

      }
    }
    val clientPool = mock[ClientPool]

    val processor = new PayloadProcessor(testDlq, clientPool)

  }

  "when it is valid message, client pool sends a message " in new Context {

    val validPayload = "43|P|32|56"
    processor.process(validPayload)


    there was one(clientPool).send("43|P|32|56")( 56L)
  }

  "when it is invalid message, the invalid message gets added to DLQ " in new Context {

    val invalidPayload = "43|P|32|asd"
    processor.process(invalidPayload)

    testQueue.dequeue()  ==== ParseError("43|P|32|asd")

  }
}
