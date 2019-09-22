package commandServices

import confguration.ServerConfig
import model._
import repositories.interfaces.{PostsRepository, TopicsRepository}

import scala.concurrent.{ExecutionContext, Future}

class ForumCommandService(topicsRepository: TopicsRepository,
                          postsRepository: PostsRepository,
                          serverConfig: ServerConfig)(implicit ec: ExecutionContext) {
  val commandValidator = new CommandValidator(topicsRepository: TopicsRepository,
    postsRepository: PostsRepository, serverConfig.validationConfig)

  def createNewTopic(title: String, message: String, creator: User): Future[Either[CommandFailure, TopicCommandResponse]] = {
    val validationResult: Option[CommandFailure] = for {
      v1 <- commandValidator.validateTopicTitle(title)
      v2 <- commandValidator.validatePostMessage(message)
      v3 <- commandValidator.validateEmail(creator.email)
    } yield v1 :: v2 :: v3

    validationResult match {
      case Some(failure) => Future.successful(Left(failure))
      case None =>
        topicsRepository.createNew(Topic(title, message, creator)).
                map(topicId => Right(TopicCommandResponse(topicId)))
    }
  }

  def createNewPost(topicId: TopicId, message: String, creator: User): Future[Either[CommandFailure, PostCommandResponse]] = {
    val validationResult = for {
      v1 <- commandValidator.validateIfTopicExists(topicId)
      v2 <- Future.successful(commandValidator.validatePostMessage(message))
      v3 <- Future.successful(commandValidator.validateEmail(creator.email))
    } yield (v1 ++ v2 ++ v3).reduceOption((a,b) => a :: b)

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
      v1 <- Future.successful(commandValidator.validateSecretPost(postSecret))
      v2 <- Future.successful(commandValidator.validatePostMessage(newMessage))
      v3 <- if(postId.isDefined) commandValidator.validateIfPostExists(postId.get) else Future.successful(None)
    } yield (v1 ++ v2 ++ v3).reduceOption((a,b) => a :: b)

    validationResult flatMap {
      case Some(failure) => Future.successful(Left(failure))
      case None =>
        postsRepository.update(postId.get, newMessage).
          map(_ => Right(PostCommandResponse(postSecret)))
    }
  }

  def deletePost(postSecret: PostSecret): Future[Either[CommandFailure, PostCommandResponse]] = {
    val postId = PostSecretGenerator.getPostId(postSecret)

    val validationResult = for {
      v1 <- Future.successful(commandValidator.validateSecretPost(postSecret))
      v2 <- if(postId.isDefined) commandValidator.validateIfPostExists(postId.get) else Future.successful(None)
    } yield (v1 ++ v2).reduceOption((a,b) => a :: b)

    validationResult flatMap {
      case Some(failure) => Future.successful(Left(failure))
      case None =>
        postsRepository.delete(postId.get).
          map(_ => Right(PostCommandResponse(postSecret)))
    }
  }
}
