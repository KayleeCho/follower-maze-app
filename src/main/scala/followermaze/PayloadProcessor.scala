package followermaze


class PayloadProcessor(deadLetterQueue: DeadLetterQueue[MessageError], clientPool: ClientPool) {
  val followersRegistry = new FollowersRegistry

  def process(message: String): Unit = {
    val parsedMessage = parseMessage(message)
    parsedMessage match {
      case None => deadLetterQueue.enqueue(ParseError(message))
      case Some(parsed) => sendMessage(parsed)
    }
  }

  def parseMessage(message: String): Option[Message] = {
    Message(message) match {
      case Left(error) =>
        System.out.println("Non parsible Message received: " + error.message)
        None
      case Right(parsedMessage) => Some(parsedMessage)
    }

  }
  def sendMessage(message: Message): Unit = {
    message match {
      case Follow(_, fromUserId, toUserId, payload) =>
        followersRegistry.follow(fromUserId, toUserId)
        clientPool.send(payload)(toUserId)
      case Unfollow(_, fromUserId, toUserId, _) =>
        followersRegistry.unfollow(fromUserId, toUserId)
      case Private(_, _, toUserId, payload) =>
        clientPool.send(payload)(toUserId)
      case Broadcast(_, payload) =>
        clientPool.broadcast(payload)
      case Status(_, fromUserId, payload) =>
        val followers = followersRegistry.getFollowersOf(fromUserId)
        followers.foreach(clientPool.send(payload))
    }
  }
}

