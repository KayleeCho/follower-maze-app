package maze.service

import maze.registry.{FollowersRegistry, UserRegistry}
import maze.model._

class MessageRouter(userHandler: UserRegistry) {
  val followersRegistry = new FollowersRegistry

  def sendMessages(message: Message): Unit = message match {
    case Follow(_, fromUserId, toUserId, payload) =>
      followersRegistry.follow(fromUserId, toUserId)
      userHandler.send(payload)(toUserId)
    case Unfollow(_, fromUserId, toUserId, _) =>
      followersRegistry.unfollow(fromUserId, toUserId)
    case Private(_, toUserId, payload) =>
      userHandler.send(payload)(toUserId)
    case Broadcast(_, payload) =>
      userHandler.broadcast(payload)
    case Status(_, fromUserId, payload) =>
      val followers = followersRegistry.getFollowersOf(fromUserId)
      followers.foreach(userHandler.send(payload))
  }
}
