import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

import commandServices.ForumCommandService
import confguration.ServerConfig
import queryServices.ForumQueryService
import repositories.slick.{PostsRepositorySlickImpl, TopicsRepositorySlickImpl}
import routes.SimpleForumRoute

object Server {
  def start() = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val serverConfig = ServerConfig.fromConfig
    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("forum_db")


    val topicsRepository = new TopicsRepositorySlickImpl(databaseConfig)
    val postsRepository = new PostsRepositorySlickImpl(databaseConfig)

    topicsRepository.init()
    postsRepository.init()

    val forumCommandService = new ForumCommandService(topicsRepository, postsRepository)
    val forumQueryService = new ForumQueryService(topicsRepository, postsRepository, serverConfig)

    val mainRoute = new SimpleForumRoute(forumCommandService, forumQueryService, serverConfig)

    Http().bindAndHandle(mainRoute.route, serverConfig.host, serverConfig.port)

    println("Server started", serverConfig.host, serverConfig.port)
  }
}
