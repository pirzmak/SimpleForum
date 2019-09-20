package repositories.slick

import confguration.ServerConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.Await

trait Db {
  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db

  def tableExists(name: String): Boolean = {
    Await.result(db.run(MTable.getTables), ServerConfig.timeout)
      .toList
      .map(_.name.name)
      .contains(name)
  }
}
