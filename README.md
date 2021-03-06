# Курсовой проект «Сервис перевода денег»

## Введение
Данный REST-сервис является backend - составляющей сервиса перевода денег "MoneyTransferService" и предназначен для осуществления перевода денежных средств между двумя банковскими картами.
Frontend - составляющая сервиса доступна по ссылке: https://github.com/serp-ya/card-transfer 

## Инструкция для использования

Запуск "MoneyTransferService" можно осуществить двумя способами:
1. Запуск вручную с помощью терминала
2. Запуск с помощью docker container

### Запуск вручную с помощью терминала
С запуском Frontend - приложения можно ознакомиться по указанной выше ссылке.
Для запуска backend - приложения необходимо в корневом каталоге запустить скомпилированный jar - архив под названием: **money_transfer_service-0.0.1-SNAPSHOT.jar**

### Запуск с помощью docker container
Файл docker-compose.yml содержит в себе описание двух контейнеров для "MoneyTransferService".
Для запуска контейнеров необходимо ввести команду docker-compose up. Frontend - сервис будет доступен в браузере по адресу: http://localhost:3000, backend - сервис будет запущен и готов к работе. Команда docker-compose down остановит запущенные контейнеры

## Описание работы backend - сервиса
После успешного ввода данных на стороне frontend, данные передаются на backend по протоколу HTTP через POST - запрос на адрес http://localhost:8080/transfer.
Сущность - контроллер "MoneyTransferController" принимает запрос по маппингу: /transfer и преобразует входные данные в объект "TransactionData". Преобразование данных запроса происходит с помощью класса - конвертера "TransactionDataConverter", который конвертирует тело POST - запроса (формата json) и производит проверку валидности принятых данных.

Далее контроллер делегирует работу основному компоненту сервисного слоя приложения - "MoneyTransferService".
Сущность - сервис "MoneyTransferService" делегирует дальнейшую обработку транзакции сервису - "TransactionHandlerService".
"TransactionHandlerService" с помощью дефолтной реализации "TransactionHandler" ("DefaultTransactionHandler") осуществляет обработку данных карты отправителя (проверяет валидность номера карты, валидность CVV - кода и срока годности карты), обработку данных карты получателя (валидность номера карты) путем обращения к Repository - слою, а именно к сущности "CardsRepository", а также генерирование id транзакции с помощью "TransactionsRepository" в случае, если данные верны. После обработки данных, "TransactionHandlerService" генерирует секретный код, состоящий из 4 цифр и заносит его значение в поле объекта "Transcation", куда так же заносятся данные о картах, номер телефона отправителя (для отправки сообщения) и данные о взимаемой комиссии. В случае удачной обработки данных объект класса "Transcation" передается "MoneyTransferService" в качестве ответа. Объект класса "Transcation" имеет поле "isCompleted", равное по умолчанию значению false, что дает понять, что транзакция не завершена и находится в стадии обработки. "TransactionHandlerService" заносит объект класса "Transcation" в репозиторий "TransactionsRepository" для временного хранения до получения подтверждения для осуществления перевода.

"MoneyTransferService", получив объект класса "Transcation" делегирует процесс отправки сообщения сервису "MessageService". Далее происходит имитация двухфакторной идентификации (она нужна из-за того, что frontend по умолчанию всегда присылает код подтверждения, равный "0000"): "MessageService" создает поток, который записывает в файл в корневой директории сгенерированный секретный ключ и выделяет ему уникальный lock для дальнейшего взаимодействия с потоком, который должен будет прочитать данные из файла. На этом первая часть работы backend -  сервиса завершается. Ниже приведена полная схема описанного выше процесса:
 
![image1](https://raw.githubusercontent.com/MaximeNefedov/MoneyTransferService/master/images/transfer.png)

Получив ответ от backend - сервиса, содержащий id операции, frontend - сервис отправляет POST - запрос на адрес http://localhost:8080/confirm с телом формата: id операции, бутафорный код подтверждения (он всегда по умолчанию равен "0000"). Сущность - контроллер "MoneyTransferController" принимает запрос по маппингу: /confirm и преобразует входные данные в объект "ConfirmationData". Контроллер передает данные сервису "MoneyTransferService", который узнает по полученному id транзакции номер телефона отправителя с помощью "TransactionHandlerService" и с помощью с сервиса "MessageService" считывает код подтверждения из файла (формат файла: [номер телефона].txt.
Далее "MoneyTransferService" опять обращается в "TransactionHandlerService" для проверки валидности прочитанного кода. Если код верный, то перевод осуществляется и транзакция заносится в репозиторий для хранения, если нет, то списания средств не происходит.
Схема обработки запроса на маппинг /confirm:
![image2](https://raw.githubusercontent.com/MaximeNefedov/MoneyTransferService/master/images/confirm.png)

### Дополнительные сведения
1. Backend - приложение покрыто модульными и интеграционными тестами
2. Приложение выбрасывает соотвествующие исключения с читабельными пояснениями. Исключения обрабатывает контроллер "MoneyTransferControllerAdvice"
3. Каждый ключевой шаг работы программы выводится в консоль приложения и в файл логов, если выбран режим с записью сразу в два места (по умолчанию запись осуществляется и в консоль и в файл).