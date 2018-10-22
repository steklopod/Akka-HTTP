# Поддержка JSON

Инфраструктура маршалинга и немаршаллинга Akka HTTP позволяет легко и просто конвертировать объекты домена приложения 
из и в JSON. Интеграция с [spray-json](https://github.com/spray/spray-json) осуществляется из коробки через модуль `akka-http-spray-json`. Интеграция с другими 
библиотеками JSON поддерживается сообществом. 

### Поддержка spray-json
Трэйт `SprayJsonSupport` предоставляет `FromEntityUnmarshaller[Т]` и `ToEntityMarshaller[T]` для каждого типа `T`, и 
неявные `spray.json.RootJsonReader` и/или `spray.json.RootJsonWriter`.

Чтобы включить автоматическую поддержку (un)маршалинга из и в JSON с помощью `spray-json`, добавьте зависимость библиотеки на:

```sbtshell
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5"
```

Затем укажите `RootJsonFormat[T]` для своего типа и переведите его в область действия. 

Наконец, импортируйте `FromEntityUnmarshaller[T]` и `ToEntityMarshaller[Т]` неявные преобразования непосредственно из 
`SprayJsonSupport` как показано в примере ниже или подмешайте `akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport`
 трэйт в ваш модуль поддержки JSON.

После того, как вы сделали это (ан)маршалинг между JSON и вашим типом `T` должен работать красиво и прозрачно.

```scala
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

// domain model
final case class Item(name: String, id: Long)
final case class Order(items: List[Item])

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order) // contains List[Item]
}

// use it wherever json (un)marshalling is needed
class MyJsonService extends Directives with JsonSupport {

  // format: OFF
  val route =
    get {
      pathSingleSlash {
        complete(Item("thing", 42)) // will render as JSON
      }
    } ~
    post {
      entity(as[Order]) { order => // will unmarshal JSON to Order
        val itemsCount = order.items.size
        val itemNames = order.items.map(_.name).mkString(", ")
        complete(s"Ordered $itemsCount items: $itemNames")
      }
    }
  // format: ON
}
```
### Использование API стиля потоковой передачи JSON
Популярный способ реализации потокового API   - это JSON Streaming
В зависимости от способа API возвращает потоковый формат JSON (строки с разделителями, сырье последовательность объектов, или 
“бесконечное множество”) возможно, придется применить другое обрамление механизма, но общая идея остается неизменной: 
использование бесконечной сущности трансляцию и применении кадрирования к ней, такие, что единичные объекты могут быть 
легко десериализован с помощью обычной мобилизации инфраструктуры:

```scala
import MyJsonProtocol._
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.common.JsonEntityStreamingSupport

implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
  EntityStreamingSupport.json()

val input = """{"uid":1,"txt":"#Akka rocks!"}""" + "\n" +
  """{"uid":2,"txt":"Streaming is so hot right now!"}""" + "\n" +
  """{"uid":3,"txt":"You cannot enter the same river twice."}"""

val response = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, input))

// unmarshal:
val unmarshalled: Future[Source[Tweet, NotUsed]] =
  Unmarshal(response).to[Source[Tweet, NotUsed]]

// flatten the Future[Source[]] into a Source[]:
val source: Source[Tweet, Future[NotUsed]] =
  Source.fromFutureSource(unmarshalled)
```

В приведенном выше примере маршалинг обрабатывается неявно предоставленным `JsonEntityStreamingSupport`, который 
также используется при построении API потоковой передачи на стороне сервера. Можно также достигнуть того же самого 
более явно, вручную соединяя поток байтов сущности через кадрирование и затем этап десериализации:

```scala
import MyJsonProtocol._
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.common.JsonEntityStreamingSupport

implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
  EntityStreamingSupport.json()

val input = """{"uid":1,"txt":"#Akka rocks!"}""" + "\n" +
  """{"uid":2,"txt":"Streaming is so hot right now!"}""" + "\n" +
  """{"uid":3,"txt":"You cannot enter the same river twice."}"""

val response = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, input))

val value: Source[Tweet, Any] =
  response.entity.dataBytes
    .via(jsonStreamingSupport.framingDecoder) // pick your Framing (could be "\n" etc)
    .mapAsync(1)(bytes ⇒ Unmarshal(bytes).to[Tweet]) // unmarshal one by one
```

### Симпатичная печать (`Pretty printing`)
По умолчанию Spray-json выполняет маршалирование типов в компактный печатный JSON путем неявного преобразования с помощью 
`CompactPrinter`, как определено в:

```scala
implicit def sprayJsonMarshallerConverter[T](writer: RootJsonWriter[T])(implicit printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[T] =
  sprayJsonMarshaller[T](writer, printer)
```
В качестве альтернативы для маршалирования ваших типов в prettyprinter в области видимости для выполнения неявного преобразования.
```scala
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

// domain model
final case class PrettyPrintedItem(name: String, id: Long)

object PrettyJsonFormatSupport {
  import DefaultJsonProtocol._
  implicit val printer = PrettyPrinter
  implicit val prettyPrintedItemFormat = jsonFormat2(PrettyPrintedItem)
}

// use it wherever json (un)marshalling is needed
class MyJsonService extends Directives {
  import PrettyJsonFormatSupport._

  // format: OFF
  val route =
    get {
      pathSingleSlash {
        complete {
          PrettyPrintedItem("akka", 42) // will render as JSON
        }
      }
    }
  // format: ON
}

val service = new MyJsonService

// verify the pretty printed JSON
Get("/") ~> service.route ~> check {
  responseAs[String] shouldEqual
    """{""" + "\n" +
    """  "name": "akka",""" + "\n" +
    """  "id": 42""" + "\n" +
    """}"""
}
```

[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
