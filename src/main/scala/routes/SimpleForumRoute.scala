package routes

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import commandServices._
import queryServices.ForumQueryService
import repositories.{PostId, PostSecret, TopicId}
import spray.json._

import scala.reflect.ClassTag

class SimpleForumRoute(forumCommandService: ForumCommandService,
                       forumQueryService: ForumQueryService) extends ForumJsonSupport {

  def route: Route =
    pathPrefix("forum") {
      path("topics") {
        get {
          parameters("offset".as[Int].?, "limit".as[Int].?) { (offset, limit) =>
            complete {
              forumQueryService.getTopicsList(offset, limit)
            }
          }
        } ~
        post {
          entity(as[CreateNewTopicByNotLoggedUser]) { command =>
            handleResponse[TopicCommandResponse] {
              forumCommandService.createNewTopic(
                command.topicName,
                command.message,
                command.creator
              )
            }
          }
        }
      } ~
      pathPrefix("topic" / Segment) { topicId =>
        path("posts") {
          get {
            parameters("postId".as[String], "offsetBefore".as[Int].?, "offsetAfter".as[Int].?) {
              (postId, offsetBefore, offsetAfter) =>
                complete {
                  forumQueryService.getTopicPosts(
                    TopicId(UUID.fromString(topicId)),
                    PostId(UUID.fromString(postId)),
                    offsetBefore, offsetAfter)
                }
            }
          } ~
          post {
            entity(as[CreateNewPostByNotLoggedUser]) { command =>
              handleResponse[PostCommandResponse] {
                forumCommandService.createNewPost(
                  TopicId(UUID.fromString(topicId)),
                  command.message,
                  command.creator)
              }
            }
          } ~
          path(Segment) { postSecret =>
            put {
              entity(as[UpdatePost]) { command =>
                handleResponse[PostCommandResponse] {
                  forumCommandService.updatePost(
                    PostSecret(postSecret),
                    command.newMessage)
                }
              }
            } ~
            delete {
              handleResponse[PostCommandResponse] {
                forumCommandService.deletePost(PostSecret(postSecret))
              }
            }
          }
        }
      }
    }

  private def handleResponse[T: ClassTag](response: Either[FailureResponse, T])(implicit writer: JsonWriter[T]): Route = {
    complete {
      response match {
        case Left(failure) => failure.code -> failure.msg
        case Right(response) => response.toJson.toString()
      }
    }
  }
}
