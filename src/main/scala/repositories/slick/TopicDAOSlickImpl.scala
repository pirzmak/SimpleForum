package repositories.slick

import models.{Topic, TopicId}
import repositories.interfaces.TopicDAO
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class TopicDAOSlickImpl(val config: DatabaseConfig[JdbcProfile])
  extends TopicDAO with Db with TopicsTable {

  override def init(): Unit = ???

  override def insert(topic: Topic): TopicId = ???

  override def getNewest(topicId: TopicId, offset: Int, limit: Int): Seq[Topic] = ???
}

