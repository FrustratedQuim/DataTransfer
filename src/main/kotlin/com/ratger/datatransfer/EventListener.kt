package com.ratger.datatransfer

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class EventListener(
    val networkHandler: NetworkHandler,
    val sqliteManager: SQLiteManager
) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        networkHandler.trackedPlayers.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        sqliteManager.addPlayer(event.player.uniqueId)
    }
}