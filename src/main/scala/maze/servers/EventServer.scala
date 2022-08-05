package maze.servers

import java.io.{BufferedReader, InputStreamReader}
import java.net.ServerSocket

import maze.model.{Message, ParseFailed}
import maze.service.MessagingService

import scala.annotation.tailrec

class EventServer(messagingService: MessagingService) extends Runnable {
  private val eventPort = 9090
  private val initializedQueueStatus = (0L, Map.empty[Long, Message])

  private type QueueStatus = (Long, Map[Long, Message]) // tuple of lastProcessedSeqNumber and current event queue that contains all current events

  override def run(): Unit = {
    System.out.println("Listening for events on " + eventPort)
    val eventSocket = new ServerSocket(eventPort).accept()
    try {
      val reader = new BufferedReader(
        new InputStreamReader(eventSocket.getInputStream()))
      processEvent(Option(reader.readLine()))(initializedQueueStatus)
    } finally if (eventSocket != null) eventSocket.close()
  }

  private def processEvent(nextEventExist: => Option[String])(currentQueueStatus: QueueStatus): Unit = {

    nextEventExist match {
      case Some(payload) => val nextQueueStatus = getEventQueueStatus(currentQueueStatus)(payload)
        processEvent(_: Option[String])(nextQueueStatus)
      case None => ()
    }
  }

  def getEventQueueStatus(queueStatus: QueueStatus)(
    payload: String): QueueStatus = {
    val (lastProcessedSeqNo, hashMapOfSeqAndPayload) = queueStatus
    @tailrec
    def dequeueOnMessageSent(queueState: QueueStatus): QueueStatus = {
      val (lastProcessedSeqNo, hashMapOfSeqAndPayload) = queueState
      val seqToProcess = lastProcessedSeqNo + 1L

      hashMapOfSeqAndPayload.get(seqToProcess) match {
        case None => queueStatus
        case Some(payload) =>
          messagingService.sendMessages(payload)
          dequeueOnMessageSent(seqToProcess, hashMapOfSeqAndPayload - (seqToProcess))
      }
    }

    def ifParseFailed(error: ParseFailed): QueueStatus = {
      System.out.println("Non parsible Message received: " + error.message)
      queueStatus
    }

    def ifValid(message: Message): QueueStatus = {
      System.out.println("Message received: " + payload)
      dequeueOnMessageSent(lastProcessedSeqNo,
        hashMapOfSeqAndPayload + (message.seqNo -> message))
    }

    Message.parse(payload) match {
      case Left(error) => ifParseFailed(error)
      case Right(message) => ifValid(message)
    }
  }
}

