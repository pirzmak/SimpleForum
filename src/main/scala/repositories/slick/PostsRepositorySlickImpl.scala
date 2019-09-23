package repositories.slick

import java.sql.{SQLException, Timestamp}
import java.time.LocalDateTime

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import model.{Post, PostId, TopicId}
import repositories.interfaces.PostsRepository
import slick.dbio.DBIOAction

class PostsRepositorySlickImpl(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends PostsRepository with Db with PostsTable {

  import config.profile.api._

  override def init(initValues: Seq[Post] = Seq.empty): Future[Unit] =
    if (!tableExists("POSTS")) {
      db.run(
        DBIO.seq(
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
      a <- posts returning posts.map(_.id) += post
      _ <- updateTopicAction(Some(post.topicId), Some(post.lastModification))
    } yield a

    db.run(action.transactionally).map(PostId)
  }

  override def update(postId: PostId, newMessage: String): Future[Boolean] = {
    val query = for {
      post <- posts if post.id === postId.value
    } yield (post.text, post.last_modified)

    val action = for {
      a <- query.update(newMessage, Timestamp.valueOf(LocalDateTime.now()))
      post <- posts.filter(_.id === postId.value).result.headOption
      _ <- updateTopicAction(post.map(_.topicId), post.map(_.lastModification))
    } yield a

    db.run(action.transactionally).map(_ > 0)
  }

  override def delete(postId: PostId): Future[Boolean] = {
    val action = for {
      a <- posts.filter(_.id === postId.value).delete
      post <- posts.filter(_.id === postId.value).result.headOption
      _ <- updateTopicAction(post.map(_.topicId), post.map(_.lastModification))
    } yield a

    db.run(action.transactionally).map(_ > 0)
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
          db.run((after ++ before).sortBy(_.id.desc).result)
        case None =>
          val posts = topicPosts.
            sortBy(_.id).
            take(afterNo + 1).
            sortBy(_.id.desc)
          db.run(posts.result)
      }
    }

  private def updateTopicAction(topicId: Option[TopicId], timestamp: Option[Timestamp]) = {
    topicId match {
      case Some(id) =>
        val query = for {
          topic <- topics if topic.id === id.value
        } yield topic.last_modified

        query.update(timestamp.get)
      case None => DBIOAction.successful()
    }
  }
}

