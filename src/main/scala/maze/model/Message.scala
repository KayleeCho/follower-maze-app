package maze.model

sealed trait Message {
  def seqNo: Long
  def payload: String

}

case class Follow(override val seqNo: Long, from: Long, to: Long, override val payload: String) extends Message
case class Unfollow(override val seqNo: Long, from: Long, to: Long, override val payload: String) extends Message
case class Private(override val seqNo: Long, to: Long, override val payload: String) extends Message
case class Broadcast(override val seqNo: Long, override val payload: String) extends Message
case class Status(override val seqNo: Long, from: Long, override val payload: String) extends Message

object Message {
  def apply(payload: String): Either[ParseError, Message] =
    payload.split("\\|").toList match {
      case List(Converter(id), "F", Converter(from), Converter(to)) => Right(Follow(id, from, to, payload))
      case List(Converter(id), "U", Converter(from), Converter(to)) => Right(Unfollow(id, from, to, payload))
      case List(Converter(id), "B") => Right(Broadcast(id, payload))
      case List(Converter(id), "P", Converter(from), Converter(to)) => Right(Private(id, to, payload))
      case List(Converter(id), "S", Converter(from)) => Right(Status(id, from, payload))
      case _: Any => Left(ParseError(payload))
    }
}

object Converter {
  def unapply(str: String): Option[Int] =
    try {
      Some(str.toInt)
    } catch {
      case _: NumberFormatException => None
    }
}

sealed trait MessageError

case class ParseError(message: String) extends MessageError
