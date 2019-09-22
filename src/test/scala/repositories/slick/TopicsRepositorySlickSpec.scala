package repositories.slick

import model._
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, MustMatchers}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class TopicsRepositorySlickSpec
  extends AsyncFlatSpec
    with MustMatchers
    with BeforeAndAfter {
  val config = DatabaseConfig.forConfig[JdbcProfile]("h2mem1")

  val timeout = 500 milliseconds

  val topicsRepository = new TopicsRepositorySlickImpl(config)
  val tmpUser = User("test", "test@test")

  after {
    Await.result(topicsRepository.drop(), timeout)
  }

  "Empty topics repository getById method" should "return None" in {
    Await.result(topicsRepository.init(), timeout)

    topicsRepository.getById(TopicId(1)) map {
      result => result mustBe None
    }
  }

  "Topics repository getById method" should "return element" in {
    Await.result(topicsRepository.init(Seq(Topic("tmp", "test", tmpUser))), timeout)

    topicsRepository.getById(TopicId(1)) map {
      result => result.flatMap(_.id) mustBe Some(TopicId(1))
    }
  }

  "Topics repository getById method with wrong id" should "return None" in {
    Await.result(topicsRepository.init(Seq(Topic("tmp", "test", tmpUser))), timeout)

    topicsRepository.getById(TopicId(2)) map {
      result => result mustBe None
    }
  }

  "Empty topics repository getAllSortedByLastActivity" should "return empty list" in {
    Await.result(topicsRepository.init(), timeout)

    topicsRepository.getAllSortedByLastActivity( 0, 3) map {
      result => result mustBe Seq.empty
    }
  }

  "Topics repository getAllSortedByLastActivity" should "return list with topics" in {
    Await.result(topicsRepository.init(Seq(Topic("tmp", "test", tmpUser))), timeout)

    topicsRepository.getAllSortedByLastActivity(0, 3) map {
      result => result.length mustBe 1
    }
  }

  "Topics repository getAllSortedByLastActivity" should "return filtered list with topics" in {
    Await.result(topicsRepository.init(Seq(
      Topic("tmp", "test", tmpUser),
      Topic("tmp", "test", tmpUser),
      Topic("tmp", "test", tmpUser),
      Topic("tmp", "test", tmpUser))), timeout)

    topicsRepository.getAllSortedByLastActivity(1, 1) map {
      result => result.length mustBe 1
    }
    topicsRepository.getAllSortedByLastActivity(1, 2) map {
      result => result.length mustBe 2
    }
    topicsRepository.getAllSortedByLastActivity(0, 2) map {
      result => result.length mustBe 3
    }
    topicsRepository.getAllSortedByLastActivity(0, 22) map {
      result => result.length mustBe 4
    }
    topicsRepository.createNew(Topic("tmp", "test", tmpUser)) flatMap  {
      result => topicsRepository.getAllSortedByLastActivity(0, 1) map {
        result => result.map(_.id) mustBe Seq(Some(TopicId(5)))
      }
    }
  }

  "Topics repository create new" should "create new row in table" in {
    Await.result(topicsRepository.init(), timeout)

    topicsRepository.createNew(Topic("tmp", "test", tmpUser)) flatMap {
      result => result mustBe TopicId(1)
        topicsRepository.getById(TopicId(1)) map {
          r => r.flatMap(_.id) mustBe Some(TopicId(1))
        }
    }
  }

  "Topics repository create new" should "return topicId" in {
    Await.result(topicsRepository.init(), timeout)

    topicsRepository.createNew(Topic("tmp", "test", tmpUser)) map {
      result => result mustBe TopicId(1)
    }
  }

  "Topics repository when add new post to topic" should "update timestamp" in {
    val postsRepository = new PostsRepositorySlickImpl(config)
    Await.result(topicsRepository.init(Seq(Topic("tmp", "test", tmpUser))), timeout)
    Await.result(postsRepository.init(), timeout)

    topicsRepository.getById(TopicId(1)) flatMap  {
      result => postsRepository.createNew(Post(TopicId(1), "tmp", tmpUser)) flatMap {
        _ => topicsRepository.getById(TopicId(1)) map  {
          rr =>
            Await.result(postsRepository.drop(), timeout)
            result.map(_.lastModified) must not be rr.map(_.lastModified)
        }
      }
    }
  }
}
