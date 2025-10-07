# Как это понимать? (RU)

## Описание

Плагин `DataTransfer` - пример реализации обмена данными между клиентом и сервером. Используется передача пакетов через кастомный канал.

## Зависимости

Для работы плагина требуется библиотека PacketEvents:

- PacketEvents 2.9.4+ - https://github.com/retrooper/packetevents
- InfoTags на стороне клиента - https://github.com/FrustratedQuim/InfoTags

## Крутые пермишены

Плагин использует следующее разрешение:

- `datatransfer.infotags` - по сути своей опционально. Добавлено, чтобы условные сторонние игроки с модом не могли получать данные.

## Как работает плагин

1. **Регистрация каналов**: Плагин регистрирует кастомные пакеты для отправки и получения данных.
2. **HandShake**: Отсылаемый клиентом хэндшейк пакет обрабатывается и если игрок соблюдает условие (в нашем случае пермишен), то доступ разрешается.
3. **Запрос данных**: Дальнейшие `get` пакеты будут указывать серверу на получение заранее установленных данных об игроке и отправку таковых обратно.

# How to understand this? (EN)

## Description

The `DataTransfer` plugin is an example of implementing data exchange between a client and a server. It uses packet transmission through a custom channel.

## Dependencies

The plugin requires the PacketEvents library:

- PacketEvents 2.9.4+ - https://github.com/retrooper/packetevents
- InfoTags on the client side - https://github.com/FrustratedQuim/InfoTags

## Key Permissions

The plugin uses the following permission:

- `datatransfer.infotags` - optional by nature. Added to prevent certain third-party mod players from receiving data.

## How the plugin works

1. **Channel Registration**: The plugin registers custom packets for sending and receiving data.
2. **Handshake**: The handshake packet sent by the client is processed, and if the player meets the condition (in this case, the permission), access is granted.
3. **Data Request**: Subsequent `get` packets signal the server to retrieve predefined player data and send it back.