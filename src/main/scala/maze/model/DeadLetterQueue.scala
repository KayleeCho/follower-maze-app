package maze.model

import scala.collection.mutable
import scala.util.Try

trait DeadLetterQueue[T <: DeadLetter] {
  val queue = new mutable.Queue[String]
  def enqueue(deadLetter: T): Unit
}


class DeadLetterQueueManager[T <: DeadLetter] extends DeadLetterQueue[T] {

  def enqueue(deadLetter: T): Unit = {
    println(s"delivery failed: ${deadLetter.message}")
    queue.enqueue(deadLetter.message)
  }

}

sealed trait DeadLetter {
  val message: String
}

case class DeliveryFailedMessage(message: String) extends DeadLetter
