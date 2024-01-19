package org.koornbeurs.paytowin

import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable


class PayToWin : JavaPlugin(), Listener {

    private var threadHandle: Thread? = null;
    private val server = PayToWinServer(50051, this)
    override fun onEnable() {
        threadHandle = server.start()
        println(1234)
        getServer().pluginManager.registerEvents(this, this)
        // server.blockUntilShutdown()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // server.stop()

        threadHandle?.interrupt()
        server.stop()
    }

    @EventHandler
    fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
        runBlocking {
            server.sendUpdatedUserList()
        }
    }

    @EventHandler
    fun onPlayerQuit(playerQuitEvent: PlayerQuitEvent) {
        object : BukkitRunnable() {
            override fun run() {
                // Code to execute after the player has fully left
                runBlocking {
                    server.sendUpdatedUserList()
                }
            }
        }.runTaskLater(this, 1L)
    }

}
