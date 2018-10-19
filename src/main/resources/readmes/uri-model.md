# Модель URI

Akka HTTP предлагает свой собственный специализированный класс модели [URI](http://doc.akka.io/api/akka-http/10.1.5/akka/http/scaladsl/model/Uri.html),
 который настроен как для производительности, так и для идиоматического использования в других типах модели HTTP. Например, 
 целевой `URI HttpRequest` анализируется в этом типе, где применяются все символы экранирования и другие специфические 
 семантики URI.

#### Парсинг строки URI

Мы следуем [RFC 3986](http://tools.ietf.org/html/rfc3986#section-1.1.2) для реализации правил разбора URI. При 
попытке проанализировать `строку URI` Akka HTTP внутренне создает экземпляр класса Uri, который содержит моделируемые 
компоненты URI внутри.

>Например, следующее создает экземпляр простого допустимого URI:

```scala
Uri("http://localhost")
```

Ниже приведены еще несколько примеров допустимых строк URI и способы создания экземпляров класса модели Uri с помощью 
метода `Uri.from()` путем передачи параметров `scheme`, `host`, `path` и `query`:

```scala
Uri("ftp://ftp.is.co.za/rfc/rfc1808.txt") shouldEqual
  Uri.from(scheme = "ftp", host = "ftp.is.co.za", path = "/rfc/rfc1808.txt")

Uri("http://www.ietf.org/rfc/rfc2396.txt") shouldEqual
  Uri.from(scheme = "http", host = "www.ietf.org", path = "/rfc/rfc2396.txt")

Uri("ldap://[2001:db8::7]/c=GB?objectClass?one") shouldEqual
  Uri.from(scheme = "ldap", host = "[2001:db8::7]", path = "/c=GB", queryString = Some("objectClass?one"))

Uri("mailto:John.Doe@example.com") shouldEqual
  Uri.from(scheme = "mailto", path = "John.Doe@example.com")

Uri("news:comp.infosystems.www.servers.unix") shouldEqual
  Uri.from(scheme = "news", path = "comp.infosystems.www.servers.unix")

Uri("tel:+1-816-555-1212") shouldEqual
  Uri.from(scheme = "tel", path = "+1-816-555-1212")

Uri("s3:image.png") shouldEqual
  Uri.from(scheme = "s3", path = "image.png")

Uri("telnet://192.0.2.16:80/") shouldEqual
  Uri.from(scheme = "telnet", host = "192.0.2.16", port = 80, path = "/")

Uri("urn:oasis:names:specification:docbook:dtd:xml:4.1.2") shouldEqual
  Uri.from(scheme = "urn", path = "oasis:names:specification:docbook:dtd:xml:4.1.2")
```

Для точного определения части URI, как схемы, пути и запросов см. в [RFC 3986](http://tools.ietf.org/html/rfc3986#section-1.1.2). Вот небольшой обзор:

```text
  foo://example.com:8042/over/there?name=ferret#nose
  \_/   \______________/\_________/ \_________/ \__/
   |           |            |            |        |
scheme     authority       path        query   fragment
   |   _____________________|__
  / \ /                        \
  urn:example:animal:ferret:nose
```


[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
