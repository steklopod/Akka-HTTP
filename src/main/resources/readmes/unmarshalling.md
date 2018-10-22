# АнМаршаллинг

Анмаршаллинг - процесс Анмаршаллинг - процесс преобразования какой-то нижнем уровне, часто “провод формате”, на более 
высокий уровень (объект) структуры.
В Akka HTTP “Unmarshalling "означает преобразование исходного объекта более низкого уровня, например `MessageEntity`
 (который формирует "тело сущности" HTTP-запроса или ответа) или полный `HttpRequest` или `HttpResponse`, в экземпляр типа `T`.

### Базовая конструкция
Unmarshalling экземпляров типа `A` в экземпляры типа `B` выполняется с помощью `Unmarshaller[A, B]`.

Akka HTTP также предопределяет ряд полезных псевдонимов для типов анмаршаллеров, с которыми вы, вероятно, будете работать:

```scala
type FromEntityUnmarshaller[T] = Unmarshaller[HttpEntity, T]
type FromMessageUnmarshaller[T] = Unmarshaller[HttpMessage, T]
type FromResponseUnmarshaller[T] = Unmarshaller[HttpResponse, T]
type FromRequestUnmarshaller[T] = Unmarshaller[HttpRequest, T]
type FromByteStringUnmarshaller[T] = Unmarshaller[ByteString, T]
type FromStringUnmarshaller[T] = Unmarshaller[String, T]
type FromStrictFormFieldUnmarshaller[T] = Unmarshaller[StrictForm.Field, T]
```

По своей сути `Unmarshaller[A, B]` очень похож на функцию `A => Future[B]` и как таковой немного проще, чем его аналог 
маршалинга. Процесс unmarshalling не должен поддерживать согласование содержания, которое сохраняет два дополнительных 
уровня косвенного обращения, которые требуются на стороне маршалинга.

### Предопределенные Unmarshallers
Akka HTTP уже предопределяет ряд unmarshallers для наиболее распространенных типов. Конкретно эти:

* PredefinedFromStringUnmarshallers
   * Byte
   * Short
   * Int
   * Long
   * Float
   * Double
   * Boolean
* PredefinedFromEntityUnmarshallers
   * Array[Byte]
   * ByteString
   * Array[Char]
   * String
   * akka.http.scaladsl.model.FormData
* GenericUnmarshallers
   * Unmarshaller[T, T]
   * Unmarshaller[Option[A], B], if an Unmarshaller[A, B] is available
   * Unmarshaller[A, Option[B]], if an Unmarshaller[A, B] is available

Дополнительные unmarshallers доступны в отдельных модулях для определенных типов контента, таких как JSON и XML.

### Неявное Разрешение
Инфраструктура unmarshalling Akka HTTP полагается на подход, основанный на классе типа, что означает, что экземпляры 
Unmarshaller от определенного типа `A` до определенного типа `B` должны быть доступны неявно.

Импликаты для большинства предопределенных unmarshallers в Akka HTTP предоставляются через сопутствующий объект признака 
Unmarshaller. Это означает, что они всегда доступны и никогда не нужно явно импортировать. Кроме того, их можно просто 
“переопределить”, добавив собственную пользовательскую версию в локальную область.

### Пользовательские Unmarshallers
Akka HTTP предоставляет вам несколько удобных инструментов для создания unmarshallers для ваших собственных типов. 
Обычно вам не придется” вручную " реализовывать черту Unmarshaller напрямую. Скорее, должно быть возможно использовать 
один из помощников конструкции удобства, определенных на спутнике Unmarshaller:


```scala
/**
 * Создает " Unmarshaller` из данной функции.
 */
def apply[A, B](f: ExecutionContext ⇒ A ⇒ Future[B]): Unmarshaller[A, B] =
  withMaterializer(ec => _ => f(ec))

def withMaterializer[A, B](f: ExecutionContext ⇒ Materializer => A ⇒ Future[B]): Unmarshaller[A, B] =
  new Unmarshaller[A, B] {
    def apply(a: A)(implicit ec: ExecutionContext, materializer: Materializer) =
      try f(ec)(materializer)(a)
      catch { case NonFatal(e) ⇒ FastFuture.failed(e) }
  }

/**
 * Помощник для создания синхронного "Unmarshaller" из данной функции.
 */
def strict[A, B](f: A ⇒ B): Unmarshaller[A, B] = Unmarshaller(_ => a ⇒ FastFuture.successful(f(a)))

/**
  * Помощник для создания "супер-unmarshaller" из последовательности "суб-unmarshallers", которые опробованы
  * в заданном порядке. Первый успешный unmarshalling "суб-unmarshallers" является тот, произведенный
  * "super-unmarshaller".
 */
def firstOf[A, B](unmarshallers: Unmarshaller[A, B]*): Unmarshaller[A, B] = //...
```

### Выведение Unmarshallers
Иногда вы можете сэкономить себе немного работы, повторно используя существующие unmarshallers для пользовательских. 
Идея состоит в том, чтобы” обернуть “существующий unmarshaller с некоторой логикой, чтобы” переориентировать " его на ваш тип.

Обычно то, что вы хотите сделать, это преобразовать вывод некоторого существующего unmarshaller и преобразовать его в 
свой тип. Для этого типа unmarshaller преобразования Akka HTTP определяет следующие методы:

* baseUnmarshaller.transform
* baseUnmarshaller.map
* baseUnmarshaller.mapWithInput
* baseUnmarshaller.flatMap
* baseUnmarshaller.flatMapWithInput
* baseUnmarshaller.recover
* baseUnmarshaller.withDefaultValue
* baseUnmarshaller.mapWithCharset (доступно только для `FromEntityUnmarshallers`)
* baseUnmarshaller.forContentTypes (доступно только для `FromEntityUnmarshallers`)

Сигнатуры методов должны сделать их семантику относительно ясной.

Во многих местах в Akka HTTP unmarshallers используются неявно, например, когда вы хотите получить доступ к сущности 
запроса с помощью DSL маршрутизации.

Однако при желании можно также напрямую использовать инфраструктуру демаршалинга, что может быть полезно, например, в 
тестах. Лучшая точка входа для это `akka.http.scaladsl.unmarshalling.Unmarshal`, который можно использовать 
следующим образом:

```scala
import akka.http.scaladsl.unmarshalling.Unmarshal
import system.dispatcher // Необязательный ExecutionContext (по умолчанию из Materializer)
implicit val materializer: Materializer = ActorMaterializer()

import scala.concurrent.Await
import scala.concurrent.duration._

val intFuture = Unmarshal("42").to[Int]
val int = Await.result(intFuture, 1.second) // не блокируйте в нетестовом коде!
int shouldEqual 42

val boolFuture = Unmarshal("off").to[Boolean]
val bool = Await.result(boolFuture, 1.second) // не блокируйте в нетестовом коде!
bool shouldBe false
```

[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
