package com.ratger.datatransfer

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent

class ChannelListener(private val plugin: DataTransfer) : Listener {
    @EventHandler
    fun onPlayerRegisterChannel(event: PlayerRegisterChannelEvent) {
        if (event.channel == "datatransfer:playerinfo") {
            // Пока-что работаем лишь с одним модом и соответственно пермишеном.
            // В будущем как-нибудь расширим.
            if (event.player.hasPermission("datatransfer.infotags")) {
                plugin.trackedPlayers.add(event.player)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.trackedPlayers.remove(event.player)
    }
}
