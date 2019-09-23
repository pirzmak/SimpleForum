package commandServices

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

import confguration.ValidationConfig
import model.{PostId, PostSecret, PostSecretGenerator, TopicId}
import repositories.interfaces.{PostsRepository, TopicsRepository}

class CommandValidator(topicsRepository: TopicsRepository,
                       postsRepository: PostsRepository,
                       validationConfig: ValidationConfig)(implicit ec: ExecutionContext) {

  def validateSecretPost(postSecret: PostSecret): Future[Option[CommandFailure]] = {
    validationResult(PostSecretGenerator.getPostId(postSecret).isEmpty, CommandFailure.IncorrectPostSecretFailure)
  }

  def validateIfPostExists(postId: PostId): Future[Option[CommandFailure]] = {
    postsRepository.getById(postId) flatMap { p =>
      validationResult(p.isEmpty, CommandFailure.PostIdNotFoundFailure)
    }
  }

  def validateIfTopicExists(topicId: TopicId): Future[Option[CommandFailure]] = {
    topicsRepository.getById(topicId) flatMap { t =>
      validationResult(t.isEmpty, CommandFailure.TopicIdNotFoundFailure)
    }
  }

  def validateEmail(email: String): Future[Option[CommandFailure]] = {
    val regex = new Regex(validationConfig.emailRegex)

    validationResult(!regex.matches(email), CommandFailure.EmailIncorrectValidationFailure)
  }

  def validateTopicTitle(title: String): Future[Option[CommandFailure]] = {
    val tooShort = title.length < validationConfig.topicTitleMinLength
    val tooLong = title.length > validationConfig.topicTitleMaxLength

    for {
      r1 <- validationResult(tooShort, CommandFailure.TopicTitleValidationTooShortFailure)
      r2 <- validationResult(tooLong, CommandFailure.TopicTitleValidationTooLongFailure)
    } yield (r1 ++ r2).headOption
  }

  def validatePostMessage(postMessage: String): Future[Option[CommandFailure]] = {
    val tooShort = postMessage.length < validationConfig.topicTitleMinLength
    val tooLong = postMessage.length > validationConfig.topicTitleMaxLength

    for {
      r1 <- validationResult(tooShort, CommandFailure.PostValidationTooShortFailure)
      r2 <- validationResult(tooLong, CommandFailure.PostValidationTooLongFailure)
    } yield (r1 ++ r2).headOption
  }

  def validateNickname(nick: String): Future[Option[CommandFailure]] = {
    val tooShort = nick.length < validationConfig.nickMinLength
    val tooLong = nick.length > validationConfig.nickMaxLength

    for {
      r1 <- validationResult(tooShort, CommandFailure.NicknameValidationTooShortFailure)
      r2 <- validationResult(tooLong, CommandFailure.NicknameValidationTooLongFailure)
    } yield (r1 ++ r2).headOption
  }

  private def validationResult(result: Boolean, onFailure: CommandFailure): Future[Option[CommandFailure]] = {
    if(result) Future.successful(Some(onFailure)) else Future.successful(None)
  }
}
