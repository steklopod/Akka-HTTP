package ru.rest.db

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import ru.rest.models.TweetEntity

import scala.concurrent.ExecutionContext.Implicits.global

object TweetManager {
  import MongoDB._

  val collection = db2.collection[BSONCollection]("tweets")
//  val collection = db2.map(_.collection("tweets"))

  def save(tweetEntity: TweetEntity) =
    collection
      .insert(tweetEntity)
      .map(_ => Created(tweetEntity.id.stringify))

  def findById(id: String) =
    collection
      .find(queryById(id))
      .one[TweetEntity]

  def deleteById(id: String) =
    collection
      .remove(queryById(id))
      .map(_ => Deleted)

  def find =
    collection
      .find(emptyQuery)
      .cursor[BSONDocument]
      .collect[List]()

  private def queryById(id: String) = BSONDocument("_id" -> BSONObjectID(id))

  private def emptyQuery = BSONDocument()
}