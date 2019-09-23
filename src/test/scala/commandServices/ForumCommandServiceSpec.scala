package commandServices

import confguration.{PaginationConfig, ServerConfig, ValidationConfig}
import model._
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, MustMatchers}
import repositories.mocks.{PostsRepositoryMock, TopicsRepositoryMock}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class ForumCommandServiceSpec
  extends AsyncFlatSpec
    with MustMatchers
    with BeforeAndAfter {
  val paginationDefault = 10
  val paginationMaxLimit = 50

  val config = ServerConfig("", 0, 500 milliseconds,
    PaginationConfig(paginationMaxLimit, paginationDefault),
    ValidationConfig("""(\w+)@([\w\.]+)""", 0, 100, 0, 100, 2, 10))

  val timeout = config.timeout

  val tmpUser = User("test", "test@test")

  val topicsRepository = new TopicsRepositoryMock()
  val postsRepository = new PostsRepositoryMock(topicsRepository)

  val forumCommandService = new ForumCommandService(topicsRepository, postsRepository, config)

  before {
    Await.result(topicsRepository.init(Seq(Topic("test", "test", tmpUser))), timeout)
    Await.result(postsRepository.init(Seq(Post(TopicId(1), "test", tmpUser))), timeout)
  }

  after {
    Await.result(topicsRepository.drop(), timeout)
    Await.result(postsRepository.drop(), timeout)
  }

  "Forum command service when add new post with wrong topicId" should "return TopicIdNotFoundFailure" in {
    forumCommandService.createNewPost(TopicId(0), "test", tmpUser) map {
      result => result mustBe Left(CommandFailure.TopicIdNotFoundFailure)
    }
  }

  "Forum command service when add new proper post" should "return PostCommandResponse" in {
    forumCommandService.createNewPost(TopicId(1), "test", tmpUser) map {
      result => result mustBe Right(PostCommandResponse(PostSecretGenerator.getPostSecret(PostId(2))))
    }
  }

  "Forum command service when add new topic" should "return TopicCommandResponse" in {
    forumCommandService.createNewTopic("test", "test", tmpUser) map {
      result => result mustBe Right(TopicCommandResponse(TopicId(2)))
    }
  }

  "Forum command service when edit post with incorrect PostSecret" should "return IncorrectPostSecretFailure" in {
    forumCommandService.updatePost(PostSecret("asd"), "test") map {
      result => result mustBe Left(CommandFailure.IncorrectPostSecretFailure)
    }
  }

  "Forum command service when edit post with correct date" should "return PostCommandResponse" in {
    val postSecret = PostSecretGenerator.getPostSecret(PostId(1))

    forumCommandService.updatePost(postSecret, "test") map {
      result => result mustBe Right(PostCommandResponse(postSecret))
    }
  }

  "Forum command service when delete post with incorrect PostSecret" should "return IncorrectPostSecretFailure" in {
    forumCommandService.deletePost(PostSecret("asd")) map {
      result => result mustBe Left(CommandFailure.IncorrectPostSecretFailure)
    }
  }

  "Forum command service when delete post with correct PostSecret" should "return PostCommandResponse" in {
    val postSecret = PostSecretGenerator.getPostSecret(PostId(1))

    forumCommandService.deletePost(postSecret) map {
      result => result mustBe Right(PostCommandResponse(postSecret))
    }
  }
}
