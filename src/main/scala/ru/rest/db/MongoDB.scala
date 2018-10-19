package ru.rest.db

import com.typesafe.config.ConfigFactory
import reactivemongo.api.MongoDriver

import scala.collection.JavaConverters._

//http://reactivemongo.org/releases/0.1x/documentation/tutorial/getstarted.html
object MongoDB {

  val config = ConfigFactory.load()
  val database = config.getString("mongodb.database")
  val servers = config.getStringList("mongodb.servers").asScala

  val driver = new MongoDriver
  val connection = driver.connection(servers)

  val db = connection(database)
}