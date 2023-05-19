package followermaze

sealed trait MessageError {
  val message: String
}

case class DeliveryError(to: Long, message: String) extends MessageError
case class ParseError(message: String) extends MessageError