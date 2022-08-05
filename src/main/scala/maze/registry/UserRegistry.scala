package maze.registry

import java.io.{BufferedWriter, OutputStreamWriter}
import java.net.Socket

import maze.model.{DeadLetterQueue, DeliveryFailed}

import scala.collection.concurrent.TrieMap
import scala.util._

class UserRegistry(deadLetterQueue: DeadLetterQueue[DeliveryFailed]) {
  private val userPool = new TrieMap[Long, Socket]

  def addUser(userId: Long, clientSocket: Socket): Unit = userPool.put(userId, clientSocket)

  def numberOfUsersOnline: Int = userPool.size

  def broadcast(message: String): Unit =
    userPool.foreach { value =>
      write(message)(value._2)
        .foldLeft(deadLetterQueue.enqueue(DeliveryFailed(value._1, message)))(_)
    }

  def send(message: String)(whom: Long): Unit =
    userPool.get(whom).map(write(message)) match {
      case Some(socket) => socket
      case None => deadLetterQueue.enqueue(DeliveryFailed(whom,message))
    }


  private def write(message: String)(socket: Socket): Option[Unit] =
    Try {
      val writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
      writer.write(message + "\n")
      writer.flush()
    }.toOption
}

