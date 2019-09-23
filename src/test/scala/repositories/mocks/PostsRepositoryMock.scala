package repositories.mocks

import java.sql.Timestamp
import java.time.LocalDateTime

import model.{Post, PostId, TopicId}
import repositories.interfaces.PostsRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostsRepositoryMock(topics: TopicsRepositoryMock) extends PostsRepository {
  var db: Map[PostId, Post] = Map.empty

  override def init(initValues: Seq[Post]): Future[Unit] = {
    db = initValues.zipWithIndex.map(p => (PostId(p._2 + 1), p._1.copy(id = Some(PostId(p._2  + 1))))).toMap
    Future.successful()
  }

  override def drop(): Future[Unit] = {
    db = Map.empty
    Future.successful()
  }

  override def createNew(post: Post): Future[PostId] = {
    updateTopic(post.topicId) {
      val nextId = PostId(db.lastOption.map(_._1.value).getOrElse(0) + 1)
      db = db + (nextId -> post.copy(id = Some(nextId)))
      Future.successful(nextId)
    }
  }

  override def update(postId: PostId, newMessage: String): Future[Boolean] = {
    db.get(postId) match {
      case None => Future.failed(throw new IllegalArgumentException)
      case Some(post) =>
        updateTopic(post.topicId) {
          db = db + (postId -> post.copy(message = newMessage))
          Future.successful(true)
        }
    }
  }

  override def delete(postId: PostId): Future[Boolean] = {
    db.get(postId) match {
      case None => Future.failed(throw new IllegalArgumentException)
      case Some(post) =>
        updateTopic(post.topicId) {
          db = db - postId
          Future.successful(true)
        }
    }
  }

  override def getById(postId: PostId): Future[Option[Post]] = {
    Future.successful(db.get(postId))
  }

  override def getAll(topicId: TopicId, actualPost: Option[PostId], beforeNo: Int, afterNo: Int): Future[Seq[Post]] = {
    val list = db.toList.map(_._2).filter(_.topicId == topicId).sortBy(_.id.get.value).reverse
    val (after, before) = actualPost match {
      case Some(postId) =>
        list.span(_.id.get != postId)
      case None =>
        list.splitAt(1)
    }
    Future.successful(after.takeRight(afterNo) ++ before.take(beforeNo + 1))
  }

  private def updateTopic[T](topicId: TopicId)(handler: Future[T]): Future[T] = {
    topics.getById(topicId) flatMap {
      case None => Future.failed(throw new IllegalArgumentException)
      case Some(topic) =>
        topics.db = topics.db + (topicId -> topic.copy(lastModified = Timestamp.valueOf(LocalDateTime.now())))
        handler
    }
  }
}
