package queryServices

import models.{Post, PostId, Topic, TopicId}

class ForumQueryService {
  def getTopicsList(offset: Option[Int], limit: Option[Int]): Seq[Topic] = ???
  def getTopicPosts(topicId: TopicId, postId: PostId, offsetBefore: Option[Int], offsetAfter: Option[Int]): Seq[Post] = ???
}
