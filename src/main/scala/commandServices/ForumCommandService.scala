package commandServices

import models.{PostId, PostSecret, TopicId, User}
import routes.FailureResponse

case class TopicCommandResponse(topicId: TopicId)
case class PostCommandResponse(postId: PostId, postSecret: PostSecret)

class ForumCommandService() {
  def createNewTopic(topicName: String, message: String, creator: User): Either[FailureResponse, TopicCommandResponse] = ???
  def createNewPost(topicId: TopicId, message: String, creator: User): Either[FailureResponse, PostCommandResponse] = ???
  def updatePost(postSecret: PostSecret, newMessage: String): Either[FailureResponse, PostCommandResponse] = ???
  def deletePost(postSecret: PostSecret): Either[FailureResponse, PostCommandResponse] = ???
}
