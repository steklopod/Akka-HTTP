package ru.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import ru.client.JsonProtocol._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object HostLevel extends App {
  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec           = system.dispatcher

  // Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool]
  val poolClientFlow = Http().cachedHostConnectionPool[Int]("api.ipify.org")

  val responseFuture: Future[(Try[HttpResponse], Int)] =
    Source
      .single(HttpRequest(uri = "/?format=json") -> 4)
      .via(poolClientFlow)
      .runWith(Sink.head)

  responseFuture map {
    case (Success(res), _) =>
      res.status match {
        case OK =>
          Unmarshal(res.entity).to[IpInfo].map { info =>
            println(s"The information for my ip is: $info")
            shutdown()
          }
        case _ =>
          Unmarshal(res.entity).to[String].map { body =>
            println(s"The response status is ${res.status} and response body is ${body}")
            shutdown()
          }
      }
    case (Failure(err), i) =>
      println(s"Error Happened ${err}")
      shutdown()
  }

  def shutdown() = {
    Http().shutdownAllConnectionPools().onComplete { _ =>
      system.terminate()
      Await.ready(system.whenTerminated, 1 minute)
    }
  }

}