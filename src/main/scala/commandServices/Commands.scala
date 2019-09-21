package commandServices

import models.User

trait Command
case class CreateNewTopic(topicName: String, message: String, creator: User) extends Command
case class CreateNewPost(message: String, creator: User) extends Command
case class UpdatePost(newMessage: String) extends Command
case class DeletePost() extends Command

