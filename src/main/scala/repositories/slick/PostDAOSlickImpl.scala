package repositories.slick

import models.{Post, PostId}
import repositories.interfaces.PostsDAO
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class PostDAOSlickImpl(val config: DatabaseConfig[JdbcProfile])
  extends PostsDAO with Db with PostsTable {

  override def init(): Unit = ???

  override def insert(post: Post): PostId = ???

  override def update(id: PostId, newPost: Post): PostId = ???

  override def delete(id: PostId): PostId = ???

  override def getList(id: PostId, offsetBefore: Int, offsetAfter: Int, limit: Int): Seq[Post] = ???
}

