package ru.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object HighLevel extends App {
  implicit val system       = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec           = system.dispatcher

  /*
    path(RESOURCE_PATH) {
      method [get|put|post ...] {
        complete {
          // верните ответ, в случае, если вы принимаете этот запрос.
        }
        reject {
          // верните ответ, если вы отклоните этот запрос.
        }
      }
    }
   */
  val route =
    path("") {
      get {
        complete("Hello Akka HTTP Server Side API - High Level")
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
