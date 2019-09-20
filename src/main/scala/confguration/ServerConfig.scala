package confguration

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object ServerConfig {
  val host: String = ConfigFactory.load().getString("host")
  val port: Int = ConfigFactory.load().getInt("port")
  val paginationMaxLimit: Int = ConfigFactory.load().getInt("pagination.paginationMaxLimit")
  val timeout: Duration = 60.second
}
