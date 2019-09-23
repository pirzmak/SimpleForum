package commandServices

import scala.concurrent.{ExecutionContext, Future}

import confguration.ServerConfig
import model._
import repositories.interfaces.{PostsRepository, TopicsRepository}

class ForumCommandService(topicsRepository: TopicsRepository,
                          postsRepository: PostsRepository,
                          serverConfig: ServerConfig)(implicit ec: ExecutionContext) {
  val commandValidator = new CommandValidator(topicsRepository: TopicsRepository,
    postsRepository: PostsRepository, serverConfig.validationConfig)

  def createNewTopic(title: String, message: String, creator: User): Future[Either[CommandFailure, TopicCommandResponse]] = {
    val validationResult = for {
      v1 <- commandValidator.validateTopicTitle(title)
      v2 <- commandValidator.validatePostMessage(message)
      v3 <- commandValidator.validateEmail(creator.email)
      v4 <- commandValidator.validateNickname(creator.nickName)
    } yield (v1 ++ v2 ++ v3 ++ v4).reduceOption(_::_)

    validationResult flatMap {
      case Some(failure) => Future.successful(Left(failure))
      case None =>
        topicsRepository.createNew(Topic(title, message, creator)).
                map(topicId => Right(TopicCommandResponse(topicId)))
    }
  }

  def createNewPost(topicId: TopicId, message: String, creator: User): Future[Either[CommandFailure, PostCommandResponse]] = {
    val validationResult = for {
      v1 <- commandValidator.validateIfTopicExists(topicId)
      v2 <- commandValidator.validatePostMessage(message)
      v3 <- commandValidator.validateEmail(creator.email)
      v4 <- commandValidator.validateNickname(creator.nickName)
    } yield (v1 ++ v2 ++ v3 ++ v4).reduceOption(_::_)

    validationResult flatMap {
      case Some(failure) => Future.successful(Left(failure))
      case None =>
        postsRepository.createNew(Post(topicId, message, creator)).
          map(postId => Right(PostCommandResponse(PostSecretGenerator.getPostSecret(postId))))
    }
  }

  def updatePost(postSecret: PostSecret, newMessage: String): Future[Either[CommandFailure, PostCommandResponse]] = {
    val postId = PostSecretGenerator.getPostId(postSecret)

    val validationResult = for {
      v1 <- commandValidator.validateSecretPost(postSecret)
      v2 <- commandValidator.validatePostMessage(newMessage)
    } yield (v1 ++ v2).reduceOption(_::_)

    validationResult flatMap {
      case Some(failure) => Future.successful(Left(failure))
      case None =>
        postsRepository.update(postId.get, newMessage).map(response =>
          if(response) Right(PostCommandResponse(postSecret)) else Left(CommandFailure.PostIdNotFoundFailure))
    }
  }

  def deletePost(postSecret: PostSecret): Future[Either[CommandFailure, PostCommandResponse]] = {
    val postId = PostSecretGenerator.getPostId(postSecret)

    val validationResult = for {
      v1 <- commandValidator.validateSecretPost(postSecret)
    } yield v1

    validationResult flatMap {
      case Some(failure) => Future.successful(Left(failure))
      case None =>
        postsRepository.delete(postId.get).map(response =>
          if(response) Right(PostCommandResponse(postSecret)) else Left(CommandFailure.PostIdNotFoundFailure))
    }
  }
}
