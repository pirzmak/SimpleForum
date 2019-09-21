package repositories.slick

import java.sql.Timestamp

import model.Post

trait PostsTable extends TopicsTable { this: Db =>
  import config.profile.api._

  class Posts(tag: Tag) extends Table[Post](tag, "POSTS") {
    // Columns
    def id = column[Int]("POST_ID", O.PrimaryKey, O.AutoInc)
    def topic_id = column[Int]("TOPIC_ID")
    def text = column[String]("TEXT")
    def creator_nickName = column[String]("CREATOR_NICKNAME")
    def creator_email = column[String]("CREATOR_EMAIL")
    def created = column[Timestamp]("CREATED")
    def last_modified = column[Timestamp]("LAST_MODIFIED")

    // ForeignKey
    def topic_fk =
      foreignKey("TOPIC_FK", topic_id, topics)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

    // Select
    def * = (id.?, topic_id, text, creator_nickName, creator_email, created, last_modified) <>
      (Post.fromRecord, Post.toRecord)
  }

  val posts = TableQuery[Posts]
}
