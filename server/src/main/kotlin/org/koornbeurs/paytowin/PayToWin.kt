package org.koornbeurs.paytowin

import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

private val invertedPlayers = mutableMapOf<Player, BukkitRunnable>()
private val recentlyProcessedPlayers = Collections.synchronizedSet(HashSet<UUID>())

class PayToWin : JavaPlugin(), Listener {

    private var threadHandle: Thread? = null
    private val server = PayToWinServer(50051, this)

    override fun onEnable() {
        threadHandle = server.start()
        println(1234)
        getServer().pluginManager.registerEvents(this, this)
        // server.blockUntilShutdown()
    }

    override fun onDisable() {
        // Plugin shutdown logic
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
                runBlocking {
                    server.sendUpdatedUserList()
                }
            }
        }.runTaskLater(this, 1L)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        if (player in invertedPlayers) {

            println("hahheh")

            // Invert the pitch and yaw
            val newPitch = -player.location.pitch
            val newYaw = -player.location.yaw

            // Apply the new pitch and yaw to the player's location
            player.location.pitch = newPitch
            player.location.yaw = newYaw

        }
    }
}
