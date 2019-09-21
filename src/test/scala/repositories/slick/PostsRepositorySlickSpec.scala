package repositories.slick


import models.{Post, PostId, Topic, TopicId, User}
import org.h2.jdbc.JdbcException
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, MustMatchers}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await

class PostsRepositorySlickSpec
  extends AsyncFlatSpec
    with MustMatchers
    with BeforeAndAfter {
  val config = DatabaseConfig.forConfig[JdbcProfile]("h2mem1")

  val timeout = 500 milliseconds

  val topicsRepository = new TopicsRepositorySlickImpl(config)
  val topicId = TopicId(1)
  val tmpUser = User("test", "test@test")

  val postsRepository = new PostsRepositorySlickImpl(config)

  before {
    Await.result(topicsRepository.init(Seq(Topic(Some(topicId), "tmp", tmpUser))), timeout)
  }

  after {
    Await.result(postsRepository.drop(), timeout)
    Await.result(topicsRepository.drop(), timeout)
  }

  "Empty posts repository getById method" should "return None" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.getById(PostId(1)) map {
      result => result mustBe None
    }
  }

  "Posts repository getById method" should "return element" in {
    Await.result(postsRepository.init(Seq(Post(None, topicId, "tmp", tmpUser))), timeout)

    postsRepository.getById(PostId(1)) map {
      result => result mustBe Some(Post(Some(PostId(1)), topicId, "tmp", tmpUser))
    }
  }

  "Posts repository getById method with wrong id" should "return None" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.getById(PostId(2)) map {
      result => result mustBe None
    }
  }

  "Empty posts repository getAll method" should "return empty list" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.getAll(topicId, Some(PostId(1)), 3, 3) map {
      result => result mustBe Seq.empty
    }
  }

  "Posts repository getAll method from start" should "return list with posts" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.getAll(topicId, None, 0, 0) map {
      result => result.length mustBe 1
    }
  }

  "Posts repository getAll method" should "return list with posts" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.getAll(topicId, Some(PostId(1)), 0, 0) map {
      result => result.length mustBe 1
    }
  }

  "Posts repository getAll method with limits" should "return filtered list with posts" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser),
      Post(Some(PostId(2)), topicId, "tmp", tmpUser),
      Post(Some(PostId(3)), topicId, "tmp", tmpUser),
      Post(Some(PostId(4)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.getAll(topicId, Some(PostId(3)), 1, 1) map {
      result => result mustBe Seq(
        Post(Some(PostId(2)), topicId, "tmp", tmpUser),
        Post(Some(PostId(3)), topicId, "tmp", tmpUser),
        Post(Some(PostId(4)), topicId, "tmp", tmpUser))
    }
  }

  "Posts repository create new" should "create new row in table" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.createNew(Post(None, topicId, "tmp", tmpUser)) flatMap {
      result => result mustBe PostId(1)
        postsRepository.getAll(topicId, Some(PostId(1)), 3, 3) map {
          r => r.length mustBe 1
        }
    }
  }

  "Posts repository create new" should "return postId" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.createNew(Post(None, topicId, "tmp", tmpUser)) map {
      result => result mustBe PostId(1)
    }
  }

  "Posts repository create new with wrong topicId" should "return failure" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.createNew(Post(None, TopicId(3), "tmp", tmpUser)).failed map {
      result => result mustBe a [JdbcException]
    }
  }

  "Posts repository edit post" should "return postId" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.update(PostId(1), "newMessage") map {
      result => result mustBe PostId(1)
    }
  }

  "Posts repository edit post" should "edit row in table" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.update(PostId(1), "newMessage") flatMap {
      result => postsRepository.getById(PostId(1)) map {
        r => r.get.message mustBe "newMessage"
      }
    }
  }

  "Posts repository delete post" should "delete row row from table" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.delete(PostId(1)) flatMap {
      result => postsRepository.getAll(topicId, Some(PostId(1)), 5, 5) map {
        r => r.length mustBe 0
      }
    }
  }
}
