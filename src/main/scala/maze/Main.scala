package maze

import maze.model.{DeadLetterQueueService, DeliveryFailed}
import maze.registry.UserRegistry
import maze.servers.{ClientServer, EventServer}
import maze.service.MessagingService

object Main {

  def main(args: Array[String]): Unit = {

    val userRegistry = new UserRegistry(new DeadLetterQueueService[DeliveryFailed])

    new Thread(new EventServer(new MessagingService(userRegistry))).start()
    new Thread(new ClientServer(userRegistry)).start()
  }

}