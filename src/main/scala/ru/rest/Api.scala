package ru.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import ru.rest.db.TweetManager
import ru.rest.models.Tweet
import spray.json._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

trait RestApi {
  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val ec: ExecutionContext

  val route =
    pathPrefix("tweets"){
      (post & entity(as[Tweet])) { tweet =>
        complete {
          TweetManager.save(tweet) map { r =>
            Created -> Map("id" -> r.id).toJson
          }
        }
      } ~
        (delete & path(Segment)) { id =>
          complete {
            TweetManager.deleteById(id) map { _ =>
              NoContent
            }
          }
        }
//      TODO
//      ~
//        (get & path(Segment)) { id =>
//          complete {
//            TweetManager.findById(id)
//            map { t =>
//              OK -> t
//            }
//          }
//        } ~

//        get {
//          complete {
//            TweetManager.find map { ts =>
//              OK -> ts.map(_.as[TweetEntity])
//            }
//          }
//        }
    }

}
object Api extends App with RestApi {
  override implicit val system = ActorSystem("rest-api")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher
 
  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
 
  bindingFuture
    .flatMap(_.unbind()) 
    .onComplete(_ => system.terminate())
}