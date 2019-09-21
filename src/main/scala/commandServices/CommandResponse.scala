package commandServices

import model.{PostSecret, TopicId}

trait CommandResponse
case class TopicCommandResponse(topicId: TopicId) extends CommandResponse
case class PostCommandResponse(postSecret: PostSecret) extends CommandResponse

case class CommandFailure(msg: String)

object CommandFailure {
  val TopicIdNotFoundFailure = CommandFailure("TopicId not found")
  val IncorrectPostSecretFailure = CommandFailure("Incorrect post secret")
}
