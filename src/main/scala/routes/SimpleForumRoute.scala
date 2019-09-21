package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import commandServices._
import confguration.ServerConfig
import models.{PostId, PostSecret, TopicId}
import queryServices.ForumQueryService
import spray.json._


class SimpleForumRoute(forumCommandService: ForumCommandService,
                       forumQueryService: ForumQueryService,
                       serverConfig: ServerConfig) extends ForumJsonSupport {

  val offsetErrorMessage = "Offset can't be smaller than 0"

  def route: Route =
    pathPrefix("forum") {
      path("topics") {
        get {
          parameters("offset".as[Int].?, "limit".as[Int].?) { (offset, limit) =>
            validate(offset.getOrElse(0) >= 0, offsetErrorMessage) {
              complete {
                forumQueryService.getTopicsSortedByLastActive(offset, limit)
              }
            }
          }
        } ~
        post {
          entity(as[CreateNewTopic]) { command =>
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
            parameters("postId".as[Int].?, "elementsBefore".as[Int].?, "elementsAfter".as[Int].?) {
              (postId, elementsBefore, elementsAfter) =>
                validate(elementsBefore.getOrElse(0) >= 0 && elementsAfter.getOrElse(0) >= 0, offsetErrorMessage) {
                  complete {
                    forumQueryService.getTopicPosts(
                      TopicId(topicId.toInt),
                      postId.map(PostId),
                      elementsBefore, elementsAfter)
                  }
                }
            }
          } ~
          post {
            entity(as[CreateNewPost]) { command =>
              handleResponse[PostCommandResponse] {
                forumCommandService.createNewPost(
                  TopicId(topicId.toInt),
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

  private def handleResponse[T](response: Either[FailureResponse, T])(implicit writer: JsonWriter[T]): Route = {
    complete {
      response match {
        case Left(failure) => failure.code -> failure.msg
        case Right(response) => response.toJson.toString()
      }
    }
  }
}
