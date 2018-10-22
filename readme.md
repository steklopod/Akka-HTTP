## ``Akka HTTP`` - руководство

**Акка** - популярный инструментарий для создания параллельных и распределенных приложений в JVM. Он имеет несколько 
модулей, которые помогают создавать такие приложения, и одним из них является `Akka HTTP`.

`Akka HTTP` предлагает общий набор инструментов для предоставления и использования HTTP-сервисов. HTTP-модули Akka 
реализуют полный HTTP-стек на стороне сервера и клиента поверх [akka-actor](https://github.com/steklopod/akka) и 
[akka-stream](https://github.com/steklopod/Akka-Streams). 

### Содержание:
1. [Введение](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/introduction.md)
2. [Конфигурация](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/configuration.md)
3. [Типы данных и абстракции](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/data-types-and-abstractions.md)
   * [Модель URI](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/uri-model.md)
   * [Маршаллинг](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/marshalling.md)
   * [АнМаршаллинг](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/unmarshalling.md)
   * [Кодирование / Декодирование](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/encoding_decoding.md)
   * [Поддержка JSON](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/json-support.md)
4. [Серверное API](https://github.com/steklopod/Akka-HTTP/blob/master/src/main/resources/readmes/server_api.md)


`Akka HTTP` следует довольно открытому дизайну и часто предлагает несколько API для решения одной и той же задачи. 
Вы можете выбрать API с уровнем абстракции, который лучше всего подходит для вашего приложения. Если у вас есть проблемы с 
достижением чего-либо с использованием API высокого уровня, вы можете найти API более низкого уровня для использования. 
Низкоуровневые API-интерфейсы предлагают большую гибкость, но могут потребовать от вас написать больше кода приложения.

Этот проект демонстрирует библиотеку Akka HTTP и Scala для написания простой службы REST (micro). В проекте показаны 
следующие задачи, характерные для большинства проектов на основе Akka HTTP:

* запуск автономного HTTP-сервера;
* обработка файловой конфигурации;
* логгирование;
* маршрутизации;
* деконструирование запросов;
* анмаршаллинг JSON-сущности в кэйс-классы;
* маршалинг кэйс-классов в JSON-ответы;
* обработка ошибок;
* создание запросов внешним сервисам;
* тестирование внешних mock-сервисов.

_В проекте вы найдете не только теоретические материалы но и рабочие примеры кода._

_Если этот проект окажется полезным тебе - нажми на кнопочку **`★`** в правом верхнем углу._
