package followermaze


import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.collection.mutable


class EventListenerSpec extends Specification with Mockito{
  trait Context extends Scope {
    val payloadProcessor = mock[PayloadProcessor]
    val logger = mock[QueueStatusLogger]
    var states = (41L, scala.collection.mutable.Map[Long, String]())

    states = states.copy (42L, mutable.Map(42L ->"42|P|32|16"))
    val eventListener = new EventListener(payloadProcessor, logger)
  }

  "process the current payload and also process the payload that has not been processed yet" in new Context {

   val payload = "43|P|32|56"
    eventListener.processEvent(Some(payload))(states)

    there was one (payloadProcessor).process(payload)
    there was one(logger).printTheLastProcessedPointer(43L)


  }


}
