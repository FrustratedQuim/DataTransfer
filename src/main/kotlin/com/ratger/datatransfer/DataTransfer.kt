package com.ratger.datatransfer

import com.github.retrooper.packetevents.PacketEvents
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class DataTransfer : JavaPlugin(), Listener {

    private val sqliteManager = SQLiteManager(this)
    private val networkHandler = NetworkHandler(this, sqliteManager)

    override fun onEnable() {
        server.pluginManager.registerEvents(EventListener(networkHandler, sqliteManager), this)
        server.messenger.registerIncomingPluginChannel(this, "datatransfer:main") { _, _, _ -> }
        server.messenger.registerOutgoingPluginChannel(this, "datatransfer:main")

        PacketEvents.getAPI().eventManager.registerListener(networkHandler)
    }

    override fun onDisable() {
        sqliteManager.close()
        PacketEvents.getAPI().eventManager.unregisterListeners(networkHandler)
    }
}
