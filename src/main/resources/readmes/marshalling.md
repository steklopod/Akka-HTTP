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
[подробнее](https://doc.akka.io/docs/akka-http/current/common/marshalling.html#deriving-marshallers)



[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
