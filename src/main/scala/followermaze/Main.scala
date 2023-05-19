package followermaze


object Main {
  def main(args: Array[String]): Unit = {

    val deadLetterQueueManager = new MessagingDeadLetterQueue[MessageError]
    val clientPool = new ClientPool( deadLetterQueueManager)
    val logger = new QueueStatusLogger
val messageProcessor = new PayloadProcessor( deadLetterQueueManager, clientPool)
    new Thread(new EventListener(messageProcessor, logger)).start()
    new Thread(new ClientListener(clientPool)).start()
  }
}
