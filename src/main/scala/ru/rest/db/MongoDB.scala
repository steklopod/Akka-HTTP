package ru.rest.db

import com.typesafe.config.ConfigFactory
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

//http://reactivemongo.org/releases/0.1x/documentation/tutorial/getstarted.html
object MongoDB {

  val config = ConfigFactory.load()
  val database = config.getString("mongodb.database")
  val servers = config.getStringList("mongodb.servers").asScala

  val driver = new MongoDriver
  val connection: MongoConnection = driver.connection(servers)

//  val db = connection(database)
  val db: Future[DefaultDB] = connection.database(database)
  val db2: DefaultDB = Await.result(db,10 seconds)

}