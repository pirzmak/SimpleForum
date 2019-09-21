package queryServices

import confguration.ServerConfig
import models.{Post, PostId, Topic, TopicId}
import repositories.interfaces.{PostsRepository, TopicsRepository}

import scala.concurrent.Future

class ForumQueryService(topicsRepository: TopicsRepository,
                        postsRepository: PostsRepository,
                        serverConfig: ServerConfig) {
  private val paginationDefault = serverConfig.paginationConfig.paginationDefault
  private val paginationMaxLimit = serverConfig.paginationConfig.paginationMaxLimit

  def getTopicsSortedByLastActive(offset: Option[Int], limit: Option[Int]): Future[Seq[Topic]] = {
    topicsRepository.getAllSortedByLastActivity(offset.getOrElse(0),
      Math.min(limit.getOrElse(paginationDefault), paginationMaxLimit))
  }

  def getTopicPosts(topicId: TopicId, postId: Option[PostId], elementsBefore: Option[Int], elementsAfter: Option[Int]): Future[Seq[Post]] = {
    val (limitedOffsetBefore, limitedOffsetAfter) = getOffsetsValues(elementsBefore, elementsAfter)
    postsRepository.getAll(topicId, postId, limitedOffsetBefore, limitedOffsetAfter)
  }

  private def getOffsetsValues(elementsBefore: Option[Int], elementsAfter: Option[Int]): (Int, Int) =
    (elementsBefore, elementsAfter) match {
      case (None, None) => (paginationDefault  / 2, paginationDefault  / 2)
      case (Some(before), None) => (Math.min(before, paginationMaxLimit - 1), 0)
      case (None, Some(after)) => (0, Math.min(after, paginationMaxLimit - 1))
      case (Some(before), Some(0)) => (Math.min(before, paginationMaxLimit - 1), 0)
      case (Some(0), Some(after)) => (0, Math.min(after, paginationMaxLimit - 1))
      case (Some(before), Some(after)) if before + after < paginationMaxLimit => (before, after)
      case (Some(before), Some(after)) =>
        val ration = before.toDouble/(before + after)
        ((ration * paginationMaxLimit).toInt, ((1-ration) * paginationMaxLimit - 1).toInt)
    }
}
