# Food-card Service

Сервис приёма, валидации и сохранения банковских файлов.

## Назначение

`food-card` принимает файлы от `generator-service`, валидирует структуру и данные, сохраняет аудит обработки в PostgreSQL и бизнес-записи в Oracle.

Основной поток:

```text
Generator
   ↓
multipart / raw HTTP / gRPC / shared directory
   ↓
Food-card
   ├── PostgreSQL: pom.*
   └── Oracle: GRU.GRU_VISTA_TAB
```

После успешной записи в Oracle данные забирает `app-adapter`.

## Стек

- Java 21
- Spring Boot 3.3.5
- Spring Web
- gRPC
- PostgreSQL
- Oracle XE
- Liquibase
- Docker / Docker Compose

## Порты

| Порт | Назначение |
|---|---|
| `8081` | HTTP REST |
| `9090` | gRPC |

## Входные каналы

### Multipart HTTP

```http
POST /api/files/upload
Content-Type: multipart/form-data
```

Generator отправляет файл в multipart-part:

```text
file
```

### Raw HTTP

```http
POST /api/files/stream
```

### gRPC

По умолчанию:

```text
9090
```

### Shared directory

Food-card периодически сканирует входную директорию.

Docker path:

```text
/app/input_files
```

Интервал задаётся:

```text
SPRING_FILES_SCAN_INTERVAL
```

Текущее значение:

```text
5000 ms
```

## Обработка файла

Файл проходит:

```text
input
  ↓
data/YYYYMMDD/in_progress
  ↓
validation + persistence
  ├── success
  └── error
```

Основные директории:

```text
data/YYYYMMDD/in_progress
data/YYYYMMDD/success
data/YYYYMMDD/error
```

Body-строка должна иметь длину:

```text
152 символа
```

## Хранилища

### PostgreSQL

Используется для технического аудита обработки файлов.

Схема:

```text
pom
```

Основные таблицы:

```text
pom.file
pom.unit
pom.unit_error
```

### Oracle

Используется для бизнес-данных GRU.

Основная таблица:

```text
GRU.GRU_VISTA_TAB
```

Ключевые поля состояния:

```text
FOC_STATUS
FOC_STATUS_TS
FOC_TYPE
FOC_TS
```

`FOC_TS` используется как плановое время обработки для `INTIME`.

`FOC_STATUS_TS` используется как время изменения технического статуса.

Основные статусы:

```text
WAIT
IN_PROCESS
SUCCESS
ERROR
```

После успешного приёма файла записи создаются со статусом:

```text
WAIT
```

## Liquibase

Food-card владеет миграциями:

- PostgreSQL `pom.*`;
- Oracle `GRU.*`.

Структура:

```text
src/main/resources/db/changelog/
├── postgres/
│   ├── db.changelog-master.yaml
│   ├── 001-pom-schema.sql
│   ├── 002-pom-tables.sql
│   └── 003-pom-constraints-indexes.sql
│
└── oracle/
    ├── db.changelog-master.yaml
    ├── 001-gru-vista.sql
    ├── 002-gru-reject.sql
    ├── 003-gru-processing-state.sql
    └── 004-gru-indexes-grants.sql
```

Так как используются два DataSource, Liquibase запускается отдельными `SpringLiquibase` bean для PostgreSQL и Oracle.

Автоматический стандартный Liquibase Spring Boot отключён:

```yaml
spring:
  liquibase:
    enabled: false
```

Это не отключает вручную созданные `SpringLiquibase` bean.

## Oracle user для adapter

Объекты `GRU` принадлежат food-card, но adapter работает под пользователем:

```text
ACC_APP_ADAPTER
```

На чистом Oracle volume этого пользователя необходимо создать до применения GRU grants:

```sql
CREATE USER ACC_APP_ADAPTER IDENTIFIED BY root;

ALTER USER ACC_APP_ADAPTER DEFAULT TABLESPACE USERS;
ALTER USER ACC_APP_ADAPTER QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION TO ACC_APP_ADAPTER;
GRANT CREATE TABLE TO ACC_APP_ADAPTER;
GRANT CREATE SEQUENCE TO ACC_APP_ADAPTER;
```

После этого Liquibase GRU выдаёт необходимые права пользователю adapter.

> При `docker compose down -v` Oracle volume удаляется, поэтому созданный пользователь также исчезнет.

## Docker Compose

Актуальная схема:

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:16-alpine
    container_name: food_card_postgres

    environment:
      POSTGRES_DB: postgres_db
      POSTGRES_USER: user
      POSTGRES_PASSWORD: root

    ports:
      - "5432:5432"

    volumes:
      - pgdata:/var/lib/postgresql/data

    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user -d postgres_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  oracle:
    image: gvenzl/oracle-xe:11-slim
    container_name: food_card_oracle

    environment:
      ORACLE_PASSWORD: root
      APP_USER: gru
      APP_USER_PASSWORD: root

    ports:
      - "1521:1521"

    volumes:
      - oracledata:/u01/app/oracle

    healthcheck:
      test:
        [
          "CMD-SHELL",
          "echo 'SELECT 1 FROM DUAL;' | sqlplus -S gru/root@//localhost:1521/XE || exit 1"
        ]
      interval: 10s
      timeout: 5s
      retries: 15
      start_period: 30s

    networks:
      - default
      - food_bridge

  app:
    build:
      context: .
      dockerfile: Dockerfile

    container_name: food-card-app

    ports:
      - "8081:8081"
      - "9090:9090"

    environment:
      SPRING_DATASOURCE_POSTGRES_URL: jdbc:postgresql://postgres:5432/postgres_db
      SPRING_DATASOURCE_POSTGRES_USERNAME: user
      SPRING_DATASOURCE_POSTGRES_PASSWORD: root

      SPRING_DATASOURCE_ORACLE_URL: jdbc:oracle:thin:@//oracle:1521/XE
      SPRING_DATASOURCE_ORACLE_USERNAME: gru
      SPRING_DATASOURCE_ORACLE_PASSWORD: root

      SPRING_FILES_DIR: /app/input_files
      SPRING_FILES_DATA: /app/data
      SPRING_FILES_SCAN_INTERVAL: "5000"

      SPRING_JPA_PROPERTIES_ORACLE_JDBC_TIMEZONEASREGION: "false"
      TZ: Europe/Moscow

    volumes:
      - ./data:/app/data
      - D:/gpb_tasks/shared_exchange:/app/input_files
      - ./config:/app/config

    depends_on:
      postgres:
        condition: service_healthy
      oracle:
        condition: service_healthy

    networks:
      - default
      - food_bridge

volumes:
  pgdata:
  oracledata:

networks:
  food_bridge:
    external: true
```

Сеть создать один раз:

```bash
docker network create food_bridge
```

Если сеть уже существует, команда не нужна.

## Запуск

```bash
docker compose up -d --build
```

Логи:

```bash
docker logs -f food-card-app
```

Состояние контейнеров:

```bash
docker compose ps
```

## Подключение к Oracle

Из Windows/хоста:

```text
jdbc:oracle:thin:@//localhost:1521/XE
```

Из контейнера `food-card`:

```text
jdbc:oracle:thin:@//oracle:1521/XE
```

Из generator через `food_bridge`:

```text
jdbc:oracle:thin:@//food_card_oracle:1521/XE
```

## Подключение к PostgreSQL

Из хоста:

```text
jdbc:postgresql://localhost:5432/postgres_db
```

Из контейнера:

```text
jdbc:postgresql://postgres:5432/postgres_db
```

## SQL-проверки

Количество бизнес-записей:

```sql
SELECT COUNT(*)
FROM GRU.GRU_VISTA_TAB;
```

Статусы:

```sql
SELECT FOC_STATUS, COUNT(*)
FROM GRU.GRU_VISTA_TAB
GROUP BY FOC_STATUS;
```

Полностью очистить VISTA для нового теста:

```sql
TRUNCATE TABLE GRU.GRU_VISTA_TAB;
```

Через Docker:

```bash
docker exec -it food_card_oracle \
  sqlplus gru/root@//localhost:1521/XE
```

## E2E-тест

Для полного сценария:

```text
Generator
   ↓ multipart
Food-card
   ↓
GRU.GRU_VISTA_TAB (WAIT)
   ↓
App-adapter
```

1. Поднять `food-card`.
2. Поднять Kafka.
3. Запустить `app-adapter`.
4. Запустить generator.
5. В generator вызвать `/api/parametres`.
6. Проверить успешную обработку файла.
7. Проверить строки `WAIT` в `GRU.GRU_VISTA_TAB`.
8. Проверить, что adapter забирает их для отправки в Kafka.

## Важные замечания

### Oracle ORA-12505

Для Oracle XE использовать service-name URL:

```text
jdbc:oracle:thin:@//oracle:1521/XE
```

Не использовать старую SID-форму, если listener зарегистрировал только service `XE`.

### Дублирование файлов

Если generator одновременно:

- отправляет файл через multipart;
- пишет этот же файл в shared-directory,

food-card может увидеть один файл дважды.

Для чистого теста рекомендуется использовать только один транспорт.

### Liquibase ownership

- `food-card` создаёт и мигрирует `pom.*` и `GRU.*`;
- `app-adapter` создаёт только `ACC_APP_ADAPTER.*`;
- generator не владеет Liquibase-миграциями.
