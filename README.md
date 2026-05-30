# WarehouseAppBackend

## Ключи шифрования

Ключи используются для подписи и проверки JWT-токенов.  
Перед запуском проекта необходимо создать папку `keys` в каталоге `src/main/resources` и поместить туда два файла:

- `privateKey.pem` — приватный ключ
- `publicKey.pem` — публичный ключ

Структура каталога должна быть следующей:

```text
src/
└── main/
    └── resources/
        └── keys/
            ├── privateKey.pem
            └── publicKey.pem
```

## Генерация ключей

Сначала создайте папку для ключей:

```bash
mkdir -p src/main/resources/keys
```

Сгенерируйте приватный ключ:

```bash
openssl genrsa -out src/main/resources/keys/privateKey.pem 2048
```

Сгенерируйте публичный ключ на основе приватного:

```bash
openssl rsa -in src/main/resources/keys/privateKey.pem -pubout -outform PEM -out src/main/resources/keys/publicKey.pem
```

## Настройка `.env`

После этого необходимо создать файл `.env` по примеру файла `.env.example`.

Например:

```bash
cp .env.example .env
```

В файле `.env` обязательно должны быть указаны следующие пути к ключам:

```env
PRIVATE_KEY_PATH=/app/keys/privateKey.pem
PUBLIC_KEY_PATH=/app/keys/publicKey.pem
```

Эти пути должны быть указаны именно в таком виде, так как они используются внутри Docker-образа.  
При сборке приложения в Docker пути до ключей прописываются относительно контейнера, а папка с ключами монтируется внутрь образа по пути:

```text
/app/keys/
```

То есть локальные файлы:

```text
src/main/resources/keys/privateKey.pem
src/main/resources/keys/publicKey.pem
```

в контейнере должны быть доступны как:

```text
/app/keys/privateKey.pem
/app/keys/publicKey.pem
```

## Запуск проекта

Для запуска проекта через Docker Compose используйте команду:

```bash
docker compose up -d
```

После этого контейнеры будут запущены в фоновом режиме.

## Остановка проекта

Для остановки контейнеров выполните:

```bash
docker compose down
```