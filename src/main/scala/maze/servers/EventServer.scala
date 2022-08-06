package maze.servers

import java.io.{BufferedReader, InputStreamReader}
import java.net.ServerSocket

import maze.model._
import maze.service.MessageRouter

import scala.annotation.tailrec

class EventServer(messagingService: MessageRouter, deadLetterQueue: DeadLetterQueue[ParseError]) extends Runnable {
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

  def updateEventQueue(currentQueue: EventQueue)(
    payload: String): EventQueue = {
    val (lastProcessedSeqNo, mapOfSeqAndPayload) = currentQueue

    @tailrec
    def dequeueAfterSuccessfulSent(queueStateInRec: EventQueue): EventQueue = {
      val (lastProcessedSeqNo, mapOfSeqAndPayload) = queueStateInRec
      val seqToProcess = lastProcessedSeqNo + 1L

      mapOfSeqAndPayload.get(seqToProcess) match {
        case None => currentQueue
        case Some(payload) =>
          messagingService.sendMessages(payload)
          dequeueAfterSuccessfulSent(seqToProcess, mapOfSeqAndPayload - (seqToProcess))
      }
    }

    Message(payload) match {
      case Left(error) => System.out.println("Non parsible Message received: " + error.message)
        deadLetterQueue.enqueue(ParseError(payload))//ignores
        currentQueue
      case Right(message) =>   System.out.println("Message received: " + payload)
        dequeueAfterSuccessfulSent(lastProcessedSeqNo,
          mapOfSeqAndPayload + (message.seqNo -> message))
    }
  }
}

