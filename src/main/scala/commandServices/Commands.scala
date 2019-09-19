package commandServices

import repositories.NotLoggedUser

trait Command
case class CreateNewTopicByNotLoggedUser(topicName: String, message: String, creator: NotLoggedUser)
case class CreateNewPostByNotLoggedUser(message: String, creator: NotLoggedUser)
case class UpdatePost(newMessage: String)
case class DeletePost()

