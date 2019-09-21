package commandServices

import model.{Post, PostId, PostSecret, PostSecretGenerator, Topic, TopicId, User}
import repositories.interfaces.{PostsRepository, TopicsRepository}

import scala.concurrent.{ExecutionContext, Future}

class ForumCommandService(topicsRepository: TopicsRepository,
                          postsRepository: PostsRepository)(implicit ec: ExecutionContext) {
  def createNewTopic(topicName: String, message: String, creator: User): Future[Either[CommandFailure, TopicCommandResponse]] = {
    topicsRepository.createNew(Topic(None, topicName, message, creator)).
      map(topicId => Right(TopicCommandResponse(topicId)))
  }

  def createNewPost(topicId: TopicId, message: String, creator: User): Future[Either[CommandFailure, PostCommandResponse]] = {
    topicsRepository.getById(topicId).flatMap {
      case Some(_) =>
        postsRepository.createNew(Post(None, topicId, message, creator)).
          map(postId => Right(PostCommandResponse(PostSecretGenerator.getPostSecret(postId))))
      case None => Future.successful(Left(CommandFailure.TopicIdNotFoundFailure))
    }
  }

  def updatePost(postSecret: PostSecret, newMessage: String): Future[Either[CommandFailure, PostCommandResponse]] = {
    checkPostSecret(postSecret) { postId =>
      postsRepository.update(postId, newMessage).
        map(_ => Right(PostCommandResponse(postSecret)))
    }
  }

  def deletePost(postSecret: PostSecret): Future[Either[CommandFailure, PostCommandResponse]] = {
    checkPostSecret(postSecret) { postId =>
      postsRepository.delete(postId).
        map(_ => Right(PostCommandResponse(postSecret)))
    }
  }

  private def checkPostSecret(postSecret: PostSecret)
                             (onSuccess: PostId => Future[Either[CommandFailure, PostCommandResponse]]): Future[Either[CommandFailure, PostCommandResponse]] =
    PostSecretGenerator.getPostId(postSecret) match {
      case None => Future.successful(Left(CommandFailure.IncorrectPostSecretFailure))
      case Some(postId) => postsRepository.getById(postId) flatMap {
        case None => Future.successful(Left(CommandFailure.IncorrectPostSecretFailure))
        case Some(_) => onSuccess(postId)
    }
  }
}
