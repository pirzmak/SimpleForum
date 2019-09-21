package repositories.slick

import models.{Topic, TopicId}
import repositories.interfaces.TopicsRepository
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class TopicsRepositorySlickImpl(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends TopicsRepository with Db with TopicsTable {

  import config.profile.api._

  override def init(initValues: Seq[Topic] = Seq.empty): Future[Unit] =
    if(!tableExists("TOPICS")) {
      db.run(
        DBIOAction.seq(
          topics.schema.create,
          topics ++= initValues
        ))
    } else Future.successful()

  override def drop(): Future[Unit] =
    db.run(topics.schema.drop)

  override def createNew(topic: Topic): Future[TopicId] =
    db.run(topics returning topics.map(_.id) += topic)
      .map(id => topic.copy(id = Some(TopicId(id))).id.get)

  override def getById(topicId: TopicId): Future[Option[Topic]] =
    db.run(topics.filter(_.id === topicId.value).result.headOption)

  override def getAllSortedByLastActivity(offset: Int, limit: Int): Future[Seq[Topic]] =
    db.run(topics.sortBy(_.last_modified.desc).drop(offset).take(limit).result)
}

