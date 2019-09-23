package commandServices

import model.{PostSecret, TopicId}

trait CommandResponse
case class TopicCommandResponse(topicId: TopicId) extends CommandResponse
case class PostCommandResponse(postSecret: PostSecret) extends CommandResponse

case class CommandFailure(msg: Seq[String]) {
  def ::(c: CommandFailure): CommandFailure =
    CommandFailure(this.msg ++ c.msg)
}

object CommandFailure {
  val TopicIdNotFoundFailure = CommandFailure(Seq("TopicId not found"))
  val PostIdNotFoundFailure = CommandFailure(Seq("PostId not found"))
  val IncorrectPostSecretFailure = CommandFailure(Seq("Incorrect post secret"))

  val PostValidationTooShortFailure = CommandFailure(Seq("Post message is too short"))
  val PostValidationTooLongFailure = CommandFailure(Seq("Post message is too long"))
  val TopicTitleValidationTooShortFailure = CommandFailure(Seq("Topic title is too short"))
  val TopicTitleValidationTooLongFailure = CommandFailure(Seq("Topic title is too long"))
  val NicknameValidationTooShortFailure = CommandFailure(Seq("Nickname is too short"))
  val NicknameValidationTooLongFailure = CommandFailure(Seq("Nickname title is too long"))
  val EmailIncorrectValidationFailure = CommandFailure(Seq("Incorrect user email"))
}
