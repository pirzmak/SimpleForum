package model

import java.util.UUID

object PostSecretGenerator {
  def getPostSecret(postId: PostId): PostSecret = {
    PostSecret(new UUID(Long.MaxValue / 97, Int.MaxValue + postId.value).toString)
  }

  def getPostId(postSecret: PostSecret): Option[PostId] = {
    try {
      Some(PostId((UUID.fromString(postSecret.secret).getLeastSignificantBits - Int.MaxValue).toInt))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}
