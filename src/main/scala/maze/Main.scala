package maze

import maze.model.{DeadLetterQueueManager, DeliveryError, ParseError}
import maze.registry.UserRegistry
import maze.servers.{ClientServer, EventServer}
import maze.service.MessageRouter

object Main {

  def main(args: Array[String]): Unit = {

    val userRegistry = new UserRegistry(new DeadLetterQueueManager[DeliveryError])

    new Thread(new EventServer(new MessageRouter(userRegistry), new DeadLetterQueueManager[ParseError])).start()
    new Thread(new ClientServer(userRegistry)).start()
  }

}