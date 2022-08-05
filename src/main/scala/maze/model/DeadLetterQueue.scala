package maze.model

import scala.collection.mutable
import scala.util.Try

trait DeadLetterQueue[T <: DeadLetter] {
  val queue = new mutable.Queue[String]
  def enqueue(deadLetter: T): Unit
  def hasNext: Boolean
  def dequeue(): Option[String]
  def popAll: Seq[String]
}


class DeadLetterQueueService[T <: DeadLetter] extends DeadLetterQueue[T] {

  def enqueue(deadLetter: T): Unit = {
    println(s"delivery failed: ${deadLetter.message}")
    queue.enqueue(deadLetter.message)
  }

  def hasNext: Boolean = {
    println("queue is not empty")
    queue.nonEmpty
  }


  def dequeue(): Option[String] = {
    Try(queue.dequeue).toOption
  }

  def popAll: Seq[String] = {
    queue.dequeueAll(_ => true)
  }
}

sealed trait DeadLetter {
  val receiver: Long
  val message: String

}

case class DeliveryFailed(receiver: Long, message: String) extends DeadLetter
