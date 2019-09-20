package repositories.interfaces

import models.{Post, PostId}

import scala.concurrent.Future

trait PostsRepository {
  def init(initValues: Seq[Post]): Future[Unit]
  def drop(): Future[Unit]
  def createNew(post: Post): Future[PostId]
  def update(postId: PostId, newMessage: String): Future[PostId]
  def delete(postId: PostId): Future[Boolean]
  def getById(postId: PostId): Future[Option[Post]]
  def getAll(actualPost: PostId, beforeNo: Int, afterNo: Int): Future[Seq[Post]]
}
