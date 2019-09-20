package repositories.slick

import java.time.LocalDateTime

import models.{Topic, TopicId, User}
import repositories.interfaces.TopicsRepository
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class TopicsRepositorySlickImpl(val config: DatabaseConfig[JdbcProfile])
  extends TopicsRepository with Db with TopicsTable {

  import config.profile.api._

  override def init(initValues: Seq[Topic] = Seq.empty): Unit =
    if(!tableExists("asd")) {
      db.run(
        DBIOAction.seq(
          topics.schema.create,
          topics ++= initValues
        ))
    }

  override def createNew(topic: Topic): Future[TopicId] = ???

  override def getListByLastActivity(topicId: TopicId, offset: Int, limit: Int): Future[List[Topic]] = ???
}

