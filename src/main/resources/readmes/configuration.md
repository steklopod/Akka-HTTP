# Конфигурация

Как и любой другой модуль Akka, Akka HTTP настраивается через Typesafe Config. Обычно это означает, что вы предоставляете 
**`application.conf`**, который содержит все специфичные для приложения Настройки, отличающиеся от настроек по умолчанию, 
предоставляемых эталонными конфигурационными файлами отдельных модулей Akka.

Это соответствующие значения конфигурации по умолчанию для HTTP-модулей Akka.

[переведено отсюда](https://doc.akka.io/docs/akka-http/current/configuration.html)

```yaml
##################
# akka-http-core # Это ссылочный конфигурационный файл, содержащий все настройки по умолчанию.
##################


#  Akka HTTP-версия, проверенная на исполнение версии Akka HTTP. Загружается из созданного файла conf.
include "akka-http-version"

akka.http {

  server {
    # Значение по умолчанию заголовка `Server` для создания, если явный `Server`-header не был включен в ответ.
    # Если это значение является пустой строкой, и заголовок не был включен в
    # запрос, никакой заголовок `Server` не будет отображаться вообще.
    server-header = akka-http/${akka.http.version}


    # «PREVIEW», которые еще не полностью готовы к производству.
    # Эти флаги могут меняться или удаляться между релизами патчей.
    preview {
      # Работает только с `bindAndHandleAsync` (в настоящее время)
      # Если этот параметр включен и найдена поддержка akka-http2
      # на пути к классу обычный Http ().связывать... вызовы методов будут связывать
      # использование HTTP / 2. Обратите внимание, что при этом необходимо настроить HTTPS.
      enable-http2 = off
    }

    # Время, по истечении которого простое соединение будет автоматически закрыто.
    # Установить "бесконечный", чтобы полностью отключить время ожидания подключения.
    idle-timeout = 60 s

    # Определяет период времени по умолчанию, в течение которого заявитель должен
    # производить HttpResponse для любого заданного HttpRequest он получил.
    # Тайм-аут начинает работать когда *конец* требования
    # получено, поэтому даже потенциально длинные загрузки могут иметь короткий тайм-аут.
    # Установите в ' бесконечный`, чтобы полностью отключить проверку тайм-аута запроса.
    #
    # Убедитесь, что это время ожидания меньше, чем время простоя, в противном случае,
    # тайм-аут простоя включится первым и сбросит TCP соединение
    # без ответа.
    #
    # Если этот параметр не "бесконечный" слой HTTP-сервер добавляет
    # ` Timeout-Access ' заголовок запроса, который включает программный
    # настройка периода тайм-аута и ответа тайм-аута для каждого
    # запрос индивидуально.
    request-timeout = 20 s

    # Период времени, в течение которого должен быть завершен процесс привязки TCP.
    bind-timeout = 1s

    # Порт по умолчанию для привязки HTTP-сервера, когда порт явно не указан.
    default-http-port = 80

    # Порт по умолчанию для привязки HTTPS-сервера, когда порт явно не указан.
    default-https-port = 443

    # Период времени реализации HTTP-сервера будет держать соединение открытым после
    # все данные были доставлены на сетевой уровень. Этот параметр аналогичен параметру сокета SO_LINGER
    # но не только включает сокет уровня ОС, но и покрывает сетевой стек потоков Akka IO / Akka.
    # Параметр является дополнительной мерой предосторожности, которая предотвращает клиентов от держать открытым соединение, которое
    # уже считается завершенным со стороны сервера.
    #
    # Если буферы сетевого уровня (включая буферы сетевого стека Akka Stream / Akka IO)
    # содержит больше данных, чем может быть передано клиенту в данный момент времени, когда серверная сторона считает
    # чтобы завершить работу с этим подключением, клиент может столкнуться с сбросом подключения.
    #
    # Установите значение 'infinite', чтобы отключить автоматическое закрытие соединений (что может привести к утечке соединений).
    linger-timeout = 1 min

    # Максимальное количество одновременно принимаемых соединений при использовании
    # `Http().bindAndHandle` метод
    #
    # Этот параметр не применяется к ' Http ().привязать метод, который до сих пор
    # выполнять неограниченное backpressured поток входящих подключений.
    #
    # Обратите внимание, что этот параметр ограничивает количество подключений по мере необходимости.
    # Он *не* строго гарантировать, что количество TCP-подключений не будет
    # превышение предела (но это будет приблизительно правильно), потому что происходит прекращение соединения
    # асинхронно. Это также *не * гарантирует, что число одновременно активного обработчика
    # материализации подачи никогда не будут превышать предел по той причине, что невозможно надежно
    # определить, когда материализация закончилась.
    max-connections = 1024

    # Максимальное количество запросов, которые принимаются (и отправляются в
    # заявление) на одно подключение до первого запроса
    # должен быть завершен.
    # Входящие запросы, которые могут привести к превышению лимита конвейерной обработки
    # не прочитано от гнезда соединений для того чтобы построить вверх по " обратному давлению"
    # клиенту через TCP flow control.
    # Установка 1 отключает конвейерную передачу HTTP, так как только один запрос
    # соединение может быть "открытым" (т. е. обрабатываться приложением) в любом
    # время. Установите более высокие значения, чтобы включить конвейерную передачу HTTP.
    # Это значение должно быть > 0 и <= 1024.
    pipelining-limit = 16

    # Включает / выключает добавление заголовка `Remote-Address` 
    # удержание IP-адреса клиентов (удаленных).
    remote-address-header = off

    # Включает / отключает добавление заголовка ' Raw-Request-URI`, содержащего
    # исходный необработанный URI запроса, отправленный клиентом.
    raw-request-uri-header = off

    # Включает / отключает автоматическую обработку запросов HEAD.
    # Если этот параметр включен, сервер отправляет запросы HEAD как GET
    # запросы к приложению и автоматически удаляет все сообщения
    # тела из исходящих ответов.
    # Обратите внимание, что даже если этот параметр выключен сервер никогда не пошлет
    # out тела сообщений на ответы на запросы головы.
    transparent-head-requests = on

    # Включает / отключает возврат более подробных сообщений об ошибках в
    # клиент в ответе на ошибку.
    # Должен быть отключен для интерфейсов API браузера из-за риска XSS-атак
    # и (возможно) включен для внутренних или не-браузерных API.
    # Обратите внимание, что akka-http всегда будет производить сообщения журнала, содержащие полный
    # сведения об ошибке.
    verbose-error-messages = off

    # Начальный размер буфера для отображения заголовков ответа.
    # Может использоваться для тонкой настройки производительности рендеринга ответа, но, вероятно
    # не нужно возиться с в большинстве приложений.
    response-header-size-hint = 512

    # Запрошенная максимальная длина очереди входящих соединений.
    # Если сервер занят и отставание в ОС будут дропаться
    # SYN-пакеты и попытки подключения могут завершиться ошибкой. Заметим, что отставание
    # размер обычно только максимум намек на размер, ОС и ОС
    # ограничить число далее на основе глобальных ограничений.
    backlog = 100

    # Если этот параметр пуст, сервер принимает только
    # непустой заголовок` Host'. В противном случае он отвечает "400 неверный запрос".
    # Установите непустое значение, которое будет использоваться вместо отсутствующего или пустого `Хоста`
    # заголовок, чтобы сервер принимал такие запросы . 
    # Обратите внимание, что сервер не поддерживает HTTP/1.1 запрос без "хозяина"
    # header, т. е. этот параметр влияет только на HTTP / 1.1 запросы с пустым
    # Заголовок ' Host`, а также HTTP/1.0 запросов.
    # Примеры: `www.spray.io или примеру.ком:8080`
    default-host-header = ""

    # Параметры сокета, чтобы установить для прослушивания сокета. Если настройка осталась
    # неопределенным, он будет использовать все, что по умолчанию в системе.
    socket-options {
      so-receive-buffer-size = undefined
      so-send-buffer-size = undefined
      so-reuse-address = undefined
      so-traffic-class = undefined
      tcp-keep-alive = undefined
      tcp-oob-inline = undefined
      tcp-no-delay = undefined
    }

    # Когда изящное завершение включено и используется вызывается с крайним сроком,
    # после того, как крайний срок проходит ожидающие запросы будут отвечены с" завершающим " http-ответом,
    # вместо доставки этих запросов обработчику пользователя.
    # Этот ответ настраивается здесь, используя конфигурацию, или через код, если требуется более сложный 
    #    (например, `response entity`) ответ.
    termination-deadline-exceeded-response {
      # Код состояния ответа" завершение", который будет автоматически отправлен на ожидающие запросы при превышении крайнего срока завершения.
      status = 503 # ServiceUnavailable
    }

    # Изменить, чтобы настроить параметры разбора только на стороне сервера.
    parsing {
      # по умолчанию переопределений нет, см. ' akka.http.анализа` для значений по умолчанию
    }

    # Включает / отключает протоколирование незашифрованного HTTP-трафика в HTTP и из HTTP
    # сервер для отладки.
    #
    # Примечание: используйте с осторожностью. Ведение журнала незашифрованного трафика данных может предоставлять секретные данные.
    #
    # Входящий и исходящий трафик будет регистрироваться в формате шестнадцатеричного дампа. Включение ведения журнала,
    # укажите количество байт для записи в журнал для каждого блока данных (фактическое разбиение зависит
    # о деталях реализации и сетевых условиях и должны рассматриваться как
    # произвольный.)
    #
    # Для логгирования на стороне клиента см. akka.http.client.log-unencrypted-network-bytes.
    #
    # "off`: сообщения журнала не создаются
    # Int  : определяет, сколько байт должно быть записано в журнал для каждого блока данных
    log-unencrypted-network-bytes = off

    http2 {
      # Максимальное количество запросов на соединение, одновременно отправленных обработчику запросов.
      max-concurrent-streams = 256

        # Максимальное количество байт, получаемых от сущности запроса в одном чанке.
        #
        # Причина ограничить этот объем (вместо доставки всех буферизованных данных для потока) заключается в том, что
        # количество данных во внутренних буферах будет управлять обратным давлением и управлением потоками на уровне HTTP / 2. Больший
        # chunks означало бы, что читатель сущностей пользовательского уровня должен будет буферизировать все эти данные, если он не может прочитать их в одном
        # идти. Реализация не сможет противодавления дополнительных данных в этом случае, потому что он не знает о
        # этот буфер пользовательского уровня .
      request-entity-chunk-size = 65536 b

      # The number of request data bytes the HTTP/2 implementation is allowed to buffer internally per connection. Free
      # space in this buffer is communicated to the peer using HTTP/2 flow-control messages to backpressure data if it
      # isn't read fast enough.
      #
      # When there is no backpressure, this amount will limit the amount of in-flight data. It might need to be increased
      # for high bandwidth-delay-product connections.
      #
      # There is a relation between the `incoming-connection-level-buffer-size` and the `incoming-stream-level-buffer-size`:
      # If incoming-connection-level-buffer-size < incoming-stream-level-buffer-size * number_of_streams, then
      # head-of-line blocking is possible between different streams on the same connection.
      incoming-connection-level-buffer-size = 10 MB

      # The number of request data bytes the HTTP/2 implementation is allowed to buffer internally per stream. Free space
      # in this buffer is communicated to the peer using HTTP/2 flow-control messages to backpressure data if it isn't
      # read fast enough.
      #
      # When there is no backpressure, this amount will limit the amount of in-flight data per stream. It might need to
      # be increased for high bandwidth-delay-product connections.
      incoming-stream-level-buffer-size = 512kB
    }

    websocket {
      # periodic keep alive may be implemented using by sending Ping frames
      # upon which the other side is expected to reply with a Pong frame,
      # or by sending a Pong frame, which serves as unidirectional heartbeat.
      # Valid values:
      #   ping - default, for bi-directional ping/pong keep-alive heartbeating
      #   pong - for uni-directional pong keep-alive heartbeating
      #
      # It is also possible to provide a payload for each heartbeat message,
      # this setting can be configured programatically by modifying the websocket settings.
      # See: https://doc.akka.io/docs/akka-http/current/server-side/websocket-support.html
      periodic-keep-alive-mode = ping

      # Interval for sending periodic keep-alives
      # The frame sent will be the one configured in akka.http.server.websocket.periodic-keep-alive-mode
      # `infinite` by default, or a duration that is the max idle interval after which an keep-alive frame should be sent
      # The value `infinite` means that *no* keep-alive heartbeat will be sent, as: "the allowed idle time is infinite"
      periodic-keep-alive-max-idle = infinite
    }
  }

  client {
    # The default value of the `User-Agent` header to produce if no
    # explicit `User-Agent`-header was included in a request.
    # If this value is the empty string and no header was included in
    # the request, no `User-Agent` header will be rendered at all.
    user-agent-header = akka-http/${akka.http.version}

    # The time period within which the TCP connecting process must be completed.
    connecting-timeout = 10s

    # The time after which an idle connection will be automatically closed.
    # Set to `infinite` to completely disable idle timeouts.
    idle-timeout = 60 s

    # The initial size of the buffer to render the request headers in.
    # Can be used for fine-tuning request rendering performance but probably
    # doesn't have to be fiddled with in most applications.
    request-header-size-hint = 512

    # Socket options to set for the listening socket. If a setting is left
    # undefined, it will use whatever the default on the system is.
    socket-options {
      so-receive-buffer-size = undefined
      so-send-buffer-size = undefined
      so-reuse-address = undefined
      so-traffic-class = undefined
      tcp-keep-alive = undefined
      tcp-oob-inline = undefined
      tcp-no-delay = undefined
    }

    # Client https proxy options. When using ClientTransport.httpsProxy() with or without credentials,
    # host/port must be either passed explicitly or set here. If a host is not set, the proxy will not be used.
    proxy {
      https {
        host = ""
        port = 443
      }
    }

    # Modify to tweak parsing settings on the client-side only.
    parsing {
      # no overrides by default, see `akka.http.parsing` for default values
    }

    # Enables/disables the logging of unencrypted HTTP traffic to and from the HTTP
    # client for debugging reasons.
    #
    # Note: Use with care. Logging of unencrypted data traffic may expose secret data.
    #
    # Incoming and outgoing traffic will be logged in hexdump format. To enable logging,
    # specify the number of bytes to log per chunk of data (the actual chunking depends
    # on implementation details and networking conditions and should be treated as
    # arbitrary).
    #
    # For logging on the server side, see akka.http.server.log-unencrypted-network-bytes.
    #
    # `off` : no log messages are produced
    # Int   : determines how many bytes should be logged per data chunk
    log-unencrypted-network-bytes = off

    websocket {
      # periodic keep alive may be implemented using by sending Ping frames
      # upon which the other side is expected to reply with a Pong frame,
      # or by sending a Pong frame, which serves as unidirectional heartbeat.
      # Valid values:
      #   ping - default, for bi-directional ping/pong keep-alive heartbeating
      #   pong - for uni-directional pong keep-alive heartbeating
      #
      # See https://tools.ietf.org/html/rfc6455#section-5.5.2
      # and https://tools.ietf.org/html/rfc6455#section-5.5.3 for more information
      periodic-keep-alive-mode = ping

      # Interval for sending periodic keep-alives
      # The frame sent will be the onne configured in akka.http.server.websocket.periodic-keep-alive-mode
      # `infinite` by default, or a duration that is the max idle interval after which an keep-alive frame should be sent
      periodic-keep-alive-max-idle = infinite
    }
  }

  host-connection-pool {
    # The maximum number of parallel connections that a connection pool to a
    # single host endpoint is allowed to establish. Must be greater than zero.
    max-connections = 4

    # The minimum number of parallel connections that a pool should keep alive ("hot").
    # If the number of connections is falling below the given threshold, new ones are being spawned.
    # You can use this setting to build a hot pool of "always on" connections.
    # Default is 0, meaning there might be no active connection at given moment.
    # Keep in mind that `min-connections` should be smaller than `max-connections` or equal
    min-connections = 0

    # The maximum number of times failed requests are attempted again,
    # (if the request can be safely retried) before giving up and returning an error.
    # Set to zero to completely disable request retries.
    max-retries = 5

    # The maximum number of open requests accepted into the pool across all
    # materializations of any of its client flows.
    # Protects against (accidentally) overloading a single pool with too many client flow materializations.
    # Note that with N concurrent materializations the max number of open request in the pool
    # will never exceed N * max-connections * pipelining-limit.
    # Must be a power of 2 and > 0!
    max-open-requests = 32

    # The maximum number of requests that are dispatched to the target host in
    # batch-mode across a single connection (HTTP pipelining).
    # A setting of 1 disables HTTP pipelining, since only one request per
    # connection can be "in flight" at any time.
    # Set to higher values to enable HTTP pipelining.
    # This value must be > 0.
    # (Note that, independently of this setting, pipelining will never be done
    # on a connection that still has a non-idempotent request in flight.
    #
    # Before increasing this value, make sure you understand the effects of head-of-line blocking.
    # Using a connection pool, a request may be issued on a connection where a previous
    # long-running request hasn't finished yet. The response to the pipelined requests may then be stuck
    # behind the response of the long-running previous requests on the server. This may introduce an
    # unwanted "coupling" of run time between otherwise unrelated requests.
    #
    # See http://tools.ietf.org/html/rfc7230#section-6.3.2 for more info.)
    pipelining-limit = 1

    # The time after which an idle connection pool (without pending requests)
    # will automatically terminate itself. Set to `infinite` to completely disable idle timeouts.
    idle-timeout = 30 s

    # The pool implementation to use. Currently supported are:
    #  - legacy: the original 10.0.x pool implementation
    #  - new: the pool implementation that became the default in 10.1.x and will receive fixes and new features
    pool-implementation = new

    # The "new" pool implementation will fail a connection early and clear the slot if a response entity was not
    # subscribed during the given time period after the response was dispatched. In busy systems the timeout might be
    # too tight if a response is not picked up quick enough after it was dispatched by the pool.
    response-entity-subscription-timeout = 1.second

    # Modify this section to tweak client settings only for host connection pools APIs like `Http().superPool` or
    # `Http().singleRequest`.
    client = {
      # no overrides by default, see `akka.http.client` for default values
    }
  }

  # Modify to tweak default parsing settings.
  #
  # IMPORTANT:
  # Please note that this sections settings can be overridden by the corresponding settings in:
  # `akka.http.server.parsing`, `akka.http.client.parsing` or `akka.http.host-connection-pool.client.parsing`.
  parsing {
    # The limits for the various parts of the HTTP message parser.
    max-uri-length             = 2k
    max-method-length          = 16
    max-response-reason-length = 64
    max-header-name-length     = 64
    max-header-value-length    = 8k
    max-header-count           = 64
    max-chunk-ext-length       = 256
    max-chunk-size             = 1m

    # Default maximum content length which should not be exceeded by incoming request entities.
    # Can be changed at runtime (to a higher or lower value) via the `HttpEntity::withSizeLimit` method.
    # Note that it is not necessarily a problem to set this to a high value as all stream operations
    # are always properly backpressured.
    # Nevertheless you might want to apply some limit in order to prevent a single client from consuming
    # an excessive amount of server resources.
    #
    # Set to `infinite` to completely disable entity length checks. (Even then you can still apply one
    # programmatically via `withSizeLimit`.)
    max-content-length = 8m

    # The maximum number of bytes to allow when reading the entire entity into memory with `toStrict`
    # (which is used by the `toStrictEntity` and `extractStrictEntity` directives)
    max-to-strict-bytes = 8m

    # Sets the strictness mode for parsing request target URIs.
    # The following values are defined:
    #
    # `strict`: RFC3986-compliant URIs are required,
    #     a 400 response is triggered on violations
    #
    # `relaxed`: all visible 7-Bit ASCII chars are allowed
    #
    uri-parsing-mode = strict

    # Sets the parsing mode for parsing cookies.
    # The following value are defined:
    #
    # `rfc6265`: Only RFC6265-compliant cookies are parsed. Surrounding double-quotes are accepted and
    #   automatically removed. Non-compliant cookies are silently discarded.
    # `raw`: Raw parsing allows any non-control character but ';' to appear in a cookie value. There's no further
    #   post-processing applied, so that the resulting value string may contain any number of whitespace, unicode,
    #   double quotes, or '=' characters at any position.
    #   The rules for parsing the cookie name are the same ones from RFC 6265.
    #
    cookie-parsing-mode = rfc6265

    # Enables/disables the logging of warning messages in case an incoming
    # message (request or response) contains an HTTP header which cannot be
    # parsed into its high-level model class due to incompatible syntax.
    # Note that, independently of this settings, akka-http will accept messages
    # with such headers as long as the message as a whole would still be legal
    # under the HTTP specification even without this header.
    # If a header cannot be parsed into a high-level model instance it will be
    # provided as a `RawHeader`.
    # If logging is enabled it is performed with the configured
    # `error-logging-verbosity`.
    illegal-header-warnings = on

    # Sets the list of headers for which illegal values will *not* cause warning logs to be emitted;
    #
    # Adding a header name to this setting list disables the logging of warning messages in case an incoming　message
    # contains an HTTP header which cannot be　parsed into its high-level model class due to incompatible syntax.
    ignore-illegal-header-for = []

    # Parse headers into typed model classes in the Akka Http core layer.
    #
    # If set to `off`, only essential headers will be parsed into their model classes. All other ones will be provided
    # as instances of `RawHeader`. Currently, `Connection`, `Host`, and `Expect` headers will still be provided in their
    # typed model. The full list of headers still provided as modeled instances can be found in the source code of
    # `akka.http.impl.engine.parsing.HttpHeaderParser.alwaysParsedHeaders`. Note that (regardless of this setting)
    # some headers like `Content-Type` are treated specially and will never be provided in the list of headers.
    modeled-header-parsing = on

    # Configures the verbosity with which message (request or response) parsing
    # errors are written to the application log.
    #
    # Supported settings:
    # `off`   : no log messages are produced
    # `simple`: a condensed single-line message is logged
    # `full`  : the full error details (potentially spanning several lines) are logged
    error-logging-verbosity = full

    # Configures the processing mode when encountering illegal characters in
    # header value of response.
    #
    # Supported mode:
    # `error`  : default mode, throw an ParsingException and terminate the processing
    # `warn`   : ignore the illegal characters in response header value and log a warning message
    # `ignore` : just ignore the illegal characters in response header value
    illegal-response-header-value-processing-mode = error

    # limits for the number of different values per header type that the
    # header cache will hold
    header-cache {
      default = 12
      Content-MD5 = 0
      Date = 0
      If-Match = 0
      If-Modified-Since = 0
      If-None-Match = 0
      If-Range = 0
      If-Unmodified-Since = 0
      User-Agent = 32
    }

    # Enables/disables inclusion of an Tls-Session-Info header in parsed
    # messages over Tls transports (i.e., HttpRequest on server side and
    # HttpResponse on client side).
    tls-session-info-header = off
  }
}
```


[<= содержание](https://github.com/steklopod/Akka-HTTP/blob/master/readme.md)

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
