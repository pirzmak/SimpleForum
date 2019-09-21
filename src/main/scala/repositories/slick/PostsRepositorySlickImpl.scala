package repositories.slick

import java.sql.Timestamp
import java.time.LocalDateTime

import model.{Post, PostId, TopicId}
import repositories.interfaces.PostsRepository
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class PostsRepositorySlickImpl(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends PostsRepository with Db with PostsTable {

  import config.profile.api._

  override def init(initValues: Seq[Post] = Seq.empty): Future[Unit] =
    if (!tableExists("POSTS")) {
      db.run(
        DBIOAction.seq(
          posts.schema.create,
          posts ++= initValues
        ))
    } else {
      Future.successful()
    }

  override def drop(): Future[Unit] =
    db.run(posts.schema.drop)

  override def createNew(post: Post): Future[PostId] = {
    val action = for {
      _ <- updateTopicAction(post.topicId)
      a <- posts returning posts.map(_.id) += post
    } yield a

    db.run(action.transactionally).map(PostId)
  }

  override def update(postId: PostId, newMessage: String): Future[PostId] = {
    val query = for {
      post <- posts if post.id === postId.value
    } yield post.text

    val action = for {
      post <- posts.filter(_.id === postId.value).result.head
      _ <- updateTopicAction(post.topicId)
      a <- query.update(newMessage)
    } yield a

    db.run(action.transactionally).map(_ => postId)
  }

  override def delete(postId: PostId): Future[Boolean] = {
    val action = for {
      post <- posts.filter(_.id === postId.value).result.head
      _ <- updateTopicAction(post.topicId)
      a <- posts.filter(_.id === postId.value).delete
    } yield a

    db.run(action.transactionally) map { _ > 0 }
  }

  override def getById(postId: PostId): Future[Option[Post]] =
    db.run(posts.filter(_.id === postId.value).result.headOption)

  override def getAll(topicId: TopicId, actualPost: Option[PostId], beforeNo: Int, afterNo: Int): Future[Seq[Post]] =
    {
      val topicPosts = posts.filter(_.topic_id === topicId.value)
      actualPost match {
        case Some(postId) =>
          val before = topicPosts.
            sortBy(_.id.desc).
            filter(_.id <= postId.value).
            take(beforeNo + 1)
          val after = topicPosts.
            sortBy(_.id).
            filter(_.id > postId.value).
            take(afterNo)
          db.run((before ++ after).sortBy(_.id).result)
        case None =>
          val posts = topicPosts.
            sortBy(_.id).
            take(afterNo + 1)
          db.run(posts.result)
      }
    }

  private def updateTopicAction(topicId: TopicId) = {
    val query = for {
      topic <- topics if topic.id === topicId.value
    } yield topic.last_modified

    query.update(Timestamp.valueOf(LocalDateTime.now()))
  }
}

