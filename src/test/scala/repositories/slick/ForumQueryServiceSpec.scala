package repositories.slick

import java.sql.Timestamp
import java.time.LocalDateTime

import confguration.{PaginationConfig, ServerConfig}
import models.{Post, PostId, Topic, TopicId, User}
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, MustMatchers}
import queryServices.ForumQueryService
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

  val config = ServerConfig("", 0, 500 milliseconds, PaginationConfig(paginationMaxLimit, paginationDefault))

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

  val topics = Range(1, 100).map(x => Topic(None, "test", tmpUser, Timestamp.valueOf(LocalDateTime.MIN.plusHours(x))))
  val posts = Range(1, 100).map(_ => Post(None, TopicId(1), "test", tmpUser)) ++
    Range(1, 10).map(_ => Post(None, TopicId(2), "test", tmpUser))

  Await.result(topicsRepository.init(topics), timeout)
  Await.result(postsRepository.init(posts), timeout)

  val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, config)

  "When getTopicsSortedByLastActive method with no offset and no limit" should "return default values" in {
    forumQueryService.getTopicsSortedByLastActive(None, None) map {
      result => result.length mustBe paginationDefault
    }
    forumQueryService.getTopicsSortedByLastActive(None, None) map {
      result => result.head mustBe topicsRepository.db.toList.map(_._2).sortBy(_.lastModified).reverse.head
    }
  }

  "When getTopicsSortedByLastActive method with overLimit" should "return max values" in {
    forumQueryService.getTopicsSortedByLastActive(None, Some(paginationMaxLimit * 2)) map {
      result => result.length mustBe paginationMaxLimit
    }
  }

  "When getTopicsSortedByLastActive with offset more than records" should "return empty" in {
    forumQueryService.getTopicsSortedByLastActive(Some(topicsRepository.db.size + 1), Some(1)) map {
      result => result mustBe Seq.empty
    }
  }

  "When getTopicsSortedByLastActive with offset 0" should "return newest topics" in {
    forumQueryService.getTopicsSortedByLastActive(None, Some(10)) map {
      result => result.length mustBe 10
    }

    forumQueryService.getTopicsSortedByLastActive(Some(0), Some(1)) map {
      result => result.head mustBe topicsRepository.db.toList.map(_._2).sortBy(_.lastModified).reverse.head
    }
  }

  "When getTopicsSortedByLastActive with offset 1" should "return next topics" in {
    forumQueryService.getTopicsSortedByLastActive(Some(1), Some(10)) map {
      result => result.length mustBe 10
    }

    forumQueryService.getTopicsSortedByLastActive(Some(1), Some(1)) map {
      result => result.head mustBe topicsRepository.db.toList.map(_._2).sortBy(_.lastModified).reverse.drop(1).head
    }
  }

  "When getTopicPosts method with no offset and no limit" should "return default topics" in {
    forumQueryService.getTopicPosts(TopicId(1), None, None, None) map {
      result => result.length mustBe paginationDefault / 2
    }
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(30)), None, None) map {
      result => result.length mustBe paginationDefault + 1
    }
  }

  "When getTopicPosts method" should "return sorted posts" in {
    forumQueryService.getTopicPosts(TopicId(1), None, None, Some(1)) map {
      result => result mustBe postsRepository.db.toList.map(_._2).filter(_.topicId == TopicId(1)).sortBy(_.id.get.value).takeRight(2).reverse
    }
  }

  "When getTopicPosts method get offsets bigger than limit" should "return limit size posts" in {
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(50)), Some(paginationMaxLimit), Some(paginationMaxLimit)) map {
      result => result.length mustBe paginationMaxLimit
    }
  }

  "When getTopicPosts method get offsets bigger than limit" should "return proper before and after posts" in {
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(50)), Some(paginationMaxLimit * 2), Some(paginationMaxLimit)) map {
      result =>
        val (before, after) = result.span(_.id.get != PostId(50))
        before.length / after.length mustBe 2
    }
  }

  "When getTopicPosts method offset 1" should "return 3 posts" in {
    forumQueryService.getTopicPosts(TopicId(1), Some(PostId(50)), Some(1), Some(1)) map {
      result => result.length mustBe 3
    }
  }
}
