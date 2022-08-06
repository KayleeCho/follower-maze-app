package maze.model

import scala.collection.mutable
import scala.util.Try

trait DeadLetterQueue[T <: MessageError] {
  val queue = new mutable.Queue[String]
  def enqueue(deadLetter: T): Unit
}


class DeadLetterQueueManager[T <: MessageError] extends DeadLetterQueue[T] {

  def enqueue(deadLetter: T): Unit = {
    println(s"delivery failed: ${deadLetter.message}")
    queue.enqueue(deadLetter.message)
  }
}

