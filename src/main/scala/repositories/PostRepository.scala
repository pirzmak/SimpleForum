package repositories

class PostRepository {
  def createNew(topicId: TopicId, message: String, creator: User): (PostId, PostSecret) = ???
  def update(postSecret: PostSecret, newMessage: String): (PostId, PostSecret) = ???
  def delete(postSecret: PostSecret): (PostId, PostSecret) = ???
  def getList(actualPost: PostId, beforeNo: Int, afterNo: Int): List[Post]  = ???
}
