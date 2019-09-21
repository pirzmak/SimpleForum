package commandServices

import model.User

trait Command
case class CreateNewTopic(title: String, message: String, creator: User) extends Command
case class CreateNewPost(message: String, creator: User) extends Command
case class UpdatePost(newMessage: String) extends Command
case class DeletePost() extends Command

