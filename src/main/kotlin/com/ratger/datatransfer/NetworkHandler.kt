/*
 * Можете изменять передаваемые данные как угодно, главное придерживаться формата из InfoTags.
 * Актуальный формат: "param1;param2;param3;..."
 */

package com.ratger.datatransfer

import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage
import io.netty.buffer.Unpooled
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class NetworkHandler(
    val plugin: DataTransfer,
    val sqLiteManager: SQLiteManager
) : PacketListenerAbstract() {

    val trackedPlayers = mutableSetOf<UUID>()

    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.PLUGIN_MESSAGE) return

        val packet = WrapperPlayClientPluginMessage(event)
        if (packet.channelName != "datatransfer:main") return

        val player = event.getPlayer() as Player
        val byteBuf = Unpooled.wrappedBuffer(packet.data)
        val content = DataUtils.readString(byteBuf)

        if (content == "FirstRequest") {
            val hasPerm = player.hasPermission("datatransfer.infotags")
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                if (hasPerm) {
                    trackedPlayers.add(player.uniqueId)
                    sendPacket(player, "success")
                } else {
                    sendPacket(player, "no_permission")
                }
            }, 20L)
        } else if (trackedPlayers.contains(player.uniqueId) && content.startsWith("getData")) {

            val requestedPlayer = Bukkit.getPlayer(content.substringAfter(";")) ?: return
            val magicCode = sqLiteManager.getCode(requestedPlayer.uniqueId) ?: return
            val health = requestedPlayer.health
            val food = requestedPlayer.foodLevel

            val finalContent = "giveData;${requestedPlayer.name};$health;$food;$magicCode"
            sendPacket(player, finalContent)
        }
    }

    fun sendPacket(player: Player, content: String) {
        val buf = Unpooled.buffer()
        DataUtils.writeString(buf, content)

        val finalBytes = ByteArray(buf.readableBytes())
        buf.readBytes(finalBytes)

        player.sendPluginMessage(plugin, "datatransfer:main", finalBytes)
    }
}