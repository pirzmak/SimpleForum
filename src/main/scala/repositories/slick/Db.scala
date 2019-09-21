package repositories.slick

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration._

trait Db {
  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db

  def tableExists(name: String): Boolean = {
    Await.result(db.run(MTable.getTables), 60.seconds)
      .toList
      .map(_.name.name)
      .contains(name)
  }
}
