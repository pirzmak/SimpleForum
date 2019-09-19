package repositories

import models.{Topic, TopicId, User}
import repositories.interfaces.TopicDAO

class TopicRepository(topicDAO: TopicDAO) {
  def createNew(topicName: String, message: String, creator: User): TopicId = ???
  def getListByLastActivity(offset: Int, limit: Int): List[Topic]  = ???
}

