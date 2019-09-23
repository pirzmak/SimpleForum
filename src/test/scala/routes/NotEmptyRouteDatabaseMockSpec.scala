package routes

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import commandServices.{CommandFailure, CreateNewPost, CreateNewTopic, ForumCommandService, PostCommandResponse, TopicCommandResponse, UpdatePost}
import confguration.{PaginationConfig, ServerConfig, ValidationConfig}
import model.{PostId, PostSecretGenerator, Topic, TopicId, User}
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}
import queryServices.ForumQueryService
import repositories.slick.{PostsRepositorySlickImpl, TopicsRepositorySlickImpl}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class NotEmptyRouteDatabaseMockSpec
  extends AsyncWordSpec
    with ScalatestRouteTest
    with MustMatchers
    with BeforeAndAfter
    with ForumJsonSupport {
  val config = ServerConfig("", 0, 500 milliseconds,
    PaginationConfig(50, 20),
    ValidationConfig("""(\w+)@([\w\.]+)""", 1, 100, 1, 100, 2, 10))

  val dbconfig = DatabaseConfig.forConfig[JdbcProfile]("h2mem1")

  val topicsRepository = new TopicsRepositorySlickImpl(dbconfig)
  val postsRepository = new PostsRepositorySlickImpl(dbconfig)

  val forumCommandService = new ForumCommandService(topicsRepository, postsRepository, config)
  val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, config)

  val forumRoute = new SimpleForumRoute(forumCommandService, forumQueryService, config)

  val tmpUser = User("test", "test@test.con")

  val topics = Seq(
    Topic(None, "tmp", "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN)),
    Topic(None, "tmp", "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN.plusDays(1))),
    Topic(None, "tmp", "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN.plusDays(2))),
    Topic(None, "tmp", "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN.plusDays(3))))

  val topicsResponse = topics.zipWithIndex.map(t => t._1.copy(id = Some(TopicId(t._2 + 1)))).sortBy(_.lastModified).reverse

  val posts = Seq(
    model.Post(None, TopicId(1), "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN), Timestamp.valueOf(LocalDateTime.MIN)),
    model.Post(None, TopicId(1), "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN), Timestamp.valueOf(LocalDateTime.MIN)),
    model.Post(None, TopicId(1), "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN), Timestamp.valueOf(LocalDateTime.MIN)),
    model.Post(None, TopicId(1), "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN), Timestamp.valueOf(LocalDateTime.MIN)))

  val postsResponse = posts.zipWithIndex.map(t => t._1.copy(id = Some(PostId(t._2 + 1))))

  before {
    Await.result(topicsRepository.init(topics), config.timeout)
    Await.result(postsRepository.init(posts), config.timeout)
  }

  after {
    Await.result(postsRepository.drop(), config.timeout)
    Await.result(topicsRepository.drop(), config.timeout)
  }

  s"Not Empty forum route with database mocks" should {
    "Return topics (GET /topics)" in {
      val request = HttpRequest(uri = "/forum/topics")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe topicsResponse.toJson.toString
      }
    }

    "Return limited topics (GET /topics?offset=0&limit=1)" in {
      val request = HttpRequest(uri = "/forum/topics?offset=0&limit=1")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe Seq(topicsResponse.head).toJson.toString
      }
    }

    "Return next page limited topics (GET /topics?offset=1&limit=1)" in {
      val request = HttpRequest(uri = "/forum/topics?offset=1&limit=1")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe Seq(topicsResponse.tail.head).toJson.toString
      }
    }

    "Return limited topics when overLimit (GET /topics?offset=0&limit=100)" in {
      val request = HttpRequest(uri = "/forum/topics?offset=0&limit=100")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe topicsResponse.toJson.toString
      }
    }

    "Return posts (GET /topics/1/posts)" in {
      val request = HttpRequest(uri = "/forum/topics/1/posts")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe postsResponse.toJson.toString
      }
    }

    "Return limited posts (GET /topics/1/posts?postId=1&before=1&after=1)" in {
      val request = HttpRequest(uri = "/forum/topics/1/posts?postId=1&before=1&after=1")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe postsResponse.take(2).toJson.toString
      }
    }

    "Return next page limited posts (GET /topics/1/posts?postId=2&before=1&after=1)" in {
      val request = HttpRequest(uri = "/forum/topics/1/posts?postId=2&before=1&after=1")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe postsResponse.slice(0, 3).toJson.toString
      }
    }

    "Return limited posts when overLimit (GET /topics/1/posts?postId=1&before=50&after=100)" in {
      val request = HttpRequest(uri = "/forum/topics/1/posts?postId=1&before=50&after=100")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe postsResponse.take(4).toJson.toString
      }
    }
  }

  s"Routes with validations with database mocks" should {
    "Return failed when try add incorrect topic (POST /topics)" in {
      val topic = CreateNewTopic("", "", User("joe", "doe"))

      val request = Post("/forum/topics").withEntity(ContentTypes.`application/json`, topic.toJson.toString)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        val response = CommandFailure.TopicTitleValidationTooShortFailure ::
          CommandFailure.PostValidationTooShortFailure ::
          CommandFailure.EmailIncorrectValidationFailure

        entityAs[String] mustBe response.toJson.toString
      }
    }

    "Return failed when try add incorrect post (POST /topics/{id}/posts)" in {
      val post = CreateNewPost("", User("joe", "doe"))

      val request = Post("/forum/topics/0/posts").withEntity(ContentTypes.`application/json`, post.toJson.toString)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        val response = CommandFailure.TopicIdNotFoundFailure ::
          CommandFailure.PostValidationTooShortFailure ::
          CommandFailure.EmailIncorrectValidationFailure

        entityAs[String] mustBe response.toJson.toString
      }
    }

    "Return failed when try edit incorrect post (PUT /topics/{id}/posts/{secret})" in {
      val post = UpdatePost("")

      val request = Put("/forum/topics/0/posts/afafg").withEntity(ContentTypes.`application/json`, post.toJson.toString)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        val response =
          CommandFailure.IncorrectPostSecretFailure ::
            CommandFailure.PostValidationTooShortFailure

        entityAs[String] mustBe response.toJson.toString
      }
    }

    "Return failed when try edit post with incorrect id (PUT /topics/{id}/posts/{secret})" in {
      val post = UpdatePost("aasd")

      val request = Put("/forum/topics/0/posts/7fffffff-ffff-ffff-ffff-ffff801120000").withEntity(ContentTypes.`application/json`, post.toJson.toString)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        entityAs[String] mustBe CommandFailure.PostIdNotFoundFailure.toJson.toString
      }
    }

    "Return failed when try delete post with incorrect id (DELETE /topics/{id}/posts/{secret})" in {
      val request = Delete("/forum/topics/0/posts/7fffffff-ffff-ffff-ffff-ffff801120000")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.BadRequest

        entityAs[String] mustBe CommandFailure.PostIdNotFoundFailure.toJson.toString
      }
    }

    "Return success when add correct topic (POST /topics)" in {
      val topic = CreateNewTopic("asdas", "asd", User("joe", "doe@awed.com"))

      val request = Post("/forum/topics").withEntity(ContentTypes.`application/json`, topic.toJson.toString)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe TopicCommandResponse(TopicId(5)).toJson.toString
      }
    }

    "Return success when add correct post (POST /topics/{id}/posts)" in {
      val post = CreateNewPost("adfasdf", User("joe", "doe@wp.pl"))

      val request = Post("/forum/topics/1/posts").withEntity(ContentTypes.`application/json`, post.toJson.toString)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe PostCommandResponse(PostSecretGenerator.getPostSecret(PostId(5))).toJson.toString
      }
    }

    "Return success when edit correct post (PUT /topics/{id}/posts/{secret})" in {
      val post = UpdatePost("aasd")
      val secret = PostSecretGenerator.getPostSecret(PostId(1))
      val request = Put(s"/forum/topics/1/posts/${secret.secret}").withEntity(ContentTypes.`application/json`, post.toJson.toString)

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe PostCommandResponse(secret).toJson.toString
      }
    }

    "Return success when delete post (DELETE /topics/{id}/posts/{secret})" in {
      val secret = PostSecretGenerator.getPostSecret(PostId(1))
      val request = Delete(s"/forum/topics/1/posts/${secret.secret}")

      request ~> forumRoute.route ~> check {
        status mustBe StatusCodes.OK

        entityAs[String] mustBe PostCommandResponse(secret).toJson.toString
      }
    }
  }
}