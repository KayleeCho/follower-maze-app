package maze

import maze.model.{DeadLetterQueueManager, DeliveryFailedMessage}
import maze.registry.UserRegistry
import maze.servers.{ClientServer, EventServer}
import maze.service.MessagingService

object Main {

  def main(args: Array[String]): Unit = {

    val userRegistry = new UserRegistry(new DeadLetterQueueManager[DeliveryFailedMessage])

    new Thread(new EventServer(new MessagingService(userRegistry))).start()
    new Thread(new ClientServer(userRegistry)).start()
  }

}