package repositories.interfaces

import models.{PostId, Topic, TopicId}

import scala.concurrent.Future

trait TopicsRepository {
  def init(initValues: Seq[Topic]): Future[Unit]
  def drop(): Future[Unit]
  def createNew(topic: Topic): Future[TopicId]
  def getById(topicId: TopicId): Future[Option[Topic]]
  def getAllSortedByLastActivity(offset: Int, limit: Int): Future[Seq[Topic]]
}
