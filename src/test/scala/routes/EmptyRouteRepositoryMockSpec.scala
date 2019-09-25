package routes

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import commandServices.{CommandFailure, ForumCommandService, TopicCommandResponse}
import confguration.{PaginationConfig, ServerConfig, ValidationConfig}
import model.TopicId
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}
import queryServices.ForumQueryService
import repositories.mocks.{PostsRepositoryMock, TopicsRepositoryMock}
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class EmptyRouteRepositoryMockSpec
  extends AsyncWordSpec
    with ScalatestRouteTest
    with MustMatchers
    with BeforeAndAfter
    with ForumJsonSupport {
  val config = ServerConfig("", 0, 500 milliseconds,
    PaginationConfig(50, 20),
    ValidationConfig("""(\w+)@([\w\.]+)""", 0, 100, 0, 100, 2, 10))

  val topicsRepository = new TopicsRepositoryMock()
  val postsRepository = new PostsRepositoryMock(topicsRepository)

  val forumCommandService = new ForumCommandService(topicsRepository, postsRepository, config)
  val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, config)

  val forumRoute = new SimpleForumRoute(forumCommandService, forumQueryService, config)

  before {
    Await.result(topicsRepository.init(Seq.empty), config.timeout)
    Await.result(postsRepository.init(Seq.empty), config.timeout)
  }

  after {
    Await.result(postsRepository.drop(), config.timeout)
    Await.result(topicsRepository.drop(), config.timeout)
  }

  s"Empty forum route with repository mocks" should {
    "Return no topics (GET /topics)" in {
      val request = HttpRequest(uri = "/forum/topics")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe "[]"
      }
    }

    "Return no posts (GET topics/{id}/posts)" in {
      val request = HttpRequest(uri = "/forum/topics/1/posts")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe "[]"
      }
    }

    "When try add post return topic id error (POST /topics/{id}/posts)" in {
      val post = """{"message": "text", "creator": {"nickName": "joe", "email": "jon@doe.com"}}"""

      val request = Post("/forum/topics/1/posts").withEntity(ContentTypes.`application/json`, post)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        entityAs[String] mustBe CommandFailure.TopicIdNotFoundFailure.toJson.toString
      }
    }

    "When try edit post return post id error (PUT /topics/{id}/posts/{secret})" in {
      val post = """{"newMessage": "text"}"""

      val request = Put("/forum/topics/1/posts/7fffffff-ffff-ffff-ffff-ffff80000000").withEntity(ContentTypes.`application/json`, post)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        entityAs[String] mustBe CommandFailure.PostIdNotFoundFailure.toJson.toString
      }
    }

    "When try delete post return post id error (DELETE /topics/{id}/posts/{secret})" in {
      val request = Delete("/forum/topics/1/posts/7fffffff-ffff-ffff-ffff-ffff80000000")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        entityAs[String] mustBe CommandFailure.PostIdNotFoundFailure.toJson.toString
      }
    }

    "Be able to add topic (POST /topics)" in {
      val topic = """{"title": "Title", "text": "text", "creator": {"nickName": "joe", "email": "jon@doe.com"}}"""

      val request = Post("/forum/topics").withEntity(ContentTypes.`application/json`, topic)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe TopicCommandResponse(TopicId(1)).toJson.toString
      }
    }
  }
}