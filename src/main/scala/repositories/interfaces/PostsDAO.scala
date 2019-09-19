package repositories.interfaces

import models.{Post, PostId}

trait PostsDAO {
  def init()
  def insert(post: Post): PostId
  def update(id: PostId, newPost: Post): PostId
  def delete(id: PostId): PostId
  def getList(id: PostId, offsetBefore: Int, offsetAfter: Int, limit: Int): Seq[Post]
}
