package repositories.interfaces

import models.{Topic, TopicId}

trait TopicDAO {
  def init()
  def insert(topic: Topic): TopicId
  def getNewest(topicId: TopicId, offset: Int, limit: Int): Seq[Topic]
}
