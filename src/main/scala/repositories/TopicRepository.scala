package repositories

class TopicRepository {
  def createNew(topicName: String, message: String, creator: User): TopicId = ???
  def getListByActivity(offset: Int, limit: Int): List[Topic]  = ???
}
