package confguration

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object ServerConfig {
  def fromConfig = {
    val host: String = ConfigFactory.load().getString("host")
    val port: Int = ConfigFactory.load().getInt("port")
    val paginationMaxLimit: Int = ConfigFactory.load().getInt("pagination.maxLimit")
    val paginationDefault: Int = Math.min(ConfigFactory.load().getInt("pagination.default"), paginationMaxLimit)
    val timeout: Duration = 60.second

    ServerConfig(host, port, timeout, PaginationConfig(paginationMaxLimit, paginationDefault))
  }
}

case class ServerConfig(host: String, port: Int, timeout: Duration, paginationConfig: PaginationConfig)

case class PaginationConfig(paginationMaxLimit: Int, paginationDefault: Int)
