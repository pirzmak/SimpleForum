package repositories.slick.mocks

import models.{Topic, TopicId}
import repositories.interfaces.TopicsRepository

import scala.concurrent.Future

class TopicsRepositoryMock extends TopicsRepository {
  var db: Map[TopicId, Topic] = Map.empty

  override def init(initValues: Seq[Topic]): Future[Unit] = {
    db = initValues.zipWithIndex.map(p => (TopicId(p._2), p._1.copy(id = Some(TopicId(p._2))))).toMap
    Future.successful()
  }

  override def drop(): Future[Unit] = {
    db = Map.empty
    Future.successful()
  }

  override def createNew(topic: Topic): Future[TopicId] = {
    val nextId = TopicId(db.last._1.value + 1)
    db = db + (nextId -> topic.copy(id = Some(nextId)))
    Future.successful(nextId)
  }

  override def getById(topicId: TopicId): Future[Option[Topic]] = {
    Future.successful(db.get(topicId))
  }

  override def getAllSortedByLastActivity(offset: Int, limit: Int): Future[Seq[Topic]] = {
    val list = db.toList.map(_._2).sortBy(_.lastModified).reverse.slice(offset, offset + limit)
    Future.successful(list)
  }
}
