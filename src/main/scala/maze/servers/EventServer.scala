package maze.servers

import java.io.{BufferedReader, InputStreamReader}
import java.net.ServerSocket

import maze.model.{Message, ParseError}
import maze.service.MessagingService

import scala.annotation.tailrec

class EventServer(messagingService: MessagingService) extends Runnable {
  private val eventPort = 9090
  private val initializedQueueStatus = (0L, Map.empty[Long, Message])

  private type EventQueue = (Long, Map[Long, Message]) // tuple of lastProcessedSeqNumber and current event queue that contains all current events

  override def run(): Unit = {
    System.out.println("Listening for events on " + eventPort)
    val eventSocket = new ServerSocket(eventPort).accept()
    try {
      val reader = new BufferedReader(
        new InputStreamReader(eventSocket.getInputStream()))
      processEvent(Option(reader.readLine()))(initializedQueueStatus)
    } finally if (eventSocket != null) eventSocket.close()
  }

  private def processEvent(nextEvent: => Option[String])(currentQueueStatus: EventQueue): Unit = {

    nextEvent match {
      case Some(payload) => val nextQueueStatus = updateEventQueue(currentQueueStatus)(payload)
        processEvent(_: Option[String])(nextQueueStatus)
      case None => ()
    }
  }

  def updateEventQueue(queueStatus: EventQueue)(
    payload: String): EventQueue = {
    val (lastProcessedSeqNo, hashMapOfSeqAndPayload) = queueStatus

    @tailrec
    def dequeueAfterSuccessfulSent(queueState: EventQueue): EventQueue = {
      val (lastProcessedSeqNo, hashMapOfSeqAndPayload) = queueState
      val seqToProcess = lastProcessedSeqNo + 1L

      hashMapOfSeqAndPayload.get(seqToProcess) match {
        case None => queueStatus
        case Some(payload) =>
          messagingService.sendMessages(payload)
          dequeueAfterSuccessfulSent(seqToProcess, hashMapOfSeqAndPayload - (seqToProcess))
      }
    }

    Message(payload) match {
      case Left(error) => System.out.println("Non parsible Message received: " + error.message) //ignores
        queueStatus
      case Right(message) =>   System.out.println("Message received: " + payload)
        dequeueAfterSuccessfulSent(lastProcessedSeqNo,
          hashMapOfSeqAndPayload + (message.seqNo -> message))
    }
  }
}

