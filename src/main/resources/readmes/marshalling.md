# Маршаллинг

Маршалинг-это процесс преобразования структуры более высокого уровня (объекта) в некое представление более низкого уровня, 
часто “формат wire”. Другими популярными названиями для маршалинга являются” сериализация “или”маринование".

В Akka HTTP маршалинг означает преобразование объекта типа T в целевой тип более низкого уровня, например `MessageEntity`
(который формирует “тело сущности” HTTP-запроса или ответа) или полный `HttpRequest` или `HttpResponse`.

Например, на стороне сервера маршалинг используется для преобразования объекта домена приложения в сущность ответа. 
Запросы могут содержать заголовок Accept, в котором перечислены допустимые типы содержимого для клиента, такие как 
`application/json` и `application/xml`. Маршаллер содержит логику согласования типов контента результатов на основе 
заголовков [Accept](http://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/model/headers/Accept.html) и `AcceptCharset`.


## Базовая конструкция (`Basic Design`)
Маршалинг экземпляров типа `A` в экземпляры типа `B` выполняется с помощью [ Marshaller[A, B]](http://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/marshalling/Marshaller.html).

Вопреки тому, что вы могли бы сначала ожидать,  `Marshaller[А, B]` - это не обычная функция `A => B`, но а по сути 
функция `A => Future[List[Marshalling[B]]]`. Давайте разберем этот довольно сложный вид подписи на части, чтобы понять, 
почему `marshallers` разработаны таким образом. Учитывая экземпляр типа `A` ` Marshaller[A, B] ` производит:

1. `Future`: Это, вероятно, совершенно ясно. Маршаллам не требуется синхронно производить результат, поэтому вместо 
этого они возвращают будущее, что позволяет асинхронности в процессе сортировки.

2. из списка (`List`): вместо одного единственного целевого представления для маршаллов A может предложить несколько. Какой из них 
будет передан на провод в конце, определяется путем согласования контента. Например, 
`Marshaller [OrderConfirmation, MessageEntity]` может предлагать JSON, а также представление XML. Клиент может решить, 
добавив заголовок запроса принятия, который является предпочтительным. Если клиент не выражает предпочтения, выбирается 
первое представление.

3. `Marshalling[B]`: вместо того, чтобы возвращать экземпляр B непосредственно маршаллов, сначала создайте 
`Marshalling[B]`. Это позволяет запрашивать MediaType и, возможно, HttpCharset, которые будет производиться маршаллером до 
запуска фактического сортировки. Помимо разрешения согласования контента этот проект позволяет задержать фактическое 
построение сортировочного целевого экземпляра до самого последнего момента, когда это действительно необходимо.

Вот как определяется Маршаллинг:

```scala
/**
 * Описывает один из возможных вариантов использования заданного значения.
 */
sealed trait Marshalling[+A] {
  def map[B](f: A ⇒ B): Marshalling[B]

  /**
   * Преобразует эту маршалинг в непрозрачную маршалинг, т. е. результат маршалинга, который
   * не участвует в согласовании типов контента. Данная кодировка используется, если
   * экземпляр `WithOpenCharset` кроссировка.
   */
  def toOpaque(charset: HttpCharset): Marshalling[A]
}

object Marshalling {

  /**
   * Маршалинг к определенному  [[akka.http.scaladsl.model.ContentType]].
   */
  final case class WithFixedContentType[A](
    contentType: ContentType,
    marshal:     () ⇒ A) extends Marshalling[A] {
    def map[B](f: A ⇒ B): WithFixedContentType[B] = copy(marshal = () ⇒ f(marshal()))
    def toOpaque(charset: HttpCharset): Marshalling[A] = Opaque(marshal)
  }

  /**
   * Маршалинг к определенному  [[akka.http.scaladsl.model.MediaType]] с гибкой кодировкой.
   */
  final case class WithOpenCharset[A](
    mediaType: MediaType.WithOpenCharset,
    marshal:   HttpCharset ⇒ A) extends Marshalling[A] {
    def map[B](f: A ⇒ B): WithOpenCharset[B] = copy(marshal = cs ⇒ f(marshal(cs)))
    def toOpaque(charset: HttpCharset): Marshalling[A] = Opaque(() ⇒ marshal(charset))
  }

  /**
   * Маршалинг к неизвестному типу носителя и кодировке.
   * Обходит согласование контента.
   */
  final case class Opaque[A](marshal: () ⇒ A) extends Marshalling[A] {
    def map[B](f: A ⇒ B): Opaque[B] = copy(marshal = () ⇒ f(marshal()))
    def toOpaque(charset: HttpCharset): Marshalling[A] = this
  }
}
```

Akka HTTP также определяет ряд полезных псевдонимов для типов маршаллеров, с которыми вы, скорее всего, будете работать:

```scala
type ToEntityMarshaller[T] = Marshaller[T, MessageEntity]
type ToByteStringMarshaller[T] = Marshaller[T, ByteString]
type ToHeadersAndEntityMarshaller[T] = Marshaller[T, (immutable.Seq[HttpHeader], MessageEntity)]
type ToResponseMarshaller[T] = Marshaller[T, HttpResponse]
type ToRequestMarshaller[T] = Marshaller[T, HttpRequest]
```

### Предопределенный Marshallers
Akka HTTP уже предопределяет ряд маршаллеров для наиболее распространенных типов.

* PredefinedToEntityMarshallers
   * Array[Byte]
   * ByteString
   * Array[Char]
   * String
   * akka.http.scaladsl.model.FormData
   * akka.http.scaladsl.model.MessageEntity
   * T <: akka.http.scaladsl.model.Multipart
* PredefinedToResponseMarshallers
   * T, if a ToEntityMarshaller[T] is available
   * HttpResponse
   * StatusCode
   * (StatusCode, T), if a ToEntityMarshaller[T] is available
   * (Int, T), if a ToEntityMarshaller[T] is available
   * (StatusCode, immutable.Seq[HttpHeader], T), if a ToEntityMarshaller[T] is available
   * (Int, immutable.Seq[HttpHeader], T), if a ToEntityMarshaller[T] is available
* PredefinedToRequestMarshallers
   * HttpRequest
   * Uri
   * (HttpMethod, Uri, T), if a ToEntityMarshaller[T] is available
(HttpMethod, Uri, immutable.Seq[HttpHeader], T), if a ToEntityMarshaller[T] is available
* GenericMarshallers
   * Marshaller[Throwable, T]
   * Marshaller[Option[A], B], if a Marshaller[A, B] and an EmptyValue[B] is available
   * Marshaller[Either[A1, A2], B], if a Marshaller[A1, B] and a Marshaller[A2, B] is available
   * Marshaller[Future[A], B], if a Marshaller[A, B] is available
   * Marshaller[Try[A], B], if a Marshaller[A, B] is available


### Неявное Разрешение
Инфраструктура маршаллинга Akka HTTP основана на подходе на основе классов типов, что означает, что экземпляры 
Маршаллера от определенного типа A до определенного типа B должны быть доступны неявно.

Импликаты для большинства предопределенных маршаллеров в Akka HTTP предоставляются через сопутствующий объект признака 
Маршаллера. Это означает, что они всегда доступны и никогда не нужно явно импортировать. Кроме того, их можно просто 
“переопределить”, добавив собственную пользовательскую версию в локальную область.

### Пользовательские Marshallers
Akka HTTP предоставляет вам несколько удобных инструментов для создания маршаллеров для ваших собственных типов. 
Прежде чем вы это сделаете, вам нужно подумать о том, какой маршаллер вы хотите создать. Если все, что ваш маршаллер
 должен производить, это `MessageEntity`, то вы, вероятно, должны предоставить `ToEntityMarshaller[T]`. Преимущество здесь 
 заключается в том, что он будет работать на клиента, а также на стороне сервера, поскольку `ToReponseMarshaller[т]`, а 
 также `ToRequestMarshaller[Т]` может быть автоматически создан, если `ToEntityMarshaller[Т]` есть в наличии.
 
Если, однако, ваш маршаллер также должен установить такие вещи, как код состояния ответа, метод запроса, URI запроса или 
любые заголовки, то `ToEntityMarshaller[T]` не будет работать. Вам потребуется падают на предоставление 
`ToResponseMarshaller[т]` или `ToRequestMarshaller[Т]]` напрямую. 

Для написания собственных маршаллеров вам не придется "вручную" реализовывать признак Маршаллера напрямую.

Скорее, должна быть возможность использовать один из помощников конструкции удобства, определенных на спутнике Маршаллера:

```scala
object Marshaller
  extends GenericMarshallers
  with PredefinedToEntityMarshallers
  with PredefinedToResponseMarshallers
  with PredefinedToRequestMarshallers {

  /**
   * Создает [[Marshaller]] из данной функции.
   */
  def apply[A, B](f: ExecutionContext ⇒ A ⇒ Future[List[Marshalling[B]]]): Marshaller[A, B] =
    new Marshaller[A, B] {
      def apply(value: A)(implicit ec: ExecutionContext) =
        try f(ec)(value)
        catch { case NonFatal(e) ⇒ FastFuture.failed(e) }
    }

  /**
   * Помощник для создания [[Marshaller]] с помощью данной функции
   */
  def strict[A, B](f: A ⇒ Marshalling[B]): Marshaller[A, B] =
    Marshaller { _ ⇒ a ⇒ FastFuture.successful(f(a) :: Nil) }

  /**
    * Помощник для создания "супер-маршаллер" из числа "суб-marshallers".
    * Содержание переговоров определяет, что "суб-маршаллер" в конечном итоге, чтобы сделать работу.
    *
    * Обратите внимание, что все маршаллеры будут вызваны для получения объекта Маршаллинга
    * из них, а позже решить, какой из marshallings должны быть возвращены. Это по дизайну,
    * однако в билете как описано в билете https://github.com/akka/akka-http/issues/243 это может быть
    * изменено в более поздних версиях Akka HTTP.
   */
  def oneOf[A, B](marshallers: Marshaller[A, B]*): Marshaller[A, B] =
    Marshaller { implicit ec ⇒ a ⇒ FastFuture.sequence(marshallers.map(_(a))).fast.map(_.flatten.toList) }

  /**
    * Помощник для создания "супер-маршаллер" из ряда значений и функция производства "суб-marshallers"
    * из этих значений. Content-negotiation определяет, какой "суб-маршаллер" в конечном итоге выполняет эту работу.
    *
    * Обратите внимание, что все маршаллеры будут вызваны для получения объекта Маршаллинга
    * из них, а позже решить, какой из marshallings должны быть возвращены. Это по дизайну,
    * однако в билете как описано в билете https://github.com/akka/akka-http/issues/243 это может быть
    * изменено в более поздних версиях Akka HTTP.
   */
  def oneOf[T, A, B](values: T*)(f: T ⇒ Marshaller[A, B]): Marshaller[A, B] =
    oneOf(values map f: _*)

  /**
   * Помощник для создания синхронного [[Marshaller]] контента с фиксированной кодировкой из данной функции.
   */
  def withFixedContentType[A, B](contentType: ContentType)(marshal: A ⇒ B): Marshaller[A, B] =
    new Marshaller[A, B] {
      def apply(value: A)(implicit ec: ExecutionContext) =
        try FastFuture.successful {
          Marshalling.WithFixedContentType(contentType, () ⇒ marshal(value)) :: Nil
        } catch {
          case NonFatal(e) ⇒ FastFuture.failed(e)
        }

      override def compose[C](f: C ⇒ A): Marshaller[C, B] =
        Marshaller.withFixedContentType(contentType)(marshal compose f)
    }

  /**
   * Помощник для создания синхронного [[Marshaller]] контента с оборотной кодировкой из данной функции.
   */
  def withOpenCharset[A, B](mediaType: MediaType.WithOpenCharset)(marshal: (A, HttpCharset) ⇒ B): Marshaller[A, B] =
    new Marshaller[A, B] {
      def apply(value: A)(implicit ec: ExecutionContext) =
        try FastFuture.successful {
          Marshalling.WithOpenCharset(mediaType, charset ⇒ marshal(value, charset)) :: Nil
        } catch {
          case NonFatal(e) ⇒ FastFuture.failed(e)
        }

      override def compose[C](f: C ⇒ A): Marshaller[C, B] =
        Marshaller.withOpenCharset(mediaType)((c: C, hc: HttpCharset) ⇒ marshal(f(c), hc))
    }

  /**
   * Помощник для создания синхронного [[Маршаллер]] для необоротного контента из данной функции.
   */
  def opaque[A, B](marshal: A ⇒ B): Marshaller[A, B] =
    strict { value ⇒ Marshalling.Opaque(() ⇒ marshal(value)) }

  /**
   * Помощник для создания [[Marshaller]] в сочетании с предоставленной функцией ' marshal` 
   * и неявный Маршаллер, который способен произвести требуемый конечный тип.
   */
  def combined[A, B, C](marshal: A ⇒ B)(implicit m2: Marshaller[B, C]): Marshaller[A, C] =
    Marshaller[A, C] { ec ⇒ a ⇒ m2.compose(marshal).apply(a)(ec) }
}
```

### Выводя Marshallers
Иногда вы можете сэкономить немного работы за счет использования существующих marshallers на ваш заказ. Идея состоит в 
том, чтобы” обернуть “существующий маршаллер с некоторой логикой, чтобы” переориентировать " его на ваш тип.

В этом отношении упаковка маршаллера может означать одну или обе из следующих двух вещей:

* Преобразуйте входные данные до того, как они достигнут упакованного маршаллера
* Преобразовать выход завернутый маршаллер

Для последнего (преобразования вывода) можно использовать baseMarshaller.карта, которая работает точно так же, как для функций. 
Для первого (преобразование ввода) у вас есть четыре альтернативы:

* `baseMarshaller.compose`
* `baseMarshaller.composeWithEC`
* `baseMarshaller.wrap`
* `baseMarshaller.wrapWithEC`

`compose` так же, как и для функций. `wrap` - это компоновка, которая позволяет также изменить тип содержимого (`ContentType`),
 на который маршаллер маршалил. `...WithEC` позволит вам получить `ExecutionContext` внутри, если он вам нужен, без 
того, чтобы зависеть от других неявно на месте использования.

### Используя Marshallers
Во многих местах Akka HTTP маршаллеры используются неявно, например, когда вы определяете, как выполнить запрос, 
используя DSL маршрутизации.

Однако при желании можно также использовать инфраструктуру маршалинга напрямую, что может быть полезно, например, в 
тестах. Лучшей точкой входа для этого является объект Marshal, который можно использовать следующим образом:

```scala
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._

import system.dispatcher // ExecutionContext

val string = "Yeah"
val entityFuture = Marshal(string).to[MessageEntity]
val entity = Await.result(entityFuture, 1.second) // don't block in non-test code!
entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

val errorMsg = "Easy, pal!"
val responseFuture = Marshal(420 -> errorMsg).to[HttpResponse]
val response = Await.result(responseFuture, 1.second) // don't block in non-test code!
response.status shouldEqual StatusCodes.EnhanceYourCalm
response.entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

val request = HttpRequest(headers = List(headers.Accept(MediaTypes.`application/json`)))
val responseText = "Plaintext"
val respFuture = Marshal(responseText).toResponseFor(request) // with content negotiation!
a[Marshal.UnacceptableResponseContentTypeException] should be thrownBy {
  Await.result(respFuture, 1.second) // client requested JSON, we only have text/plain!
}
```


[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
