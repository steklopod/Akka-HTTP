# Типы данных и абстракции

## Модель HTTP

Модель HTTP Akka содержит глубоко структурированную, полностью неизменяемую модель на основе case-класса всех основных 
структур данных HTTP, таких как HTTP-запросы, ответы и общие заголовки. Он живет в модуле `akka-http-core` и является 
основой для большинства API Akka HTTP.

### Общее представление 
Поскольку `akka-http-core` предоставляет центральные структуры данных HTTP, вы найдете следующий импорт в нескольких 
местах вокруг кодовой базы (и, вероятно, ваш собственный код):

```scala
import akka.http.scaladsl.model._
```
Это приносит все самые уместные типы, главным образом:

* `HttpRequest` и `HttpResponse`, центральная модель сообщений;
* `headers`, пакет, содержащий все предопределенные модели HTTP заголовков и поддерживающие типы;
* Вспомогательные типы, как `URI`, `HttpMethods`, `MediaTypes`, `StatusCodes` и т.д.

Общим шаблоном является то, что модель определенной сущности представлена неизменяемым типом (классом или признаком), в 
то время как фактические экземпляры сущности, определенной спецификацией HTTP, живут в сопутствующем объекте, несущем 
имя типа.

### HttpRequest
`HttpRequest` и `HttpResponse` являются базовыми case-классами , представляющими сообщения HTTP.

[HttpRequest](https://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/model/HttpRequest.html) состоит из

* метода (GET, POST, и т. д.);
* URI ;
* последовательности заголовков;
* сущности (данные тела);
* протокола;

Вот несколько примеров того, как создать HttpRequest:

```scala
import HttpMethods._

// построит простой запрос GET к 'homeUri`
val homeUri = Uri("/abc")
HttpRequest(GET, uri = homeUri)

// построить простой GET запрос к "/index "(неявное преобразование строки в Uri)
HttpRequest(GET, uri = "/index")

// построить простой POST-запрос, содержащий сущность
val data = ByteString("abc")
HttpRequest(POST, uri = "/receive", entity = data)

// подгоняйте каждую деталь запроса HTTP
import HttpProtocols._
import MediaTypes._
import HttpCharsets._
val userData = ByteString("abc")
val authorization = headers.Authorization(BasicHttpCredentials("user", "pass"))
HttpRequest(
  PUT,
  uri = "/user",
  entity = HttpEntity(`text/plain` withCharset `UTF-8`, userData),
  headers = List(authorization),
  protocol = `HTTP/1.0`)
```

**Все параметры `HttpRequest.apply` имеют значения по умолчанию**, поэтому заголовки, например, не нужно указывать, если их нет. 
Многие типы параметров (например, `HttpEntity` и `Uri`) определяют неявные преобразования для распространенных случаев 
использования, чтобы упростить создание экземпляров запроса и ответа.

#### Синтетические Заголовки
В некоторых случаях может потребоваться отклониться от полностью RFC-совместимого поведения. Например, Amazon S3 
рассматривает символ + в части пути URL-адреса как пробел, даже если RFC указывает, что это поведение должно быть 
ограничено исключительно частью запроса URI.

Чтобы обойти эти типы граничных случаев, Akka HTTP предоставляет возможность предоставлять дополнительную нестандартную 
информацию запросу через синтетические заголовки. Эти заголовки не передаются клиенту, а используются обработчиком 
запросов для переопределения поведения по умолчанию.

Например, чтобы предоставить необработанный uri запроса, минуя нормализацию url по умолчанию, можно сделать следующее:

```scala
import akka.http.scaladsl.model.headers.`Raw-Request-URI`
val req = HttpRequest(uri = "/ignored", headers = List(`Raw-Request-URI`("/a/b%2Bc")))
```

### Объект HttpResponse
[HttpResponse](http://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/model/HttpResponse.html) состоит из

* кода состояния;
* последовательности заголовков;
* сущности (данные тела);
* протокола.

Вот несколько примеров того, как построить `HttpResponse`:

```scala
import StatusCodes._

// простой ответ OK без данных, созданных с использованием целочисленного кода состояния
HttpResponse(200)

// 404 ответ, созданный с использованием именованной константы StatusCode
HttpResponse(NotFound)

// 404 ответ с телом, объясняющим ошибку
HttpResponse(404, entity = "Unfortunately, the resource couldn't be found.")

// Ответ перенаправления, содержащий дополнительный заголовок
val locationHeader = headers.Location("http://example.com/other")
HttpResponse(Found, headers = List(locationHeader))
```

В дополнение к простым конструкторам [HttpEntity](https://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/model/HttpEntity.html),
 которые создают сущность из фиксированной строки или [ByteString](https://doc.akka.io/api/akka/2.5.12/akka/util/ByteString.html),
  как показано здесь, Модель HTTP Akka определяет ряд подклассов `HttpEntity`, которые позволяют указывать данные тела в 
  виде потока байтов.
  
### HttpEntity
`HttpEntity` передает байты данных сообщения вместе с его `Content-Type` и, если известно, его `Content-Length`. 
В Akka HTTP существует пять различных типов объектов, которые моделируют различные способы получения или отправки 
содержимого сообщения:

#### HttpEntity.Strict
Самый простой объект, который используется, когда все объекты уже доступны в памяти. Он обертывает простой 
`ByteString` и представляет собой стандартную, нефрагментированную сущность с известным Content-Length.
#### HttpEntity.Default
Общий, нефрагментированный объект сообщения `HTTP/1.1`. Он имеет известную длину и представляет свои данные как 
`Source[ByteString, _]`, который может быть осуществлен только один раз. Это ошибка, если предоставленный источник не 
производит точно столько же байтов, сколько указано. Различие `Strict` и `Default` - это только API-интерфейс. На 
проводе оба вида объектов выглядят одинаково.
#### HttpEntity.Chunked
Модель для `HTTP/1.1` фрагментированного контента (т.е. отправлена ​​с `Transfer-Encoding: chunked`). Длина содержимого 
неизвестна, а отдельные фрагменты представлены в качестве `Source[HttpEntity.ChunkStreamPart]`. `ChunkStreamPart` - это 
либо непустой `Chunk`, либо `LastChunk`, содержащий необязательные заголовки трейлера. Поток состоит из нуля или 
более частей `Chunked` и может быть завершен дополнительной частью `LastChunk`.
#### HttpEntity.CloseDelimited
`Unchunked` объект неизвестной длины, который неявно ограничивается закрытием соединения (`Connection: close`). 
Данные контента представлены как `Source[ByteString, _]`. Поскольку соединение должно быть закрыто после отправки 
объекта этого типа, его можно использовать только на стороне сервера для отправки ответа. Кроме того, основной целью 
объектов `CloseDelimited` является совместимость с одноранговыми узлами `HTTP/1.0`, которые не поддерживают кодирование 
с передачей пакетов. Если вы создаете новое приложение и не ограничены устаревшими требованиями, вам не следует 
полагаться на объекты CloseDelimited, поскольку неявное завершение соединения за закрытием не является надежным 
способом передачи ответа на конец ответа, особенно в присутствии прокси. Кроме того, этот тип сущности предотвращает 
повторное использование соединений, которое может серьезно ухудшить производительность. Вместо этого **используйте 
`HttpEntity.Chunked`**!
#### HttpEntity.IndefiniteLength
Потоковая сущность неопределенной длины для использования в `Multipart.BodyPart`.
Типы сущностей `Strict`, `Default` и `Chunked` - это подтип `HttpEntity.Regular`, который позволяет использовать их 
для запросов и ответов. Напротив, `HttpEntity.CloseDelimited` может использоваться только для ответов.

Типы потоковых объектов (т.е. все, кроме строгого) не могут быть разделены или сериализованы. Чтобы создать строгую, 
совместимую копию объекта или сообщения, используйте `HttpEntity.toStrict` или `HttpMessage.toStrict`, который возвращает 
`Future` объекта с данными тела, собранными в `ByteString`.

Сопутствующий объект `HttpEntity` содержит несколько вспомогательных конструкторов для простого создания объектов из 
обычных типов.

Вы можете сопоставить шаблон по подтипам `HttpEntity`, если вы хотите обеспечить специальную обработку для каждого из 
подтипов. Однако во многих случаях получатель `HttpEntity` не заботится о том, какой подтип является сущностью (и как 
данные транспортируются точно на уровне HTTP). Поэтому предоставляется общий метод `HttpEntity.dataBytes`, который возвращает 
`Source[ByteString, _]`, который позволяет получить доступ к данным объекта независимо от его конкретного подтипа.

>Когда использовать какой подтип?
* Используйте `Strict`, если количество данных «мало» и уже доступно в памяти (например, как `String` или `ByteString`);
* Использовать значение по умолчанию, если данные генерируются потоковым источником данных и известен размер данных;
* Использовать `Chunked` для объекта неизвестной длины;
* Используйте `CloseDelimited` для ответа в качестве старой альтернативы `Chunked`, если клиент не поддерживает 
кодирование с короткими передачами. В противном случае используйте `Chunked`;
* В `Multipart.BodyPart` используйте `IndefiniteLength` для содержимого неизвестной длины.

>Когда вы получаете нестрогое сообщение от соединения, тогда дополнительные данные считываются только из сети, когда вы 
запрашиваете их, потребляя поток данных сущности. Это означает, что если вы не потребляете поток объектов, соединение 
будет эффективно остановлено. В частности, никакое последующее сообщение (запрос или ответ) не будет считано из 
соединения, поскольку объект текущего сообщения «блокирует» поток. Поэтому вы должны убедиться, что вы всегда 
потребляете данные сущности, даже в том случае, если вас это действительно не интересует!

#### Ограничение длины объекта сообщения
Все объекты сообщений, которые Akka HTTP считывает из сети, автоматически получают прикрепленную к ним проверку 
проверки длины. Эта проверка гарантирует, что общий размер сущности меньше или равен сконфигурированному `max-content-length`,
 что является важной защитой от определенных атак типа «отказ в обслуживании». Тем не менее, один 
глобальный предел для всех запросов (или ответов) часто слишком негибкий для приложений, которым необходимо разрешить 
большие ограничения для некоторых запросов (или ответов), но хочет блокировать все сообщения, не принадлежащие к этой группе.

Чтобы предоставить вам максимальную гибкость при определении ограничений размера сущности в соответствии с вашими 
потребностями, `HttpEntity` имеет метод `withSizeLimit`, который позволяет вам настроить максимальный размер, 
настроенный на глобальном уровне, для этого конкретного объекта, будь то увеличение или уменьшение любого ранее 
установленного значения. Это означает, что ваше приложение получит все запросы (или ответы) из уровня HTTP, даже те, 
чья `Content-Length` превышает установленный лимит (потому что вы можете сами увеличить лимит). Только когда материальный 
поток источника данных, содержащийся в объекте, будет реализован, будут фактически применены пограничные проверки. В 
случае сбоя проверки длины соответствующий поток будет прерван с помощью `EntityStreamSizeException` либо непосредственно 
во время материализации (если длина контента известна), либо когда будут прочитаны больше байтов данных, чем разрешено.

При вызове строгих сущностей метод `withSizeLimit` вернет саму сущность, если длина находится в пределах привязки, в 
противном случае сущность по умолчанию с потоком данных одного элемента. Это позволяет впоследствии (до материализации 
потока данных) уточнить ограничение размера сущности.

По умолчанию все сущности сообщений, создаваемые уровнем HTTP, автоматически несут ограничение, заданное в параметре 
конфигурации `max-content-length` приложения. Если сущность преобразуется таким образом, что изменяется длина 
содержимого, а затем применяется другое ограничение, то это новое ограничение будет оцениваться относительно 
новой длины содержимого. Если сущность преобразуется таким образом, что изменяется длина содержимого, а новое 
ограничение не применяется, то предыдущее ограничение будет применено к предыдущей длине содержимого. Как правило, 
такое поведение должно соответствовать вашим ожиданиям.

#### Особенная обработка для главных запросов
[RFC 7230](http://tools.ietf.org/html/rfc7230#section-3.3.3) определяет очень четкие правила для длины сущности HTTP-сообщений.

Особенно это правило требует специальной обработки в Akka HTTP:
>Любой ответ на запрос HEAD и любой ответ с кодом состояния 1xx (информационный), 204 (без содержимого) или 304 (не измененный)
 всегда завершается первой пустой строкой после полей заголовка, независимо от полей заголовка, присутствующих в 
 сообщении, и, таким образом, не может содержать тело сообщения.
 
Ответы на запросы HEAD усложняют представление заголовков `Content-Length` или `Transfer-Encoding`, но сущность пуста. Это 
моделируется, позволяя `HttpEntity`.Это моделируется, позволяя `HttpEntity.Default` и `HttpEntity.Chunked` использоваться 
для ответов HEAD с пустым потоком данных.

Кроме того, когда ответ HEAD имеет `HttpEntity.CloseDelimited` сущность реализация Akka HTTP не закроет соединение после 
отправки ответа. Это позволяет отправлять ответы HEAD без заголовка Content-Length через постоянные HTTP-соединения.

### Модель Заголовка
Akka HTTP содержит богатую модель наиболее распространенных HTTP заголовков. Синтаксический анализ и визуализация 
выполняются автоматически, так что приложениям не нужно заботиться о фактическом синтаксисе заголовков. Заголовки не 
моделируются явно представлены в качестве `RawHeader`.

>См. следующие примеры работы с заголовками:

```scala
import akka.http.scaladsl.model.headers._

// создаст ``Location`` заголовок
val loc = Location("http://example.com/other")

// создайте заголовок` Authorization " с данными обычной проверки подлинности HTTP
val auth = Authorization(BasicHttpCredentials("joe", "josepp"))

// пользовательский тип
case class User(name: String, pass: String)

// метод, который извлекает основные учетные данные (`credentials`) HTTP из запроса
def credentialsOfRequest(req: HttpRequest): Option[User] =
  for {
    Authorization(BasicHttpCredentials(user, pass)) <- req.header[Authorization]
  } yield User(user, pass)
```

### HTTP Заголовки 
Когда HTTP-сервер Akka получает HTTP-запрос, он пытается проанализировать все свои заголовки в соответствующие классы 
модели. Независимо от того, будет ли это выполнено успешно или нет, уровень HTTP всегда будет передавать приложению 
все полученные заголовки. Неизвестные заголовки, а также заголовки с недопустимым синтаксисом (согласно анализатору 
заголовков) будут доступны в качестве экземпляров RawHeader. Для экспонирования разбор ошибок сообщение об ошибке 
регистрируется в зависимости от стоимости незаконно-заголовок-указания по конфигурации.

Некоторые заголовки имеют особый статус в HTTP и поэтому обрабатываются иначе, чем” обычные " заголовки:

#### Тип содержимого (`Content-Type`)
Тип содержимого сообщения HTTP моделируется как поле contentType `HttpEntity`. Таким образом, заголовок `Content-Type` 
не отображается в последовательности заголовков сообщения. Кроме того, экземпляр заголовка `Content-Type`, явно 
добавленный в заголовки запроса или ответа, не будет отображаться в проводнике и вызовет вместо этого предупреждение!

#### Передача-Кодирование (`Transfer-Encoding`)
Сообщения с `Transfer-Encoding: chunked` представлены через `HttpEntity.Chunked`. Как таковые фрагментированные 
сообщения, не имеющие другой более глубокой вложенной кодировки передачи, не будут иметь заголовка кодировки передачи в 
последовательности заголовков. Аналогично, экземпляр заголовка `Transfer-Encoding`, явно добавленный в заголовки 
запроса или ответа, не будет отображаться в проводнике и вызовет вместо этого запись в журнал предупреждения!

#### Длина содержимого (`Content-Length`)
Длина содержимого сообщения моделируется с помощью `HttpEntity`. Таким образом, заголовок `Content-Length` никогда не 
будет частью последовательности заголовков сообщений. Аналогично, экземпляр заголовка Content-Length, явно добавленный 
в заголовки запроса или ответа, не будет отображаться в проводнике и вызовет вместо этого предупреждение!

### Сервер (`Server`)
Заголовок сервера обычно добавляется автоматически к любому ответу и его значение может быть настроено через 
настройку `akka.http.server.server-header`. Кроме того, приложение может переопределить настроенный заголовок 
настраиваемым, добавив его в последовательность заголовков ответа.

#### Агент пользователя(`User-Agent`)
Заголовок User-Agent обычно добавляется автоматически к любому запросу и его значение может быть настроено через 
`akka.http.client.user-agent-heade`. Кроме того, приложение может переопределить настроенный заголовок настраиваемым, 
добавив его в последовательность заголовков запроса.

#### Дата (`Date`)
Заголовок ответа на дату добавляется автоматически, но может быть переопределен путем указания его вручную.

#### Соединение (`Connection`)
На стороне сервера Akka HTTP следит за явно добавленным соединением: закрывает заголовки ответов и, как таковые, 
учитывает потенциальное желание приложения закрыть соединение после отправки соответствующего ответа. Фактическая 
логика для определения, нужно ли закрывать соединение, довольно сложная. Он учитывает метод запроса, протокол и 
заголовок потенциального соединения, а также протокол ответа, сущность и заголовок потенциального соединения. 

#### Строгая транспортная безопасность (`Strict-Transport-Security`)
`HTTP Strict Transport Security (HSTS)` -это механизм политики веб-безопасности, который передается заголовком 
`Strict-Transport-Security`. Самые важные уязвимости, которые `HSTS` может исправить такое SSL-зачистка человек-в-середине 
атаки. Атака с удалением SSL работает путем прозрачного преобразования безопасного HTTPS-соединения в простое HTTP-соединение. 
Пользователь может видеть, что соединение небезопасно, но принципиально нет способа узнать, должно ли соединение быть 
безопасным. `HSTS` устраняет эту проблему, сообщая браузеру, что подключения к сайту всегда должны использовать `TLS/SSL`.

См. также:
* [пользовательский заголовок](https://doc.akka.io/docs/akka-http/current/common/http-model.html#custom-headers)
* [Синтаксический Анализ / Рендеринг](https://doc.akka.io/docs/akka-http/current/common/http-model.html#parsing-rendering)
* [Регистрация Пользовательских Типов Носителей](https://doc.akka.io/docs/akka-http/current/common/http-model.html#registering-custom-media-types)
* [Регистрация Пользовательских Кодов Состояния](https://doc.akka.io/docs/akka-http/current/common/http-model.html#registering-custom-status-codes)
* [Регистрация пользовательского метода HTTP](https://doc.akka.io/docs/akka-http/current/common/http-model.html#registering-custom-http-method)


[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
