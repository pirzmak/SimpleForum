package models

import java.time.LocalDateTime

import slick.lifted.MappedTo

case class TopicId(value: Int)
case class PostId(value: Int)
case class UserId(value: Int)

case class User(nickName: String, email: String)

case class PostSecret(secret: String)

object Topic {
  def fromRecord(record: (Option[Int], String, String, String, LocalDateTime)): Topic = {
    Topic(record._1.map(TopicId), record._2, User(record._3, record._4), record._5)
  }

  def toRecord(topic: Topic): Option[(Option[Int], String, String, String, LocalDateTime)] = {
    Some((topic.id.map(_.value), topic.text, topic.creator.nickName, topic.creator.email, topic.lastModified))
  }
}

case class Topic(id: Option[TopicId], text: String, creator: User, lastModified: LocalDateTime)

object Post {
  def fromRecord(record: (Option[Int], Int, String, String, String)): Post = {
    Post(record._1.map(PostId), TopicId(record._2), record._3, User(record._4, record._5))
  }

  def toRecord(post: Post): Option[(Option[Int], Int, String, String, String)] = {
    Some((post.id.map(_.value), post.topicId.value, post.message, post.creator.nickName, post.creator.email))
  }
}

case class Post(id: Option[PostId], topicId: TopicId, message: String, creator: User)
