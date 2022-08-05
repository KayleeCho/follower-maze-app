package maze.model

import scala.util.Try

sealed trait Message {
  def seqNo: Long
  def payload: String

}

case class Follow(override val seqNo: Long, fromUserId: Long, toUserId: Long, override val payload: String) extends Message
case class Unfollow(override val seqNo: Long, fromUserId: Long, toUserId: Long, override val payload: String) extends Message
case class Private(override val seqNo: Long, toUserId: Long, override val payload: String) extends Message
case class Broadcast(override val seqNo: Long, override val payload: String) extends Message
case class Status(override val seqNo: Long, fromUserId: Long, override val payload: String) extends Message

object Message {

  def parse(payload: String): Either[ParseFailed, Message] = {
    val messageList = payload.split("\\|").toList
    for {
      seqNo <- Try(messageList(0).toLong).toEither.left.map(_ => ParseFailed(payload))
      message <- messageList(1) match {
        case "F" => parseFollow(messageList, seqNo, payload)
        case "U" => parseUnfollow(messageList, seqNo, payload)
        case "P" => parsePrivate(messageList, seqNo, payload)
        case "B" => parseBroadcast(messageList, seqNo, payload)
        case "S" => parseStatus(messageList, seqNo, payload)
        case _ => Left(ParseFailed(payload))
      }
    } yield message
  }

  private def parseFollow(messageList: List[String], seqNo: Long, payload: String): Either[ParseFailed, Message] =
    for {
      fromUserId <- parseFrom(messageList, payload)
      toUserId <- parseTo(messageList, payload)
    } yield Follow(seqNo, fromUserId, toUserId, payload)

  private def parseUnfollow(messageList: List[String], seqNo: Long, payload: String): Either[ParseFailed, Message] =
    for {
      fromUserId <- parseFrom(messageList, payload)
      toUserId <- parseTo(messageList, payload)
    } yield Unfollow(seqNo, fromUserId, toUserId, payload)

  private def parsePrivate(messageList: List[String], seqNo: Long, payload: String): Either[ParseFailed, Message] =
    for {
      toUserId <- parseTo(messageList, payload)
    } yield Private(seqNo, toUserId, payload)

  private def parseBroadcast(messageList: List[String], seqNo: Long, payload: String): Either[ParseFailed, Message] =
    for {
      _ <- validate(messageList, 2, payload)
    } yield Broadcast(seqNo, payload)

  private def parseStatus(messageList: List[String], seqNo: Long, payload: String): Either[ParseFailed, Message] =
    for {
      fromUserId <- parseFrom(messageList, payload)
    } yield Status(seqNo, fromUserId, payload)

  private def validate(messageList: List[String], requiredLength: Int, payload: String): Either[ParseFailed, Unit] =
    messageList.size == requiredLength match {
      case true => Right ()
      case _ => Left(ParseFailed(payload))
    }

  private def parseFrom(messageList: List[String], payload: String): Either[ParseFailed, Long] =
    Try(messageList(2).toLong).toEither.left.map(_ => ParseFailed(payload))

  private def parseTo(messageList: List[String], payload: String): Either[ParseFailed, Long] =
    Try(messageList(3).toLong).toEither.left.map(_ => ParseFailed(payload))

}

sealed trait MessageError

case class ParseFailed(message: String) extends MessageError