package ru.hello

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Client extends App {
  implicit val system           = ActorSystem()
  implicit val materializer     = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://akka.io"))

  responseFuture
    .onComplete {
      case Success(res) => println(res)
      case Failure(_)   => sys.error("something wrong")
    }
}
