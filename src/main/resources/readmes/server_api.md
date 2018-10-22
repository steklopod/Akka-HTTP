# Серверное API

Помимо HTTP-клиента, Akka HTTP также предоставляет встроенный, основанный на реактивных потоках, полностью асинхронный 
сервер HTTP/1.1, реализованный поверх потоков.

Он поддерживает следующие функции:

* Полная поддержка постоянных соединений HTTP
* Полная поддержка HTTP конвейеризации
* Полная поддержка асинхронной потоковой передачи HTTP, включая” фрагментированную " кодировку передачи, доступную через идиоматический API
* Дополнительное шифрование SSL/TLS
* Поддержка WebSocket 

Серверные компоненты Akka HTTP разделены на два уровня:

[Core Server API](https://doc.akka.io/docs/akka-http/current/server-side/low-level-api.html)
Базовая низкоуровневая реализация сервера в модуле `akka-http-core`.

[High-level Server-Side API](https://doc.akka.io/docs/akka-http/current/routing-dsl/index.html)
Высокоуровневая функциональность в модуле `akka-http`, который предлагает очень гибкую "маршрутизацию DSL" для элегантного 
определения RESTful веб-сервисов, а также функциональность типичных веб-серверов или фреймворков, таких как деконструкция URI, 
согласование контента или статическое обслуживание контента.

В зависимости от ваших потребностей вы можете либо использовать низкоуровневый API напрямую, либо полагаться на 
высокоуровневый DSL маршрутизации, который может сделать определение более сложной логики службы намного проще. Вы 
также можете взаимодействовать с различными уровнями API одновременно и, независимо от того, какой уровень API вы выберете, 
Akka HTTP будет счастливо обслуживать много тысяч одновременных подключений к одному или нескольким различным клиентам.

>Рекомендуется прочитать  [Implications of the streaming nature of Request/Response Entities](https://doc.akka.io/docs/akka-http/current/implications-of-streaming-http-entity.html) 
раздел, поскольку это объясняет основные 
понятия потоковой передачи полного стека, которые могут быть неожиданными, прибывая из фона с не “потоковыми первыми” 
серверами HTTP.

## API главного сервера
Область действия API главного сервера четко сфокусирована на основных функциях сервера HTTP/1.1:

* Управление соединением
* Парсинг и рендеринг сообщений и заголовков
* Управление временем ожидания (для запросов и подключений)
* Заказ ответа (для поддержки прозрачного конвейеризации)

Все непрофильные функции типичного HTTP-серверов (например, маршрутизация запросов, файл, сжатия, и т. д.) оставлены на 
более высоких уровнях, они не реализованы самим сервером `akka-http-core-level`. Помимо общей направленности этот дизайн 
сохраняет ядро сервера небольшим и легким, а также легко понять и поддерживать.

### Потоки и HTTP
HTTP - сервер Akka реализован поверх потоков и активно использует его-как в своей реализации, так и на всех уровнях своего API.

На уровне соединения Akka HTTP предлагает в основном тот же интерфейс, что и работа с потоковым вводом-выводом: привязка 
сокета представлена как поток входящих соединений. Приложение извлекает соединения из этого источника потока и для 
каждого из них предоставляет ` Flow[HttpRequest, HttpResponse, _]` для “перевода” запросов в ответы.

Помимо относительно розетка обязана на стороне сервера в качестве ` Source[IncomingConnection, _] ` и каждое соединение в 
качестве `Source[HttpRequest, _]` с `Sink[HttpResponse, _]` поток абстракции также присутствует внутри 
один HTTP сообщение: объекты HTTP-запросов и ответов, как правило, моделируется как ` Source[ByteString, _]`. 

### Запуск и остановка
На самом базовом уровне сервер HTTP Akka связан, вызывая bind метод [akka.http.scaladsl.Http](http://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/Http$.html) расширение:

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()
implicit val executionContext = system.dispatcher

val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
  Http().bind(interface = "localhost", port = 8080)
val bindingFuture: Future[Http.ServerBinding] =
  serverSource.to(Sink.foreach { connection => // foreach materializes the source
    println("Accepted new connection from " + connection.remoteAddress)
    // ... and then actually handle the connection
  }).run()
```

Аргументы метода `Http().bind` указывают интерфейс и порт для привязки и регистрации интереса к обработке входящих 
HTTP-соединений. Кроме того, этот метод также позволяет определять параметры сокета, а также большее количество 
настроек для настройки сервера в соответствии с вашими потребностями.

Результатом метода bind является ` Source[Http.IncomingConnection]`, который должен быть удален приложением для приема 
входящих соединений. Фактическое связывание не выполняется до того, как этот источник будет реализован как часть 
конвейера обработки. В случае сбоя связи (например, поскольку порт уже занят) материализованный поток немедленно 
прекращается с соответствующим исключением. Связывание освобождается (т. Е. Базовый сокет не подключается), когда 
абонент источника входящего соединения отменил его подписку. В качестве альтернативы можно использовать метод 
`unbind()` экземпляра `Http.ServerBinding`, который создается как часть процесса материализации процесса подключения. 
`Http.ServerBinding` также обеспечивает способ удержания фактического локального адреса связанного сокета, который полезен, 
например, при привязке к нулевому порту (и, таким образом, позволяет OS выбрать доступный порт).

### Цикл "Запрос-Ответ"
Когда новое соединение будет принято, оно будет опубликовано как `Http.IncomingConnection`, который состоит из удаленного 
адреса и методов для обеспечения ` Flow[HttpRequest, HttpResponse, _]` для обработки запросов, поступающих через это соединение.

Запросы обрабатываются вызовом одного из методов `handleWithXXX` с обработчиком, который может быть

* `Flow[HttpRequest, HttpResponse,_]` для `handleWith`;
* функция `HttpRequest => HttpResponse` для `handleWithSyncHandler`;
* функция `HttpRequest => Future[HttpResponse]` для `handleWithAsyncHandler`.

Вот полный пример:

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()
implicit val executionContext = system.dispatcher

val serverSource = Http().bind(interface = "localhost", port = 8080)

val requestHandler: HttpRequest => HttpResponse = {
  case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
    HttpResponse(entity = HttpEntity(
      ContentTypes.`text/html(UTF-8)`,
      "<html><body>Hello world!</body></html>"))

  case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
    HttpResponse(entity = "PONG!")

  case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
    sys.error("BOOM!")

  case r: HttpRequest =>
    r.discardEntityBytes() // важно для слива входящего потока сущностей HTTP
    HttpResponse(404, entity = "Unknown resource!")
}

val bindingFuture: Future[Http.ServerBinding] =
  serverSource.to(Sink.foreach { connection =>
    println("Accepted new connection from " + connection.remoteAddress)

    connection handleWithSyncHandler requestHandler
    // это эквивалентно: connection handleWith { Flow[HttpRequest] map requestHandler }
  }).run()
```

В этом примере запрос обрабатывается путем преобразования потока запросов с помощью функции `HttpRequest => HttpResponse` с 
использованием `handleWithSyncHandler` (или, что то же самое, оператора `map` Akka Stream). В зависимости от случая 
использования многие другие способы предоставления обработчика запроса возможны с помощью комбинаторов `Akka Stream`. 
Если приложение предоставляет поток, также ответственность приложения состоит в том, чтобы генерировать ровно один 
ответ для каждого запроса и что упорядочение ответов соответствует упорядочению связанных запросов (что имеет значение, 
если HTTP-конвейерная обработка включена, когда обработка нескольких входящих запросов могут перекрываться). Если 
полагаться на `handleWithSyncHandler` или `handleWithAsyncHandler`, или на оператора `map` или `mapAsync`, это требование 
будет автоматически выполнено.


#### Потоковые Сущности Запроса / Ответа
Потоковое лиц сообщение http поддерживается через подклассы `HttpEntity`. Приложение должно иметь возможность работать с 
потоковыми сущностями при получении запроса, а также, во многих случаях, при построении ответов. 

Если вы полагаетесь на средства маршалинга и/или Демаршалинга, предоставляемые Akka HTTP, то преобразование 
пользовательских типов В и из потоковых сущностей может быть довольно удобным.

####Закрытие соединения
Соединение HTTP будет закрыто, когда поток обработки отменит свою восходящую подписку или узел закроет соединение. 
Часто более удобной альтернативой является явное добавление заголовка `Connection: close` в `HttpResponse`. Этот ответ будет 
последним в соединении, и сервер будет активно закрывать соединение после его отправки.

Соединение также будет закрыто, если объект запроса был отменен (например, привязав его к `Sink.cancelled()` или 
потребляемый только частично (например, с помощью комбинатора). Чтобы предотвратить это поведение, объект должен быть 
явно удален, подключив его к `Sink.ignore()`.

[подробнее](https://doc.akka.io/docs/akka-http/current/server-side/low-level-api.html#stand-alone-http-layer-usage)


[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
