package followermaze


import java.io.{BufferedReader, InputStreamReader}
import java.net.ServerSocket
import scala.collection.mutable
import scala.concurrent.ExecutionContext



class EventListener(payloadProcessor: PayloadProcessor, logger: QueueStatusLogger ) extends Runnable {
  private val EventPort = 9090

  implicit val ec = ExecutionContext.global
  private var initializedQueueStatus = (0L, scala.collection.mutable.Map[Long, String]())
  type MessageQueueState = (Long, mutable.Map[Long, String]) // the last processed message#,and Map which contains all unprocessed messages with seq# as key and the payload as value


  override def run(): Unit = {
    println(s"Listening for events on $EventPort")
    val eventSocket = new ServerSocket(EventPort).accept()


    try {
      val reader = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()))
      processEvent(Option(reader.readLine))(initializedQueueStatus)
    } finally if (eventSocket != null) eventSocket.close()



  }


  def processEvent(message: => Option[String])(lastQueueStatus: MessageQueueState): Unit = {
     message match {
      case Some(payload) =>  val currQueueState = updateEventQueueBySendingPayload(lastQueueStatus)(payload)
        logger.printTheLastProcessedPointer(currQueueState._1)
        processEvent(_: Option[String])(currQueueState)
      case None => ()
    }

  }



  def updateEventQueueBySendingPayload(currentQueueState: MessageQueueState)(payload: String): MessageQueueState = {

    val lastProcessedSeq =  currentQueueState._1
    val lastStatusMap = currentQueueState._2
    val currSeqPayloadPair = currentQueueState._1 + 1L -> payload


    def dequeAfterProcessing(updateQueueWithCurrentPayload: MessageQueueState):MessageQueueState = {

      val currentProcessingSeq = lastProcessedSeq + 1L
      val updatedMap =  updateQueueWithCurrentPayload._2
      updatedMap.get(currentProcessingSeq) match { // start processing the existing message that is still in the queue
        case None => updateQueueWithCurrentPayload
        case Some(message) => payloadProcessor.process(message)
          dequeAfterProcessing(currentProcessingSeq, updatedMap - (currentProcessingSeq))
      }
    }
    dequeAfterProcessing((lastProcessedSeq, lastStatusMap + (currSeqPayloadPair)))


  }




}

class QueueStatusLogger {
  def printTheLastProcessedPointer(lastProcessedSeq: Long): Unit =
    { println(s"currently the last processed Seq# is ${lastProcessedSeq}")}
}
