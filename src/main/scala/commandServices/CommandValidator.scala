package commandServices

import confguration.ValidationConfig
import model.{PostId, PostSecret, PostSecretGenerator, TopicId}
import repositories.interfaces.{PostsRepository, TopicsRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

class CommandValidator(topicsRepository: TopicsRepository,
                       postsRepository: PostsRepository,
                       validationConfig: ValidationConfig)(implicit ec: ExecutionContext) {

  def validateSecretPost(postSecret: PostSecret): Option[CommandFailure] = {
    PostSecretGenerator.getPostId(postSecret) match {
      case None => Some(CommandFailure.IncorrectPostSecretFailure)
      case Some(_) => None
    }
  }

  def validateIfPostExists(postId: PostId): Future[Option[CommandFailure]] = {
    postsRepository.getById(postId) map {
      case None => Some(CommandFailure.PostIdNotFoundFailure)
      case Some(_) => None
    }
  }

  def validateIfTopicExists(topicId: TopicId): Future[Option[CommandFailure]] = {
    topicsRepository.getById(topicId).map {
      case None => Some(CommandFailure.TopicIdNotFoundFailure)
      case Some(_) => None
    }
  }

  def validateEmail(email: String): Option[CommandFailure] = {
    val regex = new Regex(validationConfig.emailRegex)
    if(regex.matches(email)) {
      None
    } else {
      Some(CommandFailure.EmailIncorrectValidationFailure)
    }
  }

  def validateTopicTitle(title: String): Option[CommandFailure] = {
    if(title.length < validationConfig.topicTitleMinLength) {
      Some(CommandFailure.TopicTitleValidationTooShortFailure)
    } else if (title.length > validationConfig.topicTitleMaxLength) {
      Some(CommandFailure.TopicTitleValidationTooLongFailure)
    } else {
      None
    }
  }

  def validatePostMessage(postMessage: String): Option[CommandFailure] = {
    if(postMessage.length < validationConfig.postMinLength) {
      Some(CommandFailure.PostValidationTooShortFailure)
    } else if (postMessage.length > validationConfig.postMaxLength) {
      Some(CommandFailure.PostValidationTooLongFailure)
    } else {
      None
    }
  }
}
