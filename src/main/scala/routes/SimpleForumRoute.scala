package routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import commandServices._
import confguration.ServerConfig
import model.{PostId, PostSecret, TopicId}
import queryServices.ForumQueryService


class SimpleForumRoute(forumCommandService: ForumCommandService,
                       forumQueryService: ForumQueryService,
                       serverConfig: ServerConfig)(implicit ec: ExecutionContext) extends ForumJsonSupport {

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
                command.title,
                command.text,
                command.creator
              )
            }
          }
        }
      } ~
      pathPrefix("topics" / Segment) { topicId =>
        path("posts") {
          get {
            parameters("postId".as[Int].?, "before".as[Int].?, "after".as[Int].?) {
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
          }
        } ~
        path("posts" / Segment) { postSecret =>
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

  private def handleResponse[T](response: Future[Either[CommandFailure, T]])(implicit writer: JsonWriter[T]): Route = {
    complete {
      response.map{
        case Left(failure) => failure.msg match {
          case CommandFailure.PostIdNotFoundFailure => StatusCodes.NotFound -> failure.toJson.toString
          case _ => StatusCodes.BadRequest -> failure.toJson.toString
        }
        case Right(response) => StatusCodes.OK -> response.toJson.toString()
      }
    }
  }
}
