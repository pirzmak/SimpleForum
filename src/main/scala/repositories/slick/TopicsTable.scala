package repositories.slick

import java.time.LocalDateTime

import models.Topic

trait TopicsTable { this: Db =>
  import config.profile.api._

  class Topics(tag: Tag) extends Table[Topic](tag, "TOPICS") {
    // Columns
    def id = column[Int]("TOPIC_ID", O.PrimaryKey, O.AutoInc)
    def text = column[String]("TEXT")
    def creator_nickName = column[String]("CREATOR_NICKNAME")
    def creator_email = column[String]("CREATOR_EMAIL")
    def last_modified = column[LocalDateTime]("LAST_MODIFIED")

    // Select
    def * = (id.?, text, creator_nickName, creator_email, last_modified) <>
      (Topic.fromRecord, Topic.toRecord)
  }

  val topics = TableQuery[Topics]
}
