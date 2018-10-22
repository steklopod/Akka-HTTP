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

Для «специальных» символов в URI обычно используется процентная кодировка, как показано ниже. Процентное кодирование 
обсуждается более подробно в строке запроса в разделе URI.

```scala
// не расшифровывать дважды
Uri("%2520").path.head shouldEqual "%20"
Uri("/%2F%5C").path shouldEqual Path / """/\"""
```

Когда недопустимая строка URI передается в `Uri()` как ниже, `IllegalUriException` бросается.

```scala
//illegal scheme
the[IllegalUriException] thrownBy Uri("foö:/a") shouldBe {
  IllegalUriException(
    "Illegal URI reference: Invalid input 'ö', expected scheme-char, 'EOI', '#', ':', '?', slashSegments or pchar (line 1, column 3)",
    "foö:/a\n" +
      "  ^")
}

// illegal userinfo
the[IllegalUriException] thrownBy Uri("http://user:ö@host") shouldBe {
  IllegalUriException(
    "Illegal URI reference: Invalid input 'ö', expected userinfo-char, pct-encoded, '@' or port (line 1, column 13)",
    "http://user:ö@host\n" +
      "            ^")
}

// illegal percent-encoding
the[IllegalUriException] thrownBy Uri("http://use%2G@host") shouldBe {
  IllegalUriException(
    "Illegal URI reference: Invalid input 'G', expected HEXDIG (line 1, column 13)",
    "http://use%2G@host\n" +
      "            ^")
}

// illegal percent-encoding ends with %
the[IllegalUriException] thrownBy Uri("http://www.example.com/%CE%B8%") shouldBe {
  IllegalUriException(
    "Illegal URI reference: Unexpected end of input, expected HEXDIG (line 1, column 31)",
    "http://www.example.com/%CE%B8%\n" +
      "                              ^")
}

// illegal path
the[IllegalUriException] thrownBy Uri("http://www.example.com/name with spaces/") shouldBe {
  IllegalUriException(
    "Illegal URI reference: Invalid input ' ', expected '/', 'EOI', '#', '?' or pchar (line 1, column 28)",
    "http://www.example.com/name with spaces/\n" +
      "                           ^")
}

// illegal path with control character
the[IllegalUriException] thrownBy Uri("http:///with\newline") shouldBe {
  IllegalUriException(
    "Illegal URI reference: Invalid input '\\n', expected '/', 'EOI', '#', '?' or pchar (line 1, column 13)",
    "http:///with\n" +
      "            ^")
}
```

#### Директивы для извлечения компонентов URI
Для извлечения компонентов URI с помощью директив см. следующие ссылки:

* [extractUri](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/basic-directives/extractUri.html)
* [extractScheme](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/scheme-directives/extractScheme.html)
* [scheme](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/scheme-directives/scheme.html)
* [PathDirectives](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/path-directives/index.html)
* [ParameterDirectives](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/parameter-directives/index.html)

#### Получение URI необработанного запроса
Иногда может потребоваться получить “сырое” значение входящего URI без применения к нему экранирования или 
синтаксического анализа. Хотя этот случай использования редок, он появляется время от времени. Можно получить “сырой” 
запрос URI в стороне сервера Akka HTTP, включив `akka.http.server.raw-request-uri-header` в запросе заголовка флаг. 
Если этот параметр включен, к каждому запросу добавляется заголовок URI необработанного запроса (`Raw-Request-URI`). 
Этот заголовок будет содержать URI исходного необработанного запроса, который был использован. Для примера проверьте 
ссылочную конфигурацию.

#### Строка запроса в URI
Хотя любая часть URI может содержать специальные символы, чаще всего строка запроса в URI содержит специальные 
символы, которые обычно кодируются в процентах.

Метод `query()` класса `Uri` возвращает строку запроса URI, которая моделируется в экземпляре класса `Query`. При создании 
экземпляра класса Uri путем передачи строки URI строка запроса хранится в виде необработанной строки. Затем при вызове 
метода `query()` строка запроса анализируется из необработанной строки.

Приведенный ниже код иллюстрирует синтаксический анализ допустимых строк запроса. В частности, вы можете проверить, как 
используется процентная кодировка и как анализируются специальные символы, такие как **`+`** и `;`.

```scala
def strict(queryString: String): Query = Query(queryString, mode = Uri.ParsingMode.Strict)
```

```scala
//компонент запроса "a=b" разбирается на имя параметра: "a" и значение: "b
strict("a=b") shouldEqual ("a", "b") +: Query.Empty

strict("") shouldEqual ("", "") +: Query.Empty
strict("a") shouldEqual ("a", "") +: Query.Empty
strict("a=") shouldEqual ("a", "") +: Query.Empty
strict("a=+") shouldEqual ("a", " ") +: Query.Empty //'+' is parsed to ' '
strict("a=%2B") shouldEqual ("a", "+") +: Query.Empty
strict("=a") shouldEqual ("", "a") +: Query.Empty
strict("a&") shouldEqual ("a", "") +: ("", "") +: Query.Empty
strict("a=%62") shouldEqual ("a", "b") +: Query.Empty

strict("a%3Db=c") shouldEqual ("a=b", "c") +: Query.Empty
strict("a%26b=c") shouldEqual ("a&b", "c") +: Query.Empty
strict("a%2Bb=c") shouldEqual ("a+b", "c") +: Query.Empty
strict("a%3Bb=c") shouldEqual ("a;b", "c") +: Query.Empty

strict("a=b%3Dc") shouldEqual ("a", "b=c") +: Query.Empty
strict("a=b%26c") shouldEqual ("a", "b&c") +: Query.Empty
strict("a=b%2Bc") shouldEqual ("a", "b+c") +: Query.Empty
strict("a=b%3Bc") shouldEqual ("a", "b;c") +: Query.Empty

strict("a+b=c") shouldEqual ("a b", "c") +: Query.Empty //'+' is parsed to ' '
strict("a=b+c") shouldEqual ("a", "b c") +: Query.Empty //'+' is parsed to ' '
```

Заметить что:

```scala
Uri("http://localhost?a=b").query()
```

эквивалентно:

```scala
Query("a=b")
```

Как и в [разделе 3.4 RFC 3986](http://tools.ietf.org/html/rfc3986#section-3.4), некоторые специальные символы, такие 
как **`/`** и **`?`** допускаются внутри строки запроса без экранирования с помощью знаков (**`%`**).

>Символы косой черты ( ”/“) и вопросительного знака ("?”) может представлять данные в компоненте запроса.

`/` и `?` Обычно используются, когда у вас есть URI, у параметра запроса которого есть другой URI.

```scala
strict("a?b=c") shouldEqual ("a?b", "c") +: Query.Empty
strict("a/b=c") shouldEqual ("a/b", "c") +: Query.Empty

strict("a=b?c") shouldEqual ("a", "b?c") +: Query.Empty
strict("a=b/c") shouldEqual ("a", "b/c") +: Query.Empty
```

Однако некоторые другие специальные символы могут вызвать `IllegalUriException` без процентов кодирования следующим образом.

```scala
the[IllegalUriException] thrownBy strict("a^=b") shouldBe {
  IllegalUriException(
    "Illegal query: Invalid input '^', expected '+', '=', query-char, 'EOI', '&' or pct-encoded (line 1, column 2)",
    "a^=b\n" +
      " ^")
}
the[IllegalUriException] thrownBy strict("a;=b") shouldBe {
  IllegalUriException(
    "Illegal query: Invalid input ';', expected '+', '=', query-char, 'EOI', '&' or pct-encoded (line 1, column 2)",
    "a;=b\n" +
      " ^")
}
```

```scala
//двойной '=' недопустимая строка запроса
the[IllegalUriException] thrownBy strict("a=b=c") shouldBe {
  IllegalUriException(
    "Illegal query: Invalid input '=', expected '+', query-char, 'EOI', '&' or pct-encoded (line 1, column 4)",
    "a=b=c\n" +
      "   ^")
}
//following '%', it should be percent encoding (HEXDIG), but "%b=" is not a valid percent encoding
the[IllegalUriException] thrownBy strict("a%b=c") shouldBe {
  IllegalUriException(
    "Illegal query: Invalid input '=', expected HEXDIG (line 1, column 4)",
    "a%b=c\n" +
      "   ^")
}
```

#### Строгий и расслабленный режим (`Strict and Relaxed Mode`)
`Uri.query()` и `Query()` принимают параметр `mode`, который является либо `Uri.ParsingMode.Strict` или 
`Uri.ParsingMode.Relaxed`. Переключение режима дает различное поведение при разборе некоторых специальных символов в URI.

```scala
def relaxed(queryString: String): Query = Query(queryString, mode = Uri.ParsingMode.Relaxed)
```

Ниже двух случаях бросил `IllegalUriException`, когда вы указали строгий режим,

```scala
the[IllegalUriException] thrownBy strict("a^=b") shouldBe {
  IllegalUriException(
    "Illegal query: Invalid input '^', expected '+', '=', query-char, 'EOI', '&' or pct-encoded (line 1, column 2)",
    "a^=b\n" +
      " ^")
}
the[IllegalUriException] thrownBy strict("a;=b") shouldBe {
  IllegalUriException(
    "Illegal query: Invalid input ';', expected '+', '=', query-char, 'EOI', '&' or pct-encoded (line 1, column 2)",
    "a;=b\n" +
      " ^")
}
```
но расслабленный режим разбирает их как есть.

```scala
relaxed("a^=b") shouldEqual ("a^", "b") +: Query.Empty
relaxed("a;=b") shouldEqual ("a;", "b") +: Query.Empty
relaxed("a=b=c") shouldEqual ("a", "b=c") +: Query.Empty
```

Однако даже в режиме `Relaxed` все еще существуют недопустимые специальные символы, требующие процентного кодирования.

```scala
//following '%', it should be percent encoding (HEXDIG), but "%b=" is not a valid percent encoding
//still invalid even in relaxed mode
the[IllegalUriException] thrownBy relaxed("a%b=c") shouldBe {
  IllegalUriException(
    "Illegal query: Invalid input '=', expected HEXDIG (line 1, column 4)",
    "a%b=c\n" +
      "   ^")
}
```

[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
