# Как это понимать? (RU)

## Описание

Плагин `DataTransfer` - пример реализации обмена кастомными данными между модом на клиенте и плагином на сервере. Всё реализовано через собственные каналы, по которым просто предаются пакеты.

## Зависимости

Для работы плагина требуется библиотека PacketEvents:

- PacketEvents 2.9.4 - https://github.com/retrooper/packetevents
- InfoTags на стороне клиента - https://github.com/FrustratedQuim/InfoTags

## Крутые пермишены

Плагин использует следующее разрешение:

- `datatransfer.infotags` - по сути своей опционален. Добавлен, чтобы условные сторонние игроки с модом не могли получать данные.

## Как работает это дело

1. **Регистрация каналов**: Плагин регистрирует каналы:
    - Входящие: `datatransfer:handshake`, `datatransfer:playerinfo_request`.
        - Получение запрос от дома на первоначальное соединение + запрос данных.
    - Исходящий: `datatransfer:playerinfo`.
        - Отправка пакета данных обратно клиенту.
2. **Обработка handshake**: При получении пакета `datatransfer:handshake` - плагин проверяет подлинность и добавляет игрока в список отслеживаемых (`trackedPlayers`), дабы в дальнейшем понимать: "Реагировать на его запросы данных"
3. **Запрос данных**: Пакет `datatransfer:playerinfo_request` отсылаемый клиентом запрашивает данные об игроке по имени (Можно реализовать иначе, ник сделан для удобства). Сервер дёргает данные по нику (имя, здоровье, голод) и кидает клиенту через `datatransfer:playerinfo`.

## Добавление своих данных

Чтобы добавить свои данные в передаваемый пакет, нужно подкорректировать метод `sendPlayerData` в файле `PacketHandler.kt`. Например, сейчас отправляются имя, здоровье и голод игрока. Допустим, нужно добавить координаты игрока (x, y, z).

### Шаг 1: Изменение отправки данных

- Добавьте запись координат в метод `sendPlayerData`:
    - p.s. Это можно также сделать стрингом и парсить на стороне клиента (легче), но возьмём в пример более "правильную" подачу.

```kotlin
private fun sendPlayerData(to: Player, about: Player) {
    if (!plugin.trackedPlayers.contains(to)) return

    val byteBuf = Unpooled.buffer()
    DataUtils.writeString(byteBuf, "Custom packet from DataTransfer")
    DataUtils.writeString(byteBuf, about.name)
    byteBuf.writeDouble(about.health)
    byteBuf.writeInt(about.foodLevel)
    // Добавляем координаты игрока
    byteBuf.writeDouble(about.location.x)
    byteBuf.writeDouble(about.location.y)
    byteBuf.writeDouble(about.location.z)

    // Переводим в массив байтов, дабы запихнуть в пакет
    val byteArray = ByteArray(byteBuf.readableBytes())
    byteBuf.readBytes(byteArray)

    val channelId = ResourceLocation("datatransfer:playerinfo")
    val packet = WrapperPlayServerPluginMessage(channelId, byteArray)
    PacketEvents.getAPI().playerManager.sendPacket(to, packet)
    byteBuf.release()
}
```

### Шаг 2: Обработка на стороне мода

На стороне мода нужно соответственно изменить обработку входящего пакета `datatransfer:playerinfo` в `PlayerInfoPayload.java`:

```java
public record PlayerInfoPayload(String comment, String playerName, double health, int foodLevel, double x, double y, double z) implements CustomPayload {

    public static final Id<PlayerInfoPayload> ID = new Id<>(Identifier.of("datatransfer:playerinfo"));

    public static final PacketCodec<PacketByteBuf, PlayerInfoPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, PlayerInfoPayload::comment,
            PacketCodecs.STRING, PlayerInfoPayload::playerName,
            PacketCodecs.DOUBLE, PlayerInfoPayload::health,
            PacketCodecs.INTEGER, PlayerInfoPayload::foodLevel,
            // Добавляем обработку координат
            PacketCodecs.DOUBLE, PlayerInfoPayload::x,
            PacketCodecs.DOUBLE, PlayerInfoPayload::y,
            PacketCodecs.DOUBLE, PlayerInfoPayload::z,
            PlayerInfoPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    // Также в сам рекорд
    public record PlayerData(String comment, double health, int foodLevel, double x, double y, double z) {}
}
```

Затем обновите обработку в `NetworkHandler.java`:

```java
ClientPlayNetworking.registerGlobalReceiver(PlayerInfoPayload.ID, (payload, context) ->
        playerDataCache.put(payload.playerName(), new PlayerInfoPayload.PlayerData(
        payload.comment(), payload.health(), payload.foodLevel(), payload.x(), payload.y(), payload.z())));
```

### Шаг 3: Отображение новых данных

Обновите `TextDisplayManager.java` как угодно для отображения новых штук:

```java
String finalString;
PlayerInfoPayload.PlayerData data = NetworkHandler.playerDataCache.get(focused);
if (data != null) {
finalString = String.format("§6Ник: §e%s\n§6Здоровье: §e%.1f\n§6Еда: §e%d\n§6Координаты: §e(%.1f, %.1f, %.1f)\n\n§6Комментарий: §e%s",
                            focused, data.health(), data.foodLevel(), data.x(), data.y(), data.z(), data.comment());
        } else {
String value = NetworkHandler.isHandshakeSuccessful() && !NetworkHandler.isRequestsBlocked() ? "Ожидание..." : "null";
finalString = String.format("§6Ник: §e%s\n§6Здоровье: §e%s\n§6Еда: §e%s\n§6Координаты: §e%s",
                            focused, value, value, value);
}
```

### Важно

- Порядок записи данных на сервере должен совпадать с порядком чтения на клиенте.
- Используйте методы `DataUtils` для сериализации/десериализации данных, так-как по умолчанию для того-же String-типа нет поддержки.

---

# How to understand this? (EN)

## Description

The `DataTransfer` plugin is an example of implementing custom data exchange between a client-side mod and a server-side plugin. Everything is implemented through custom channels that simply transmit packets.

## Dependencies

The plugin requires the following library to function:

- PacketEvents 2.9.4 - https://github.com/retrooper/packetevents
- InfoTags on the client side - https://github.com/FrustratedQuim/InfoTags

## Permissions

The plugin uses the following permission:

- `datatransfer.infotags` - essentially optional. Added to prevent unauthorized players with the mod from accessing data.

## How it works

1. **Channel Registration**: The plugin registers the following channels:
    - Incoming: `datatransfer:handshake`, `datatransfer:playerinfo_request`.
        - Receives requests from the client for initial connection and data requests.
    - Outgoing: `datatransfer:playerinfo`.
        - Sends the data packet back to the client.
2. **Handshake Processing**: Upon receiving a `datatransfer:handshake` packet, the plugin verifies authenticity and adds the player to the `trackedPlayers` list to determine whether to respond to their data requests in the future.
3. **Data Request**: The `datatransfer:playerinfo_request` packet, sent by the client, requests data about a player by their name (this can be implemented differently; the name was chosen for convenience). The server retrieves the data (name, health, hunger) based on the name and sends it to the client via `datatransfer:playerinfo`.

## Adding Custom Data

To add custom data to the transmitted packet, you need to modify the `sendPlayerData` method in the `PacketHandler.kt` file. For example, it currently sends the player's name, health, and hunger. Let's say you want to add the player's coordinates (x, y, z).

### Step 1: Modifying Data Sending

- Add the recording of coordinates in the `sendPlayerData` method:
    - Note: This can also be done as a string and parsed on the client side (easier), but we’ll use a more "proper" approach as an example.

```kotlin
private fun sendPlayerData(to: Player, about: Player) {
    if (!plugin.trackedPlayers.contains(to)) return

    val byteBuf = Unpooled.buffer()
    DataUtils.writeString(byteBuf, "Custom packet from DataTransfer")
    DataUtils.writeString(byteBuf, about.name)
    byteBuf.writeDouble(about.health)
    byteBuf.writeInt(about.foodLevel)
    // Add player coordinates
    byteBuf.writeDouble(about.location.x)
    byteBuf.writeDouble(about.location.y)
    byteBuf.writeDouble(about.location.z)

    // Convert to a byte array to include in the packet
    val byteArray = ByteArray(byteBuf.readableBytes())
    byteBuf.readBytes(byteArray)

    val channelId = ResourceLocation("datatransfer:playerinfo")
    val packet = WrapperPlayServerPluginMessage(channelId, byteArray)
    PacketEvents.getAPI().playerManager.sendPacket(to, packet)
    byteBuf.release()
}
```

### Step 2: Handling on the Mod Side

On the mod side, you need to update the handling of the incoming `datatransfer:playerinfo` packet in `PlayerInfoPayload.java`:

```java
public record PlayerInfoPayload(String comment, String playerName, double health, int foodLevel, double x, double y, double z) implements CustomPayload {

    public static final Id<PlayerInfoPayload> ID = new Id<>(Identifier.of("datatransfer:playerinfo"));

    public static final PacketCodec<PacketByteBuf, PlayerInfoPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, PlayerInfoPayload::comment,
            PacketCodecs.STRING, PlayerInfoPayload::playerName,
            PacketCodecs.DOUBLE, PlayerInfoPayload::health,
            PacketCodecs.INTEGER, PlayerInfoPayload::foodLevel,
            // Add coordinate handling
            PacketCodecs.DOUBLE, PlayerInfoPayload::x,
            PacketCodecs.DOUBLE, PlayerInfoPayload::y,
            PacketCodecs.DOUBLE, PlayerInfoPayload::z,
            PlayerInfoPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    // Also update the record
    public record PlayerData(String comment, double health, int foodLevel, double x, double y, double z) {}
}
```

Then update the handling in `NetworkHandler.java`:

```java
ClientPlayNetworking.registerGlobalReceiver(PlayerInfoPayload.ID, (payload, context) ->
        playerDataCache.put(payload.playerName(), new PlayerInfoPayload.PlayerData(
                payload.comment(), payload.health(), payload.foodLevel(), payload.x(), payload.y(), payload.z())));
```

### Step 3: Displaying New Data

Update `TextDisplayManager.java` as desired to display the new data:

```java
String finalString;
PlayerInfoPayload.PlayerData data = NetworkHandler.playerDataCache.get(focused);
if (data != null) {
    finalString = String.format("§6Name: §e%s\n§6Health: §e%.1f\n§6Hunger: §e%d\n§6Coordinates: §e(%.1f, %.1f, %.1f)\n\n§6Comment: §e%s",
            focused, data.health(), data.foodLevel(), data.x(), data.y(), data.z(), data.comment());
} else {
    String value = NetworkHandler.isHandshakeSuccessful() && !NetworkHandler.isRequestsBlocked() ? "Waiting..." : "null";
    finalString = String.format("§6Name: §e%s\n§6Health: §e%s\n§6Hunger: §e%s\n§6Coordinates: §e%s",
            focused, value, value, value);
}
```

### Important

- The order of writing data on the server must match the order of reading on the client.
- Use the `DataUtils` methods for serialization/deserialization of data, as there is no default support for types like String.