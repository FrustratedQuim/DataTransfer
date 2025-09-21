package com.ratger.datatransfer

import com.github.retrooper.packetevents.PacketEvents
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class DataTransfer : JavaPlugin(), Listener {

    // Игроки с нужным пермишеном + модом
    val trackedPlayers = mutableSetOf<org.bukkit.entity.Player>()

    override fun onEnable() {
        server.pluginManager.registerEvents(ChannelListener(this), this)

        PacketEvents.getAPI().load()
        PacketEvents.getAPI().init()
        PacketEvents.getAPI().eventManager.registerListener(PacketHandler(this))

        // Каналы для обмена данными
        server.messenger.registerIncomingPluginChannel(this, "datatransfer:handshake") { _, _, _ -> }
        server.messenger.registerIncomingPluginChannel(this, "datatransfer:playerinfo_request") { _, _, _ -> }
        server.messenger.registerOutgoingPluginChannel(this, "datatransfer:playerinfo")
    }
}
