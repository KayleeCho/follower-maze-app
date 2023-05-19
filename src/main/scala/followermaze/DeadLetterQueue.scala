package followermaze

import scala.collection.mutable

trait DeadLetterQueue[T <: MessageError] {
  val queue = new mutable.Queue[String]
  def enqueue(deadLetter: T): Unit
}


class MessagingDeadLetterQueue[T <: MessageError] extends DeadLetterQueue[T] {

  def enqueue(deadLetter: T): Unit = {
    deadLetter match {
      case DeliveryError( seqNum, message) => println(s"delivery failed: seqNum: ${seqNum}, message: ${message}. we will try to send the message when user is online " )
        queue.enqueue(deadLetter.message)
      case ParseError(message) => println(s"parse failed. wrong format ${message} ")
        queue.enqueue(deadLetter.message)
    }

  }
}
