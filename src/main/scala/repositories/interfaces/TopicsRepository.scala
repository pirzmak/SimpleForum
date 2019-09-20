package repositories.interfaces

import models.{Topic, TopicId, User}

import scala.concurrent.Future

trait TopicsRepository {
  def init(initValues: Seq[Topic])
  def createNew(topic: Topic): Future[TopicId]
  def getListByLastActivity(topicId: TopicId, offset: Int, limit: Int): Future[List[Topic]]
}
