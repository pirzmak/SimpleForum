package model

import java.util.UUID

import scala.util.Random

case class PostId(value: Int)

case class PostSecret(secret: String)

object PostSecretGenerator {
  def getPostSecret(postId: PostId): PostSecret = {
    PostSecret(new UUID(Long.MaxValue, Int.MaxValue + postId.value).toString)
  }

  def getPostId(postSecret: PostSecret): Option[PostId] = {
    try {
    Some(PostId((UUID.fromString(postSecret.secret).getLeastSignificantBits - Int.MaxValue).toInt))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}

object Post {
  def fromRecord(record: (Option[Int], Int, String, String, String)): Post = {
    Post(record._1.map(PostId), TopicId(record._2), record._3, User(record._4, record._5))
  }

  def toRecord(post: Post): Option[(Option[Int], Int, String, String, String)] = {
    Some((post.id.map(_.value), post.topicId.value, post.message, post.creator.nickName, post.creator.email))
  }
}

case class Post(id: Option[PostId], topicId: TopicId, message: String, creator: User)
