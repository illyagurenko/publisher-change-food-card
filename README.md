# Food-card Service (Publisher)

Сервис высокоскоростного приёма, потоковой валидации и сохранения банковских файлов с фиксацией аудита обработки в PostgreSQL и загрузкой бизнес-проводок в Oracle (`GRU.GRU_VISTA_TAB`).

Реализован на **Java 21**, **Spring Boot 3.3.5**, **gRPC / Protobuf** и **Liquibase** с поддержкой двух независимых источников данных (Dual DataSource: PostgreSQL + Oracle).

---

## Архитектура и схема работы

Сервис принимает файлы по нескольким транспортным каналам, проверяет структуру строк (152 символа) и регистрирует данные в двух базах:

```text
                  [ Генератор банковских файлов ]
                                 │
                 ┌───────────────┼───────────────┐
                 ▼               ▼               ▼
           Multipart HTTP    Raw Stream        gRPC
            (порт 8081)     (порт 8081)     (порт 9090)
                 └───────────────┬───────────────┘
                                 │
                                 ▼
                     ┌───────────────────────┐
                     │    FoodCardService    │
                     │  Потоковая валидация  │
                     └───────────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
   ┌───────────────────────────┐   ┌───────────────────────────┐
   │        PostgreSQL         │   │        Oracle XE          │
   │      (Схема: pom)         │   │       (Схема: GRU)        │
   │  Аудит обработки файлов   │   │  Бизнес-таблица:          │
   │  pom.file, pom.unit       │   │  GRU.GRU_VISTA_TAB        │
   │                           │   │  (FOC_STATUS = 'WAIT')    │
   └───────────────────────────┘   └───────────────────────────┘
                                                 │
                                                 ▼
                                        [ Сервис app-adapter ]
                                        (Вычитка батчей в Kafka)
```

---

## Стек технологий

* **Язык:** Java 21 (Records, Sealed Types, Pattern Matching)
* **Фреймворк:** Spring Boot 3.3.5 (Spring Web, Spring Data JPA)
* **RPC и сериализация:** gRPC (`io.grpc`), Protobuf 3 (`file_upload.proto`)
* **Базы данных:**
  * **PostgreSQL 16** (схема `pom` — аудит и единицы обработки)
  * **Oracle Database XE 11g/19c** (схема `GRU` — финансовые записи)
* **Миграции БД:** Liquibase (два изолированных `SpringLiquibase` бина под каждый DataSource)
* **Сборщик:** Gradle 9.5

---

## Порты и сетевая архитектура

Сервис разворачивается в Docker-сети `food_bridge`:

| Контейнер | Назначение | Порт хоста | Внутренний порт |
| :--- | :--- | :--- | :--- |
| **`food-card-app`** | Основное приложение | `8084` (HTTP), `9090` (gRPC) | `8081`, `9090` |
| **`food_card_oracle`**| База данных Oracle XE | `1521` | `1521` |
| **`food_card_postgres`**| База данных PostgreSQL | `5433` | `5432` |

---

## Входные каналы приёма файлов

1. **Multipart HTTP:**
   `POST /api/files/upload` (Content-Type: `multipart/form-data`, имя поля: `file`)
2. **Raw Stream HTTP:**
   `POST /api/files/stream` (потоковая передача байтов без буферизации в память)
3. **gRPC канал:**
   Порт `9090`, сервис `FileUploadService` (`UploadFileStream`)
4. **Shared Directory Scanner:**
   Периодическое сканирование директории `/app/input_files` каждые `SPRING_FILES_SCAN_INTERVAL` (по умолчанию 5000 мс).

---

## База данных и структура таблиц

### 1. PostgreSQL (схема `pom`):
* `pom.file` — реестр загруженных файлов, контрольные суммы, статус обработки.
* `pom.unit` — строки файлов с техническими метриками.
* `pom.unit_error` — реестр ошибок валидации строк.

### 2. Oracle (схема `GRU`):
* **`GRU.GRU_VISTA_TAB`** — основная таблица проводок:
  * `ID` — уникальный идентификатор записи (`NUMBER(12)`).
  * `SYSTEMACCOUNT` — банковский счет контрагента (`VARCHAR2(32)`).
  * `CURRENCY` — код валюты (например, `'RUB'`).
  * `OPERATION` — тип банковской операции (`'CR'`, `'DR'`, `'ZR'`).
  * `FOC_STATUS` — статус обработки (`WAIT`, `IN_PROCESS`, `SUCCESS`, `ERROR`).
  * `FOC_TYPE` и `FOC_TS`:
    * Для `IMMEDIATE` поле `FOC_TS` строго **`NULL`** (готов к немедленной вычитке адаптером).
    * Для `INTIME` поле `FOC_TS` содержит плановое время исполнения (`FOC_TS <= SYSDATE`).
  * `TIME_STAMP_UPDATE` — время фиксации успешного завершения обработки в адаптере (при начальной вставке всегда `NULL`).
* **`GRU.GRU_REJECT_TAB`** — таблица брака, куда адаптер заносит отклонённые строки.

---

## Автоматическая инициализация пользователя `ACC_APP_ADAPTER` в Oracle

Для работы сервиса `app-adapter` и корректного наката Liquibase-миграции с грантами (`003-gru-indexes-grants.sql`), пользователь **`ACC_APP_ADAPTER` создается автоматически** при первом и каждом последующем запуске контейнера `food_card_oracle`.

Это обеспечивается скриптом [`oracle-init/01-init-users.sql`](file:///d:/gpb_task/publisher-change-food-card/oracle-init/01-init-users.sql), который смонтирован в системные каталоги инициализации контейнера:
* `/container-entrypoint-initdb.d` (выполняется при первичной инициализации новой базы).
* `/container-entrypoint-startdb.d` (выполняется при каждом последующем старте контейнера).

Скрипт идемпотентно проверяет наличие пользователя `ACC_APP_ADAPTER`, создает его с паролем `root`, квотой `UNLIMITED` на tablespace `USERS` и выдает необходимые привилегии (`CREATE SESSION`, `CREATE TABLE`, `CREATE SEQUENCE`, `CONNECT`, `RESOURCE`), поэтому **никаких ручных действий в SQL*Plus выполнять не требуется**.

---

## Пошаговая инструкция по запуску с нуля (Clone & Run Guide)

### Шаг 1. Клонирование репозитория
```bash
git clone https://github.com/illyagurenko/publisher-change-food-card
cd publisher-change-food-card
```

### Шаг 2. Создание Docker-сети
Создайте общую сеть `food_bridge`:
```bash
docker network create food_bridge
```

### Шаг 3. Запуск баз данных (Oracle и PostgreSQL)
```bash
docker compose up -d oracle postgres
```
Дождитесь перехода Oracle в статус Healthy (обычно 30–60 секунд):
```bash
docker compose ps
```
*При старте контейнера `food_card_oracle` пользователь `ACC_APP_ADAPTER` и права для `GRU` создаются автоматически.*

### Шаг 4. Сборка приложения через Gradle
* **Для Windows (PowerShell / CMD):**
  ```powershell
  .\gradlew.bat clean build -x test
  ```
* **Для Linux / macOS:**
  ```bash
  ./gradlew clean build -x test
  ```

### Шаг 5. Сборка Docker-образа и запуск сервиса
```bash
docker compose build app
docker compose up -d app
```

### Шаг 6. Проверка работоспособности
1. **Просмотр логов запуска и выполнения миграций Liquibase:**
   ```bash
   docker logs -f food-card-app
   ```
   В логах должно отобразиться успешное применение ченджсетов Liquibase для Oracle и PostgreSQL, а также запуск Tomcat на порту `8081` и gRPC на порту `9090`.

2. **Проверка через SQL в Oracle:**
   ```bash
   docker exec -it food_card_oracle sqlplus gru/root@//localhost:1521/XE
   ```
   ```sql
   SELECT TABLE_NAME FROM USER_TABLES;
   -- Должны присутствовать: GRU_VISTA_TAB, GRU_REJECT_TAB, DATABASECHANGELOG
   ```
