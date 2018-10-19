# Введение

## Философия
`Akka HTTP` был ориентирован на четкую ориентацию на предоставление инструментов для построения интеграционных уровней, 
а не для ядер приложений. Как таковой, он рассматривает себя как набор библиотек, а не фрэймворк.

Фрэймворк, как мы хотели бы думать об этом термине, дает вам «рамку», в котором вы создаете свое приложение. Он поставляется 
с множеством решений, уже подготовленных и создающих основу, включая структуры поддержки, которые позволяют вам начать 
работу и быстро получать результаты. В некотором смысле структура похожа на скелет, на который вы помещаете «плоть» 
вашего приложения, чтобы оживить его. Поскольку такие структуры работают лучше всего, если вы выберете их, прежде чем 
приступать к разработке приложений, и старайтесь придерживаться «способов делать вещи» в рамках вашей карьеры.

_Например, если вы создаете веб-приложение, ориентированное на браузер, имеет смысл выбрать веб-фреймворк и создать 
приложение поверх него, потому что «ядром» приложения является взаимодействие браузера с вашим кодом на веб- сервер._ 
Создатели фреймворка выбрали один «проверенный» способ разработки таких приложений и позволили вам «заполнить пробелы» 
более или менее гибкого «шаблона приложения». Способность полагаться на такую ​​архитектуру с наилучшей практикой, как 
это, может быть большим преимуществом для быстрого решения поставленных задач.

Однако, если ваше приложение не является главным образом веб-приложением, поскольку его ядро ​​не является взаимодействием 
с браузером, а некоторым специализированным, возможно, сложным бизнес-сервисом, и вы просто пытаетесь подключить его 
к миру через REST/HTTP-интерфейс, веб-инфраструктура может не быть что вам нужно. В этом случае архитектура 
приложения должна быть продиктована тем, что имеет смысл для ядра, а не для интерфейса. Кроме того, вам, вероятно, не 
удастся воспользоваться возможно существующими компонентами инфраструктуры, такими как просмотр шаблонов, управление 
активами, генерация/манипуляция JavaScript/ CSS, поддержка локализации, поддержка AJAX и т.д.

**`Akka HTTP` был разработан специально как «не-фрэймворк»** не потому, что нам не нравятся фреймворки, но для случаев 
использования, когда фрэймворк не является правильным выбором. `Akka HTTP` создан для построения интеграционных уровней 
на основе HTTP и, как таковой, пытается «оставаться на обочине». Поэтому вы, как правило, не создаете свое приложение 
«поверх» `Akka HTTP`, но вы создаете приложение поверх того, что имеет смысл, и используйте `Akka HTTP` только для нужд 
интеграции HTTP.

С другой стороны, если вы предпочитаете создавать свои приложения с помощью фреймворка, вам следует попробовать [Play Framework](https://www.playframework.com/)
 или [Lagom](https://www.lagomframework.com/), которые оба используют Akka.

## Использование Akka HTTP
`Akka HTTP` предоставляется как независимые модули от самой Akka. `Akka HTTP` 
совместим с Akka 2.5 и любыми более поздними версиями 2.x. Однако модули не зависят от [akka-actor](https://github.com/steklopod/akka) и [akka-stream](https://github.com/steklopod/Akka-Streams), 
поэтому пользователю необходимо выбрать версию Akka для запуска и добавить ручную зависимость для `akka-stream` выбранной версии.

```sbtshell
"com.typesafe.akka" %% "akka-http"   % "10.1.5" 
"com.typesafe.akka" %% "akka-stream" % "2.5.17" // или какая бы ни была последняя версия
```

Имейте в виду, что Akka HTTP поставляется в двух основных модулях: **`akka-http`** и **`akka-http-core`**. Поскольку 
`akka-http` зависит от `akka-http-core`, вам не нужно явно указывать последнее. Тем не менее вам может понадобиться 
сделать это, если вы полагаетесь исключительно на низкоуровневый API; убедитесь, что версия Scala является последней 
версией версии `2.11` или **`2.12`**.

Кроме того, вы можете загрузить новый проект `sbt` с помощью HTTP-сервера Akka, уже настроенного с использованием шаблона [Giter8](http://www.foundweekends.org/giter8/):
```sbtshell
sbt -Dsbt.version=0.13.15 new https://github.com/akka/akka-http-scala-seed.g8
```
>Дополнительные инструкции можно найти в [проекте шаблона](https://github.com/akka/akka-http-scala-seed.g8).

## Маршрутизация DSL для HTTP-серверов
Высокоуровневый API маршрутизации **`Akka HTTP` предоставляет DSL для описания «маршрутов» HTTP и того, как их следует 
обрабатывать**. Каждый маршрут состоит из одного или нескольких уровней директивы (**`Directive`**), которые сужаются 
до обработки одного конкретного типа запроса.

Например, один маршрут может начинаться с соответствия пути запроса, только совпадающего, если он `/hello`, затем 
сужает его только для обработки запросов HTTP-запроса `get`, а затем завершает (`omplete`) их с строковым литералом, который будет 
отправлен обратно как `HTTP OK` со строкой в ​​качестве тела ответа.

Преобразование тел (`body`) запроса и ответа между форматами и проводными форматами, которые будут использоваться в вашем 
приложении, осуществляется отдельно от объявлений маршрутов, в маршаллерах, которые неявно используются с использованием 
шаблона «магнит» (`magnet`). Это означает, что вы можете выполнить (`complete`) запрос с любым видом объекта, если 
имеется неявный маршаллер, доступный в области видимости.

Маршрутизаторы по умолчанию предоставляются для простых объектов, таких как `String` или `ByteString`, и вы можете определить 
свои собственные, например, для `JSON`. Дополнительный модуль обеспечивает сериализацию JSON с использованием библиотеки 
**`spray-json`** (подробнее см. [JSON Support](https://doc.akka.io/docs/akka-http/current/common/json-support.html)).

[Маршрут (Route)](http://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/server/index.html#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future%5Bakka.http.scaladsl.server.RouteResult%5D),
 созданный с использованием маршрутной DSL, затем «привязан» к порту, чтобы начать обслуживать HTTP-запросы:

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

object WebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // нужна для будущего flatMap / onComplete в итоге
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()       // пусть он работает, пока пользователь не нажмет return
    bindingFuture
      .flatMap(_.unbind()) // триггер отвязки от порта
      .onComplete(_ => system.terminate()) // и выключение когда сделанный
  }
}
```
Когда вы запустите этот сервер, вы можете открыть страницу в браузере по следующему URL: `http://localhost:8080/hello` 
или вызвать его в своем терминале через `curl http://localhost:8080/hello`.

Общим вариантом использования является ответ на запрос с использованием объекта модели, в котором маршаллер преобразует 
его в JSON. В этом случае показаны два отдельных маршрута. Первый маршрут запрашивает асинхронную базу данных и выводит 
результат `Future[Option[Item]]` в ответ JSON. Второй разворачивает ордер из входящего запроса, сохраняет его в базу 
данных и отвечает на это с помощью OK, когда это делается.

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

import scala.concurrent.Future

object WebServer {

  // необходимо запустить маршрут
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // необходима для `future map/flatmap` в конце и `future in fetchItem` и `saveOrder`
  implicit val executionContext = system.dispatcher

  var orders: List[Item] = Nil

  // модель предметной области (domain model)
  final case class Item(name: String, id: Long)
  final case class Order(items: List[Item])

  // форматы для демаршалинга и маршалинга
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  // (фейк) асинхронный api запросов к базе данных
  def fetchItem(itemId: Long): Future[Option[Item]] = Future {
    orders.find(o => o.id == itemId)
  }
  def saveOrder(order: Order): Future[Done] = {
    orders = order match {
      case Order(items) => items ::: orders
      case _            => orders
    }
    Future { Done }
  }

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("item" / LongNumber) { id =>
          // для данного идентификатора может отсутствовать элемент
          val maybeItem: Future[Option[Item]] = fetchItem(id)

          onSuccess(maybeItem) {
            case Some(item) => complete(item)
            case None       => complete(StatusCodes.NotFound)
          }
        }
      } ~
        post {
          path("create-order") {
            entity(as[Order]) { order =>
              val saved: Future[Done] = saveOrder(order)
              onComplete(saved) { done =>
                complete("order created")
              }
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()       // пусть он работает, пока пользователь не нажмет return
    bindingFuture
      .flatMap(_.unbind()) // триггер отвязки от порта
      .onComplete(_ => system.terminate()) // и выключение когда сделанный
  }
}
```

Когда вы запустите этот сервер, вы можете обновить значение через:
>url -H "Content-Type: application/json" -X POST -d '{"items":[{"name":"hhgtg","id":42}]}' http://localhost:8080/create-order 

или на вашем терминале-добавление элемента с именем `hhgtg` и имеющий `id=42`; а затем просмотреть инвентарь либо в браузере, по url: 
>http://localhost:8080/item/42

или на терминале
>curl http://localhost:8080/item/42

Логика для сортировки и разборки JSON в этом примере предоставляется библиотекой «spray-json» 

Одна из сильных сторон Akka HTTP заключается в том, что потоковые данные находятся в самом сердце, а это означает, что 
тела запросов и ответов могут передаваться через сервер, обеспечивая постоянное использование памяти даже для очень 
больших запросов или ответов. Потоковые ответы будут `обратным давлением` удаленного клиента, так что сервер не будет 
толкать данные быстрее, чем клиент может обрабатывать, потоковые запросы означает, что **сервер решает, как быстро 
удаленный клиент может толкать данные тела запроса**.

Пример, который передает случайные числа, если клиент принимает их:

```scala
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, ContentTypes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.util.Random
import scala.io.StdIn

object WebServer {

  def main(args: Array[String]) {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // нужна для `future flatMap/onComplete` в итоге
    implicit val executionContext = system.dispatcher

    // потоки можно использовать повторно, поэтому мы можем определить их здесь
    // и использовать его для каждого запроса
    val numbers = Source.fromIterator(() =>
      Iterator.continually(Random.nextInt()))

    val route =
      path("random") {
        get {
          complete(
            HttpEntity(
              ContentTypes.`text/plain(UTF-8)`,
              // преобразуйте каждое число в кусок (`chunk`) байтов
              numbers.map(n => ByteString(s"$n\n"))
            )
          )
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()       // пусть он работает, пока пользователь не нажмет return
    bindingFuture
      .flatMap(_.unbind()) // триггер отвязки от порта
      .onComplete(_ => system.terminate()) // и выключение когда сделанный
  }
}
```

Подключение к этой службе с помощью медленного HTTP-клиента приведет к обратному давлению, так что следующее случайное 
число будет создаваться по требованию с постоянным использованием памяти на сервере. Это можно увидеть, используя 
curl и ограничивая скорость:
 >curl --limit-rate 50b 127.0.0.1:8080/random

Маршруты `Akka HTTP`  легко взаимодействует с акторами. В этом примере один маршрут позволяет размещать ставки в 
стиле "огонь-и-забыть", а второй маршрут содержит взаимодействие "запрос-ответ" с субъектом. Полученный ответ 
отображается в формате json и возвращается при получении ответа от субъекта.

```scala
import akka.actor.{Actor, ActorSystem, Props, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

object WebServer {

  case class Bid(userId: String, offer: Int)
  case object GetBids
  case class Bids(bids: List[Bid])

  class Auction extends Actor with ActorLogging {
    var bids = List.empty[Bid]
    def receive = {
      case bid @ Bid(userId, offer) =>
        bids = bids :+ bid
        log.info(s"Bid complete: $userId, $offer")
      case GetBids => sender() ! Bids(bids)
      case _ => log.info("Invalid message")
    }
  }

  // these are from spray-json
  implicit val bidFormat = jsonFormat2(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val auction = system.actorOf(Props[Auction], "auction")

    val route =
      path("auction") {
        put {
          parameter("bid".as[Int], "user") { (bid, user) =>
            // сделайте ставку, `огонь-и-забудь`
            auction ! Bid(user, bid)
            complete((StatusCodes.Accepted, "bid placed"))
          }
        } ~
        get {
          implicit val timeout: Timeout = 5.seconds

          // запрос субъекта для текущего состояния аукциона
          val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
          complete(bids)
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()       // пусть он работает, пока пользователь не нажмет return
    bindingFuture
      .flatMap(_.unbind()) // триггер отвязки от порта
      .onComplete(_ => system.terminate()) // и выключение когда сделанный
  }
}
```

При запуске этого сервера вы можете добавить ставку аукциона через терминал:
>curl -X PUT http://localhost:8080/auction?bid=22&user=MartinO 
 
и тогда вы можете просмотреть статус аукциона либо в браузере, по url:
>http://localhost:8080/auction, 

или, на терминале, через 
>curl http://localhost:8080/auction

Дополнительные сведения о том, как работает маршалинг и демаршалинг JSON, можно найти в разделе [поддержка JSON](https://doc.akka.io/docs/akka-http/current/common/json-support.html).

Подробнее о высокоуровневых API читайте в разделе [высокоуровневые серверные API (High-level Server-Side API)](https://doc.akka.io/docs/akka-http/current/routing-dsl/index.html).

[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)
_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
