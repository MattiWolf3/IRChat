# IRChat
___

## Описание

IRC-чат с одноранговой (P2P) сетью и автоматической организацией кластера, позволяющий пользователям общаться в режиме 
реального времени как в общем чате, так и в личных сообщениях.

## Технологии

- Scala
- Akka
- JavaFX
- Gradle

## Запуск проекта

1. Запустите приложение одной из команд:
    ```bash
    ./gradlew run --args="127.0.0.1 25251 25252 25251" // первичный узел
    ./gradlew run --args="127.0.0.1 25251 25252 25252" // резервный узел
    ./gradlew run --args="127.0.0.1 25251 25252 0"     // случайный узел
    ```
2. Введите имя пользователя
3. Чтобы открыть приватный чат, дважды кликните на нужное имя пользователя в списке участников