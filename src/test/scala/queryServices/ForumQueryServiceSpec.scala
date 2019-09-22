package queryServices

import java.sql.Timestamp
import java.time.LocalDateTime

import confguration.{PaginationConfig, ServerConfig, ValidationConfig}
import model._
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, MustMatchers}
import repositories.slick.mocks.{PostsRepositoryMock, TopicsRepositoryMock}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class ForumQueryServiceSpec
  extends AsyncFlatSpec
    with MustMatchers
    with BeforeAndAfter {
  val paginationDefault = 10
  val paginationMaxLimit = 50

  val config = ServerConfig("", 0, 500 milliseconds,
    PaginationConfig(paginationMaxLimit, paginationDefault),
    ValidationConfig("", 0, 100, 0, 100))

  val timeout = config.timeout

  val tmpUser = User("test", "test@test")


  "When empty repositories getTopicsSortedByLastActive method" should "return Empty" in {
    val topicsRepository = new TopicsRepositoryMock()
    val postsRepository = new PostsRepositoryMock(topicsRepository)
    val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, config)

    forumQueryService.getTopicsSortedByLastActive(None, None) map {
      result => result mustBe Seq.empty
    }
  }

  "When empty repositories getTopicPosts method" should "return Empty" in {
    val topicsRepository = new TopicsRepositoryMock()
    val postsRepository = new PostsRepositoryMock(topicsRepository)
    val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, config)

    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(1)), None, None) map {
      result => result mustBe Seq.empty
    }
  }

  val topicsRepository = new TopicsRepositoryMock()
  val postsRepository = new PostsRepositoryMock(topicsRepository)

  val topics = Range(1, 100).map(x => Topic("test", "test", tmpUser))
  val posts = Range(1, 100).map(_ => Post(TopicId(1), "test", tmpUser)) ++
    Range(1, 10).map(_ => Post(TopicId(2), "test", tmpUser))

  Await.result(topicsRepository.init(topics), timeout)
  Await.result(postsRepository.init(posts), timeout)

  val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, config)

  "Forum query service when getTopicsSortedByLastActive with no offset and no limit" should "return default number of topics" in {
    forumQueryService.getTopicsSortedByLastActive(None, None) map {
      result => result.length mustBe paginationDefault
    }
    forumQueryService.getTopicsSortedByLastActive(None, None) map {
      result => result.head mustBe topicsRepository.db.toList.map(_._2).sortBy(_.lastModified).reverse.head
    }
  }

  "Forum query service when getTopicsSortedByLastActive with overLimit" should "return max number of topics" in {
    forumQueryService.getTopicsSortedByLastActive(None, Some(paginationMaxLimit * 2)) map {
      result => result.length mustBe paginationMaxLimit
    }
  }

  "Forum query service when getTopicsSortedByLastActive with offset too big" should "return empty" in {
    forumQueryService.getTopicsSortedByLastActive(Some(topicsRepository.db.size + 1), Some(1)) map {
      result => result mustBe Seq.empty
    }
  }

  "Forum query service when getTopicsSortedByLastActive with offset 0" should "return newest topics" in {
    forumQueryService.getTopicsSortedByLastActive(None, Some(10)) map {
      result => result.length mustBe 10
    }

    forumQueryService.getTopicsSortedByLastActive(Some(0), Some(1)) map {
      result => result.head mustBe topicsRepository.db.toList.map(_._2).sortBy(_.lastModified).reverse.head
    }
  }

  "Forum query service when getTopicsSortedByLastActive with offset 1" should "return next topics" in {
    forumQueryService.getTopicsSortedByLastActive(Some(1), Some(10)) map {
      result => result.length mustBe 10
    }

    forumQueryService.getTopicsSortedByLastActive(Some(1), Some(1)) map {
      result => result.head mustBe topicsRepository.db.toList.map(_._2).sortBy(_.lastModified).reverse.drop(1).head
    }
  }

  "Forum query service when getTopicPosts with no offset and no limit" should "return default number of posts" in {
    forumQueryService.getTopicPosts(TopicId(1), None, None, None) map {
      result => result.length mustBe paginationDefault / 2
    }
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(30)), None, None) map {
      result => result.length mustBe paginationDefault + 1
    }
  }

  "Forum query service when getTopicPosts" should "return sorted posts" in {
    forumQueryService.getTopicPosts(TopicId(1), None, None, Some(1)) map {
      result => result mustBe postsRepository.db.toList.map(_._2).filter(_.topicId == TopicId(1)).sortBy(_.id.get.value).takeRight(2).reverse
    }
  }

  "Forum query service when getTopicPosts with offsets bigger than limit" should "return max number of posts" in {
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(50)), Some(paginationMaxLimit), Some(paginationMaxLimit)) map {
      result => result.length mustBe paginationMaxLimit
    }
  }

  "Forum query service when getTopicPosts with offsets bigger than limit" should "return proper ration of before and after posts" in {
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(50)), Some(paginationMaxLimit * 2), Some(paginationMaxLimit)) map {
      result =>
        val (before, after) = result.span(_.id.get != PostId(50))
        before.length / after.length mustBe 2
    }
  }

  "Forum query service when getTopicPosts with after and before offset 1" should "return 3 posts" in {
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(50)), Some(1), Some(1)) map {
      result => result.length mustBe 3
    }
  }
}
