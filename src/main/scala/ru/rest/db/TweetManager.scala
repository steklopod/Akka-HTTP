package ru.rest.db

import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import ru.rest.models.TweetEntity

import scala.concurrent.ExecutionContext.Implicits.global

//http://reactivemongo.org/releases/0.1x/documentation/tutorial/find-documents.html
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

//  val projection = BSONDocument("author" -> 1)
//  val query = BSONDocument("_id" -> BSONDocument("$gt" -> 1))

  def find =
    collection
      .find(emptyQuery)
//      .find(query, projection)
      .cursor[TweetEntity]()
      .collect[List]( 20, Cursor.FailOnError[List[TweetEntity]]())

  private def queryById(id: String) = BSONDocument("_id" -> BSONObjectID(id.getBytes()))

  private def emptyQuery = BSONDocument()
}