package maze.registry

import java.io.{BufferedWriter, OutputStreamWriter}
import java.net.Socket

import maze.model.{DeadLetterQueue, DeliveryFailedMessage}

import scala.collection.concurrent.TrieMap
import scala.util._

class UserRegistry(deadLetterQueue: DeadLetterQueue[DeliveryFailedMessage]) {
  private val userPool = new TrieMap[Long, Socket]

  def add(userId: Long, clientSocket: Socket): Unit = userPool.put(userId, clientSocket)

  def numberOfUsersOnline: Int = userPool.size

  def broadcast(message: String): Unit =
    userPool.values.foreach { socket =>
      write(message)(socket) match {
        case Some(_) => ()
        case None => deadLetterQueue.enqueue(DeliveryFailedMessage(message))
      }
    }


  def send(message: String)(to: Long): Unit = {

    userPool.get(to) match {
      case Some(socket) => write(message)(socket).getOrElse(deadLetterQueue.enqueue(DeliveryFailedMessage(message)))
      case None => deadLetterQueue.enqueue(DeliveryFailedMessage(message))
    }

  }


  private def write(message: String)(socket: Socket): Option[Unit] =
    Try {
      val writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
      writer.write(message + "\n")
      writer.flush()
    }.toOption
}

