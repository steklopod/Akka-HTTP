# Кодирование / Декодирование

[Спецификация HTTP](http://tools.ietf.org/html/rfc7231#section-3.1.2.1) определяет заголовок кодировки содержимого, 
который указывает, является ли тело сущности сообщения 
HTTP “закодированным” и, если да, то каким алгоритмом. Только часто используемые кодировки алгоритмы сжатия.

В настоящее время Akka HTTP поддерживает сжатие и декомпрессию HTTP-запросов и ответов с помощью кодировок `gzip` или 
`deflate`. Основная логика для этого живет в пакете [akka.http.scaladsl.coding](http://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/coding/index.html).

## Серверная сторона
Поддержка не включается автоматически, но должна быть явно запрошена. Для включения кодирования/декодирования сообщений с 
[Routing DSL ](https://doc.akka.io/docs/akka-http/current/routing-dsl/index.html) см. [CodingDirectives](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/coding-directives/index.html).

## Клиентская сторона
В настоящее время на стороне клиента отсутствует высокоуровневая или автоматическая поддержка декодирования ответов.

В следующем примере показано, как декодировать ответы вручную на основе заголовка Content-Encoding:

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.{ Gzip, Deflate, NoCoding }
import akka.http.scaladsl.model._, headers.HttpEncodings
import akka.stream.ActorMaterializer

import scala.concurrent.Future

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()
import system.dispatcher

val http = Http()

val requests: Seq[HttpRequest] = Seq(
  "https://httpbin.org/gzip", // Content-Encoding: gzip in response
  "https://httpbin.org/deflate", // Content-Encoding: deflate in response
  "https://httpbin.org/get" // no Content-Encoding in response
).map(uri ⇒ HttpRequest(uri = uri))

def decodeResponse(response: HttpResponse): HttpResponse = {
  val decoder = response.encoding match {
    case HttpEncodings.gzip ⇒
      Gzip
    case HttpEncodings.deflate ⇒
      Deflate
    case HttpEncodings.identity ⇒
      NoCoding
  }

  decoder.decodeMessage(response)
}

val futureResponses: Future[Seq[HttpResponse]] =
  Future.traverse(requests)(http.singleRequest(_).map(decodeResponse))

futureResponses.futureValue.foreach { resp =>
  system.log.info(s"response is ${resp.toStrict(1.second).futureValue}")
}

system.terminate()
```



[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
