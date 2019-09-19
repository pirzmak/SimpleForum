package commandServices

import models.User

trait Command
case class CreateNewTopic(topicName: String, message: String, creator: User)
case class CreateNewPost(message: String, creator: User)
case class UpdatePost(newMessage: String)
case class DeletePost()

