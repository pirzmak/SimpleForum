package model

import java.sql.Timestamp
import java.time.LocalDateTime

case class TopicId(value: Int)

object Topic {
  def fromRecord(record: (Option[Int], String, String, String, String, Timestamp)): Topic = {
    Topic(record._1.map(TopicId), record._2, record._3, User(record._4, record._5), record._6)
  }

  def toRecord(topic: Topic): Option[(Option[Int], String, String, String, String, Timestamp)] = {
    Some((topic.id.map(_.value), topic.title, topic.text, topic.creator.nickName, topic.creator.email, topic.lastModified))
  }
}

case class Topic(id: Option[TopicId], title: String, text: String, creator: User,
                 lastModified: Timestamp = Timestamp.valueOf(LocalDateTime.now()))