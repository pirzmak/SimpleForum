package routes


import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import commandServices.{CreateNewPost, CreateNewTopic, PostCommandResponse, TopicCommandResponse, UpdatePost}
import model.{Post, PostId, PostSecret, Topic, TopicId, User}
import spray.json._

trait ForumJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val localDateTimeJsonFormat: JsonFormat[Timestamp] =
    new JsonFormat[Timestamp] {
      private val formatter = DateTimeFormatter.ISO_DATE_TIME

      override def write(x: Timestamp): JsValue = JsString(x.toLocalDateTime.format(formatter))

      override def read(value: JsValue): Timestamp = value match {
        case JsString(x) => Timestamp.valueOf(LocalDateTime.parse(x))
        case x => deserializationError("Wrong time format of " + x)
      }
    }

  implicit val topicIdJsonFormat: RootJsonFormat[TopicId] = jsonFormat1(TopicId)

  implicit val postIdJsonFormat: RootJsonFormat[PostId] = jsonFormat1(PostId)

  implicit val postSecretJsonFormat: RootJsonFormat[PostSecret] = jsonFormat1(PostSecret)

  implicit val notLoggedUserJsonFormat: RootJsonFormat[User] = jsonFormat2(User)

  implicit val createNewTopicByNotLoggedUserJsonFormat: RootJsonFormat[CreateNewTopic] = jsonFormat3(CreateNewTopic)

  implicit val createNewPostByNotLoggedUserJsonFormat: RootJsonFormat[CreateNewPost] = jsonFormat2(CreateNewPost)

  implicit val updatePost: RootJsonFormat[UpdatePost] = jsonFormat1(UpdatePost)

  implicit val topicCommandResponseJsonFormat: RootJsonFormat[TopicCommandResponse] = jsonFormat1(TopicCommandResponse)

  implicit val postCommandResponseJsonFormat: RootJsonFormat[PostCommandResponse] = jsonFormat1(PostCommandResponse)

  implicit val topicJsonFormat: RootJsonFormat[Topic] = jsonFormat5(Topic.apply)

  implicit val postJsonFormat: RootJsonFormat[Post] = jsonFormat4(Post.apply)
}
