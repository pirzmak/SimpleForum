package repositories.slick

import models.{Post, PostId}
import repositories.interfaces.PostsRepository
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class PostsRepositorySlickImpl(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends PostsRepository with Db with PostsTable {

  import config.profile.api._

  override def init(initValues: Seq[Post] = Seq.empty): Future[Unit] =
    if (!tableExists("asd")) {
      db.run(
        DBIOAction.seq(
          posts.schema.create,
          posts ++= initValues
        ))
    } else {
      Future.successful()
    }

  override def drop(): Future[Unit] =
    db.run(
      DBIOAction.seq(
        posts.schema.drop
      ))

  override def createNew(post: Post): Future[PostId] =
    db.run(posts returning posts.map(_.id) += post)
      .map(id => post.copy(id = Some(PostId(id))).id.get)

  override def update(postId: PostId, newMessage: String): Future[PostId] = {
    val query = for (post <- posts if post.id === postId.value)
      yield post.text
    db.run(query.update(newMessage)).map(_ => postId)
  }

  override def delete(postId: PostId): Future[Boolean] =
    db.run(posts.filter(_.id === postId.value).delete) map { _ > 0 }

  override def getById(postId: PostId): Future[Option[Post]] =
    db.run(posts.filter(_.id === postId.value).result.headOption)

  override def getAll(actualPost: PostId, beforeNo: Int, afterNo: Int): Future[Seq[Post]] =
    {
      val before = posts.sortBy(_.id.desc).filter(_.id <= actualPost.value).take(beforeNo + 1)
      val after = posts.sortBy(_.id).filter(_.id > actualPost.value).take(afterNo)
      db.run((before ++ after).sortBy(_.id).result)
    }
}

