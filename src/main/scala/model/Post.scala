package model

import java.sql.Timestamp
import java.time.LocalDateTime

case class PostId(value: Int)

case class PostSecret(secret: String)

object Post {
  def apply(topicId: TopicId, message: String, creator: User): Post = {
    Post(None, topicId, message, creator)
  }

  def fromRecord(record: (Option[Int], Int, String, String, String, Timestamp, Timestamp)): Post = {
    Post(record._1.map(PostId), TopicId(record._2), record._3, User(record._4, record._5), record._6, record._7)
  }

  def toRecord(post: Post): Option[(Option[Int], Int, String, String, String, Timestamp, Timestamp)] = {
    Some((post.id.map(_.value), post.topicId.value, post.message, post.creator.nickName, post.creator.email,
      post.createdTime, post.lastModification))
  }
}

case class Post(id: Option[PostId], topicId: TopicId, message: String, creator: User,
                createdTime: Timestamp = Timestamp.valueOf(LocalDateTime.now()), lastModification: Timestamp = Timestamp.valueOf(LocalDateTime.now()))
