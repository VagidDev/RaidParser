# RaidParser

Инструмент контроля состояния железа серверов: дисковых массивов, блоков питания и батарей
RAID-контроллеров. Данные берутся из двух источников — суточного отчёта с SFTP и прямого
опроса серверов командами по SSH — разбираются, сводятся в единый статус по каждому серверу
и отдаются наружу: в консоль, в статус-файлы, в Google Sheets и через REST API.

Этот файл — справочник по API. Его достаточно, чтобы написать внешний клиент
(веб-интерфейс, бота, экспортёр метрик), не заглядывая в исходники.

---

## Режимы запуска

Режим определяется первым аргументом и выбирается **до** создания Spring-контекста,
поэтому лишние бины не поднимаются.

| Аргумент | Режим | Что поднимается |
|---|---|---|
| `-i`, `--interactive`, без аргументов | Интерактивная консоль | Профиль `console`, **без** веб-контейнера |
| `-d`, `--daemon` | Постоянно работающий сервер с REST API | Профиль `server`, Tomcat, контроллеры |
| `-h`, `--help` | Справка и выход | Контекст не создаётся |

Свойства Spring передаются как есть и на выбор режима не влияют:

```bash
java -jar raid_parser-5.0.jar -d --server.port=9090
java -jar raid_parser-5.0.jar -i --spring.config.location=./config.yaml
./script/run.sh -d --server.port=9090
```

## Быстрый старт серверного режима

Сборка требует JDK 21 (`maven.compiler.release=21`).

```bash
mvn -DskipTests package
java -jar target/raid_parser-5.0.jar -d
curl http://localhost:8080/api/v1/status/summary
```

Порт по умолчанию — `8080`, базовый путь — `/api/v1`, тело запросов и ответов — JSON (UTF-8).

## Конфигурация, важная для клиента

`src/main/resources/application.yaml`; боевые значения принято выносить в
`application-local.yaml` рядом (он игнорируется git и подхватывается автоматически).

```yaml
server:
  port: 8080

raid:
  parser:
    api:
      key: ""                    # непусто — включает проверку заголовка
      key-header: X-API-Key
      allowed-origins: []        # например ["http://localhost:5173"]
    schedule:
      enabled: false             # автообновление статусов по cron
      cron: "0 0 * * * *"        # 6 полей, формат Spring
      mode: full                 # report | hosts | full
    cache:
      server-status-ttl-seconds: 3600
      report-data-ttl-seconds: 3600
      host-overview-ttl-seconds: 86400
      servers-to-check-ttl-seconds: 300
```

**Ключ доступа.** Пока `api.key` пуст, API открыт. Если ключ задан, каждый запрос к `/api/v1`
должен нести заголовок `X-API-Key`; без него — `401`. К preflight-запросам `OPTIONS`
проверка не применяется, поэтому браузерный клиент с ключом работает.

**CORS.** По умолчанию выключен: браузерное приложение с другого адреса получит ошибку
CORS, даже если API отвечает `200`. Перечислите его origin в `api.allowed-origins`
(разрешаются методы `GET`, `POST`, `DELETE` и любые заголовки).

**Расписание.** Выключено. Если включить, демон сам обновляет статусы по cron; прогон идёт
тем же кодом, что и запуск из API, поэтому одновременно два прогона не стартуют.

---

## Модель данных

### Компоненты

Ключи компонентов в JSON и в параметрах запроса:

| Ключ | Что это |
|---|---|
| `drive_health` | Дисковый массив (mdadm, ssacli) |
| `psu_health` | Блоки питания (dmidecode, ipmitool) |
| `battery_health` | Батарея кэша RAID-контроллера (ssacli) |

В параметрах запроса принимаются три формы: `drive_health`, `DRIVE_HEALTH` и короткая `drive`.

### Severity

Обобщённая классификация. **Ориентируйтесь в интерфейсе на неё, а не на строку `status`** —
строки приходят от вендорных утилит и выглядят по-разному (вплоть до `"OK(Failed)"`,
которое означает отказ диска).

| Severity | Смысл | Разумный цвет |
|---|---|---|
| `CRITICAL` | Требует вмешательства: отказ диска, БП или батареи | красный |
| `WARNING` | Работает с оговорками: предсказанный отказ, потеря резервирования, зарядка | янтарный |
| `NO_DATA` | Данных нет или их не удалось разобрать — это **не** «всё хорошо» | серый |
| `OK` | Всё в порядке | зелёный |

Порядок в таблице — это порядок сортировки: `GET /status` отдаёт серверы худшими вперёд.

### Полный список статусов

`priority` — внутренний вес, меньше значит серьёзнее; при слиянии статусов из разных
источников остаётся более серьёзный. Клиенту он нужен разве что для собственной сортировки.

Диски (`drive_health`):

| `status` | `severity` | `priority` |
|---|---|---|
| `Interim Recovery Mode` | CRITICAL | 0 |
| `Degraded` | CRITICAL | 0 |
| `OK(Failed)` | CRITICAL | 1 |
| `OK(Predictive Failure)` | WARNING | 2 |
| `Empty` | NO_DATA | 3 |
| `OK` | OK | 4 |
| `UNSUPPORTED_TYPE`, `UNKNOWN` | NO_DATA | 2147483647 |

Блоки питания (`psu_health`):

| `status` | `severity` | `priority` |
|---|---|---|
| `Failed` | CRITICAL | 0 |
| `Power Supply Not Present` | WARNING | 1 |
| `Unclaimed` | WARNING | 2 |
| `Empty` | NO_DATA | 3 |
| `OK` | OK | 4 |
| `UNSUPPORTED_TYPE`, `UNKNOWN` | NO_DATA | 2147483647 |

Батареи (`battery_health`):

| `status` | `severity` | `priority` |
|---|---|---|
| `Failed (Replace Batteries)` | CRITICAL | 0 |
| `Recharging` | WARNING | 1 |
| `No battery` | WARNING | 2 |
| `OK(Not safe)` | WARNING | 3 |
| `Permanently Disabled` | WARNING | 4 |
| `Empty` | NO_DATA | 5 |
| `Ok` | OK | 6 |
| `UNSUPPORTED_TYPE`, `UNKNOWN` | NO_DATA | 2147483647 |

Отдельно: если у сервера по компоненту нет данных вообще, приходит синтетический
`{"status": "NO DATA", "severity": "NO_DATA", "priority": 2147483647, "details": ""}`.
Компонент никогда не выпадает из ответа — все три ключа присутствуют всегда.

### Статус сервера

```json
{
  "server": "test-host-01",
  "severity": "CRITICAL",
  "components": {
    "drive_health": {
      "status": "OK(Failed)",
      "severity": "CRITICAL",
      "priority": 1,
      "details": "logicaldrive 1 (558.7 gb, raid 1+0, ok)\nphysicaldrive 1i:3:2 (port 1i:box 3:bay 2, sas, 300 gb, failed)"
    },
    "psu_health":     { "status": "OK", "severity": "OK", "priority": 4, "details": "" },
    "battery_health": { "status": "Ok", "severity": "OK", "priority": 6, "details": "" }
  }
}
```

`severity` сервера — худшая среди его компонентов. `details` — это строки вывода утилиты,
на основании которых сделан вывод: многострочный текст (данные из отчёта приводятся
к нижнему регистру при разборе, вывод команд по SSH — как есть). Годится для блока `<pre>`
или раскрывающейся детализации, но не для заголовка.

---

## Эндпоинты

Все ответы — `application/json`. Временные метки — ISO-8601 в UTC, с дробной частью
до наносекунд (`2026-08-21T07:22:41.316029104Z`); примеры ниже укорочены для читаемости.

### Статусы

Читают кэш, ничего не запускают, отвечают мгновенно.

#### `GET /api/v1/status`

| Параметр | Значения | По умолчанию |
|---|---|---|
| `component` | `drive_health` \| `psu_health` \| `battery_health` (или `drive`/`psu`/`battery`) | все |
| `severity` | `CRITICAL` \| `WARNING` \| `NO_DATA` \| `OK`, любой регистр; можно повторять параметр или перечислить через запятую | все |
| `server` | часть имени сервера, без учёта регистра | все |

С `component` в ответе остаётся только этот компонент, и `severity` сервера считается по нему.

```bash
curl "http://localhost:8080/api/v1/status?severity=critical,warning&component=drive"
```

```json
{
  "generatedAt": "2026-08-21T07:22:49.504Z",
  "count": 1,
  "servers": [ { "server": "test-host-01", "severity": "CRITICAL", "components": { "drive_health": { "...": "..." } } } ]
}
```

#### `GET /api/v1/status/summary`

Готовые счётчики для дашборда — отдельный обход списка не нужен.

```json
{
  "generatedAt": "2026-08-21T07:22:49.446Z",
  "servers": 1,
  "bySeverity":  { "CRITICAL": 1, "WARNING": 0, "NO_DATA": 0, "OK": 0 },
  "byComponent": {
    "drive_health":   { "CRITICAL": 1, "WARNING": 0, "NO_DATA": 0, "OK": 0 },
    "psu_health":     { "CRITICAL": 0, "WARNING": 0, "NO_DATA": 0, "OK": 1 },
    "battery_health": { "CRITICAL": 0, "WARNING": 0, "NO_DATA": 0, "OK": 1 }
  },
  "attentionRequired": ["test-host-01"]
}
```

`bySeverity` считает серверы по худшему компоненту, `byComponent` — по каждому компоненту
отдельно, `attentionRequired` — имена серверов с `CRITICAL` или `WARNING`, худшие первыми.

#### `GET /api/v1/status/{serverName}`

Один сервер (объект статуса, как выше) или `404`, если его нет в кэше. Имя без учёта регистра.

### Анализ

Работают **синхронно**: ответ приходит с готовым результатом. `/analyze/hosts` ходит по SSH
и может занимать минуты — ставьте таймаут клиента с запасом. Пока прогон идёт, любой другой
запуск получает `409`.

| Метод и путь | Что делает | Параметры |
|---|---|---|
| `POST /api/v1/analyze/report` | Разбирает суточный отчёт (локальный файл или скачивает с SFTP) | `date=2026-08-21`, по умолчанию сегодня |
| `POST /api/v1/analyze/hosts` | Опрашивает серверы командами по SSH | — |
| `POST /api/v1/analyze/full` | То и другое, статусы объединяются | `date=...` |
| `GET /api/v1/analyze/state` | Идёт ли прогон и чем закончился прошлый | — |

```json
{
  "mode": "report",
  "startedAt": "2026-08-21T07:22:41.316Z",
  "finishedAt": "2026-08-21T07:22:41.322Z",
  "durationMs": 6,
  "servers": 1,
  "statuses": [ { "server": "test-host-01", "severity": "CRITICAL", "components": { "...": "..." } } ]
}
```

`GET /api/v1/analyze/state`:

```json
{
  "running": false,
  "lastRun": { "mode": "report", "startedAt": "...", "finishedAt": "...", "durationMs": 5, "servers": 1 }
}
```

`lastRun` равен `null`, пока после старта не было ни одного прогона.
Если отчёта за дату нет ни локально, ни на SFTP — `404 report_not_found`.

### Экспорт

| Метод и путь | Что делает |
|---|---|
| `POST /api/v1/export/files` | Выгружает статусы из кэша в статус-файлы |
| `POST /api/v1/export/sheets` | Выгружает статусы из кэша в Google Sheets |

```json
{ "files": { "drive_health": "./status/drive-status.txt",
             "psu_health": "./status/psu-status.txt",
             "battery_health": "./status/battery-status.txt" } }
```

`/export/sheets` отвечает `200` только если выгрузились все три компонента, иначе `502`
с тем же телом — причина (не заполнен `spreadsheet-id`, ошибка авторизации Google) в логе:

```json
{ "success": false, "exported": { "drive_health": false, "psu_health": false, "battery_health": false } }
```

### Кэши

Пять кэшей с независимым TTL: `status` (статусы серверов), `report` (данные из файла отчёта),
`hosts` (список серверов), `hosts-file` (сырой ответ HostOverview на диске), `commands`
(команды из конфига проверки).

| Метод и путь | Что делает |
|---|---|
| `GET /api/v1/caches` | Состояние всех кэшей |
| `DELETE /api/v1/caches` | Сбросить все |
| `DELETE /api/v1/caches/{name}` | Сбросить один; неизвестное имя — `404` |

Оба `DELETE` возвращают состояние кэшей после сброса — отдельный `GET` не нужен.

```json
[ { "name": "commands", "loaded": false, "size": 0, "ageSeconds": 0, "ttlSeconds": 300, "expired": false },
  { "name": "status",   "loaded": true,  "size": 42, "ageSeconds": 137, "ttlSeconds": 3600, "expired": false } ]
```

`expired: true` означает, что данные устарели и будут перечитаны при следующем обращении;
чтение само вычищает просроченные записи, отдельный сброс для этого не нужен.

### Серверы

| Метод и путь | Что делает | Параметры |
|---|---|---|
| `GET /api/v1/hosts` | Список серверов из HostOverview | `onlyPhysical=true\|false`, по умолчанию `true` |
| `POST /api/v1/hosts/refresh` | Перечитать список, минуя кэш |  |

`onlyPhysical=true` оставляет физические серверы с корректным портом — те, которые
реально проверяются по SSH.

```json
[ { "name": "srv", "ip": "10.0.0.1", "port": 22, "serverType": "physical", "connectionType": "" } ]
```

---

## Ошибки

Единый формат у всех ошибок:

```json
{ "error": "not_found",
  "message": "Server `nosuch` is not present in the status cache",
  "timestamp": "2026-08-21T07:22:49.580Z" }
```

| Код | `error` | Когда |
|---|---|---|
| `400` | `bad_request` | Неверное значение параметра; `message` перечисляет допустимые |
| `401` | `unauthorized` | Задан `api.key`, а заголовок отсутствует или неверен |
| `404` | `not_found` | Нет такого сервера в кэше или такого кэша |
| `404` | `report_not_found` | Нет отчёта за указанную дату |
| `409` | `analysis_already_running` | Прогон анализа уже идёт |
| `502` | — | Экспорт в Google Sheets не выполнен (тело — `SheetsExportResponse`) |
| `500` | `internal_error` | Непредвиденная ошибка, подробности в логе сервера |

---

## Что учесть при написании клиента

- **Кэш при старте пуст.** Свежий демон отдаёт `count: 0`, пока не выполнен хотя бы один
  анализ или не включено расписание. Интерфейс должен внятно показывать «данных ещё нет»,
  а не «всё хорошо»; кнопка запуска анализа тут уместна.
- **`NO_DATA` — не `OK`.** Сервер без части данных не здоров, он непроверен. Смешивать
  их в одном зелёном счётчике нельзя.
- **Долгие операции.** `POST /analyze/hosts` и `/analyze/full` держат соединение минутами.
  Показывайте прогресс-состояние, а на `409` отвечайте «прогон уже идёт», опрашивая
  `GET /analyze/state` до `running: false`.
- **Push-канала нет.** Ни WebSocket, ни SSE. Обновление — поллингом: `GET /status/summary`
  дёшев, его можно опрашивать раз в 10–30 секунд.
- **Пагинации и сортировки на сервере нет** (кроме сортировки по severity). Серверов
  десятки, весь список приходит целиком — фильтруйте и сортируйте на клиенте либо
  параметрами `component`, `severity`, `server`.
- **`details` — многострочный текст**, иногда длинный. Не в таблицу, а в раскрывающийся блок.
- **CORS обязателен** для браузерного клиента с другого адреса, см. `api.allowed-origins`.
  Без него запрос не дойдёт, хотя API рабочий.
- **`X-API-Key`** добавляйте, если ключ настроен, ко всем запросам, включая `GET`.
- **Единственный источник правды об именах серверов — сам API.** Имена приходят из
  HostOverview и файла отчёта и могут не совпадать между собой; сопоставлять их
  на клиенте не нужно, кэш статусов уже сводит их по имени.

## Чего в API нет

Осознанно, чтобы не изобретать раньше времени: пользователей и ролей (только общий ключ),
истории статусов (кэш держит текущее состояние, не временной ряд), асинхронных задач
с идентификаторами, метрик в формате Prometheus. Если что-то из этого нужно интерфейсу —
это доработка API, а не обход на клиенте.
