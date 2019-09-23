import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor
import commandServices.ForumCommandService
import confguration.ServerConfig
import model.{Post, Topic, TopicId, User}
import queryServices.ForumQueryService
import repositories.slick.{PostsRepositorySlickImpl, TopicsRepositorySlickImpl}
import routes.SimpleForumRoute

object Server {
  def start(): Unit = {
    startServer()
  }

  def startWithInitialValues(): Unit = {
    val topics = Range(1,1000).map(x => Topic("Test" + x, "Message", User("joe" + x, "joe@doe.com")))
    val posts = Range(1,1000).map(x => Post(TopicId(x % 10 + 1), "Message" + x, User("joe" + x, "joe@doe.com")))

    startServer(topics, posts)
  }

  private def startServer(initTopics: Seq[Topic] = Seq.empty,
                          initPosts: Seq[Post] = Seq.empty) = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val serverConfig = ServerConfig.fromConfig
    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("forum_db")

    val topicsRepository = new TopicsRepositorySlickImpl(databaseConfig)
    val postsRepository = new PostsRepositorySlickImpl(databaseConfig)

    for {
      _ <- topicsRepository.init(initTopics)
      _ <- postsRepository.init(initPosts)
    } yield ()

    val forumCommandService = new ForumCommandService(topicsRepository, postsRepository, serverConfig)
    val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, serverConfig)

    val mainRoute = new SimpleForumRoute(forumCommandService, forumQueryService, serverConfig)

    Http().bindAndHandle(mainRoute.route, serverConfig.host, serverConfig.port)

    println("Server started", serverConfig.host, serverConfig.port)
  }
}
