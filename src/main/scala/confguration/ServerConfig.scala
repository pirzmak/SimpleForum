package confguration

import com.typesafe.config.ConfigFactory

object ServerConfig {
  val host: String = ConfigFactory.load().getString("host")
  val port: Int = ConfigFactory.load().getInt("port")
  val paginationMaxLimit: Int = ConfigFactory.load().getInt("pagination.paginationMaxLimit")
}
