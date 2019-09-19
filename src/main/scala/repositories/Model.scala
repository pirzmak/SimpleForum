package repositories

import java.util.UUID

case class TopicId(id: UUID)
case class PostId(id: UUID)
case class UserId(id: UUID)

trait User
case class LoggedUser(id: UserId) extends User
case class NotLoggedUser(nickName: String, email: String) extends User

case class PostSecret(secret: String)

case class Topic(id: TopicId, text: String, creator: User)

case class Post(id: PostId, topicId: TopicId, secret: PostSecret, message: String, creator: User)
