package routes

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import commandServices.{CreateNewPostByNotLoggedUser, CreateNewTopicByNotLoggedUser, PostCommandResponse, TopicCommandResponse, UpdatePost}
import repositories.{NotLoggedUser, Post, PostId, PostSecret, Topic, TopicId, User}
import spray.json._

trait ForumJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object UUIDJsonFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue): UUID = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }
  implicit val topicIdJsonFormat: RootJsonFormat[TopicId] = jsonFormat1(TopicId)

  implicit val postIdJsonFormat: RootJsonFormat[PostId] = jsonFormat1(PostId)

  implicit val postSecretJsonFormat: RootJsonFormat[PostSecret] = jsonFormat1(PostSecret)

  implicit val notLoggedUserJsonFormat: RootJsonFormat[NotLoggedUser] = jsonFormat2(NotLoggedUser)

  implicit val createNewTopicByNotLoggedUserJsonFormat: RootJsonFormat[CreateNewTopicByNotLoggedUser] = jsonFormat3(CreateNewTopicByNotLoggedUser)

  implicit val createNewPostByNotLoggedUserJsonFormat: RootJsonFormat[CreateNewPostByNotLoggedUser] = jsonFormat2(CreateNewPostByNotLoggedUser)

  implicit val updatePost: RootJsonFormat[UpdatePost] = jsonFormat1(UpdatePost)

  implicit val topicCommandResponseJsonFormat: RootJsonFormat[TopicCommandResponse] = jsonFormat1(TopicCommandResponse)

  implicit val postCommandResponseJsonFormat: RootJsonFormat[PostCommandResponse] = jsonFormat2(PostCommandResponse)

  implicit val userJsonFormat: JsonFormat[User] =
    new JsonFormat[User] {
      override def write(x: User): JsValue = x match {
        case u: NotLoggedUser => u.toJson
      }
      override def read(value: JsValue): User = ???
    }

  implicit val topicJsonFormat: RootJsonFormat[Topic] = jsonFormat3(Topic)

  implicit val postJsonFormat: RootJsonFormat[Post] = jsonFormat5(Post)
}
