package followermaze

import java.io.{BufferedWriter, OutputStreamWriter}
import java.net.Socket
import scala.collection.concurrent.TrieMap
import scala.util.Try

class ClientPool(deadLetterQueue: DeadLetterQueue[MessageError]) {
  private val userPool = new TrieMap[Long, Socket]

  def add(userId: Long, clientSocket: Socket): Unit = userPool.put(userId, clientSocket)

  def numberOfUsersOnline: Int = userPool.size

  def broadcast(message: String): Unit =
    userPool.foreach { client =>
      write(message)(client._2) match {
        case Some(_) => ()
        case None => deadLetterQueue.enqueue(DeliveryError(client._1, message))
      }
    }


  def send(message: String)(to: Long): Unit = {

    userPool.get(to) match {
      case Some(socket) => write(message)(socket).getOrElse(deadLetterQueue.enqueue(DeliveryError(to, message)))
      case None => deadLetterQueue.enqueue(DeliveryError(to, message))
    }
  }


  private def write(message: String)(socket: Socket): Option[Unit] =
    Try {val writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
      writer.write(message + "\n")
      writer.flush()
    }.toOption
}
