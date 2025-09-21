package com.ratger.datatransfer

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.resources.ResourceLocation
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage
import io.netty.buffer.Unpooled
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class PacketHandler(private val plugin: DataTransfer) : PacketListenerAbstract() {
    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.PLUGIN_MESSAGE) return

        val packet = WrapperPlayClientPluginMessage(event)
        val channelId = packet.channelName.toString()
        val player = event.getPlayer() as? Player ?: return

        when (channelId) {
            "datatransfer:handshake" -> { // Первый запрос от игрока с модом для подключения к плагину
                val byteBuf = Unpooled.wrappedBuffer(packet.data)
                val data = DataUtils.readString(byteBuf).trim()
                if (data == "DataTransferHandshake") {
                    plugin.trackedPlayers.add(player)
                    player.sendMessage(Component.text("С подключением, Сеньор!", NamedTextColor.GREEN))
                }
                byteBuf.release()
            }

            "datatransfer:playerinfo_request" -> { // Запрос данных о конкретном игроке при помощи мода
                if (!plugin.trackedPlayers.contains(player)) return

                val byteBuf = Unpooled.wrappedBuffer(packet.data)
                val targetName = DataUtils.readString(byteBuf).trim()
                val target = plugin.server.onlinePlayers.firstOrNull { it.name.equals(targetName, true) }
                if (target != null) sendPlayerData(player, target)
                byteBuf.release()
            }
        }
    }

    private fun sendPlayerData(to: Player, about: Player) {
        if (!plugin.trackedPlayers.contains(to)) return

        val byteBuf = Unpooled.buffer()
        DataUtils.writeString(byteBuf, "Custom packet from DataTransfer")
        DataUtils.writeString(byteBuf, about.name)
        byteBuf.writeDouble(about.health)
        byteBuf.writeInt(about.foodLevel)

        // Переводим в массив байтов, дабы запихнуть в пакет
        val byteArray = ByteArray(byteBuf.readableBytes())
        byteBuf.readBytes(byteArray)

        val channelId = ResourceLocation("datatransfer:playerinfo")
        val packet = WrapperPlayServerPluginMessage(channelId, byteArray)
        PacketEvents.getAPI().playerManager.sendPacket(to, packet)
        byteBuf.release()
    }
}