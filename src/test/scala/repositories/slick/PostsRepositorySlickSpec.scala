package repositories.slick


import model.{Post, PostId, Topic, TopicId, User}
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
    Await.result(topicsRepository.init(Seq(Topic(Some(topicId), "tmp", "test", tmpUser))), timeout)
  }

  after {
    Await.result(postsRepository.drop(), timeout)
    Await.result(topicsRepository.drop(), timeout)
  }

  "Empty posts repository when getById" should "return None" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.getById(PostId(1)) map {
      result => result mustBe None
    }
  }

  "Posts repository with posts when getById with proper id" should "return element" in {
    val post = Post(Some(PostId(1)), topicId, "tmp", tmpUser)
    Await.result(postsRepository.init(Seq(post)), timeout)

    postsRepository.getById(PostId(1)) map {
      result => result mustBe Some(post)
    }
  }

  "Posts repository with posts when getById with wrong id" should "return None" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.getById(PostId(2)) map {
      result => result mustBe None
    }
  }

  "Empty posts repository when getAll" should "return empty" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.getAll(topicId, Some(PostId(1)), 3, 3) map {
      result => result mustBe Seq.empty
    }
  }

  "Posts repository with posts when getAll from start" should "return list with posts" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.getAll(topicId, None, 0, 0) map {
      result => result.length mustBe 1
    }
  }

  "Posts repository with posts when getAll from some post" should "return list with posts" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.getAll(topicId, Some(PostId(1)), 0, 0) map {
      result => result.length mustBe 1
    }
  }

  "Posts repository with posts when getAll with limits" should "return limited list with posts" in {
    val posts = Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser),
      Post(Some(PostId(2)), topicId, "tmp", tmpUser),
      Post(Some(PostId(3)), topicId, "tmp", tmpUser),
      Post(Some(PostId(4)), topicId, "tmp", tmpUser))

    Await.result(postsRepository.init(posts), timeout)

    postsRepository.getAll(topicId, Some(PostId(3)), 1, 1) map {
      result => result mustBe posts.tail.reverse
    }
  }

  "Posts repository when create new" should "create new row in table" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.createNew(Post(topicId, "tmp", tmpUser)) flatMap {
      result => result mustBe PostId(1)
        postsRepository.getAll(topicId, Some(PostId(1)), 3, 3) map {
          r => r.length mustBe 1
        }
    }
  }

  "Posts repository when create new" should "return postId" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.createNew(Post(topicId, "tmp", tmpUser)) map {
      result => result mustBe PostId(1)
    }
  }

  "Posts repository whe create new with wrong topicId" should "return failure" in {
    Await.result(postsRepository.init(), timeout)

    postsRepository.createNew(Post(TopicId(3), "tmp", tmpUser)).failed map {
      result => result mustBe a [JdbcException]
    }
  }

  "Posts repository when edit post" should "return postId" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.update(PostId(1), "newMessage") map {
      result => result mustBe true
    }
  }

  "Posts repository when edit post" should "edit row in table" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.update(PostId(1), "newMessage") flatMap {
      result => postsRepository.getById(PostId(1)) map {
        r => r.get.message mustBe "newMessage"
      }
    }
  }

  "Posts repository when edit post with wrong Id" should "return failure" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.update(PostId(0), "newMessage") map {
      result => result mustBe false
    }
  }

  "Posts repository when delete post" should "delete row row from table" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.delete(PostId(1)) flatMap {
      result => postsRepository.getAll(topicId, Some(PostId(1)), 5, 5) map {
        r => r.length mustBe 0
      }
    }
  }

  "Posts repository when delete post with wrong Id" should "return failure" in {
    Await.result(postsRepository.init(Seq(Post(Some(PostId(1)), topicId, "tmp", tmpUser))), timeout)

    postsRepository.delete(PostId(0)) map {
      result => result mustBe false
    }
  }
}
