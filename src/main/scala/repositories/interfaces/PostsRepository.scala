package repositories.interfaces

import model.{Post, PostId, TopicId}

import scala.concurrent.Future

trait PostsRepository {
  def init(initValues: Seq[Post]): Future[Unit]
  def drop(): Future[Unit]
  def createNew(post: Post): Future[PostId]
  def update(postId: PostId, newMessage: String): Future[Boolean]
  def delete(postId: PostId): Future[Boolean]
  def getById(postId: PostId): Future[Option[Post]]
  def getAll(topicId: TopicId, actualPost: Option[PostId], beforeNo: Int, afterNo: Int): Future[Seq[Post]]
}
