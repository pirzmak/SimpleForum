package confguration

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object ServerConfig {
  def fromConfig = {
    val host: String = ConfigFactory.load().getString("host")
    val port: Int = ConfigFactory.load().getInt("port")

    val timeout: Duration = ConfigFactory.load().getInt("timeout").second

    val paginationMaxLimit: Int = ConfigFactory.load().getInt("pagination.maxLimit")
    val paginationDefault: Int = Math.min(ConfigFactory.load().getInt("pagination.default"), paginationMaxLimit)

    val emailRegex: String = ConfigFactory.load().getString("validations.emailRegex")
    val postMinLength: Int = ConfigFactory.load().getInt("validations.postMinLength")
    val postMaxLength: Int = ConfigFactory.load().getInt("validations.postMaxLength")
    val topicTitleMinLength: Int = ConfigFactory.load().getInt("validations.topicTitleMinLength")
    val topicTitleMaxLength: Int = ConfigFactory.load().getInt("validations.topicTitleMaxLength")
    val nickMinLength: Int = ConfigFactory.load().getInt("validations.nicknameMinLength")
    val nickMaxLength: Int = ConfigFactory.load().getInt("validations.nicknameMaxLength")

    ServerConfig(host, port, timeout,
      PaginationConfig(paginationMaxLimit, paginationDefault),
      ValidationConfig(emailRegex,
        postMinLength, postMaxLength,
        topicTitleMinLength, topicTitleMaxLength,
        nickMinLength, nickMaxLength))
  }
}

case class ServerConfig(host: String, port: Int, timeout: Duration,
                        paginationConfig: PaginationConfig,
                        validationConfig: ValidationConfig)

case class PaginationConfig(paginationMaxLimit: Int, paginationDefault: Int)

case class ValidationConfig(emailRegex: String,
                            postMinLength: Int, postMaxLength: Int,
                            topicTitleMinLength: Int, topicTitleMaxLength: Int,
                            nickMinLength: Int, nickMaxLength: Int)
