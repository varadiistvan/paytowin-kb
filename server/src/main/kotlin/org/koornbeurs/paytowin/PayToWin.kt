package org.koornbeurs.paytowin

import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.coroutines.runBlocking
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.random.Random


private val invertedPlayers = mutableMapOf<Player, BukkitRunnable>()
private val recentlyProcessedPlayers = Collections.synchronizedSet(HashSet<UUID>())

val bucket1 = listOf(Material.DIAMOND to 1, Material.IRON_INGOT to 3)
val bucket2 = listOf(Material.GOLD_INGOT to 2, Material.COAL to 5)
val bucket3 = listOf(Material.EMERALD to 1, Material.REDSTONE to 10)

class PayToWin : JavaPlugin(), Listener {

    private var threadHandle: Thread? = null
    private val server = PayToWinServer(50051, this)
    lateinit var lootBoxKey: NamespacedKey

    override fun onEnable() {
        threadHandle = server.start()
        println(1234)
        lootBoxKey = NamespacedKey(this, "lootBox")
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
        print("player attempted to join");
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
    fun onPlayerDeath(event: PlayerDeathEvent) {
        server.playerDead(event)
    }

    // Disable block destruction for explosions
    @EventHandler
    fun EntityExplodeEvent(event: EntityExplodeEvent) {
        event.blockList().clear()
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.DIRT) {
            val meta = CustomBlockData(block, this)
            if (meta.has(this.lootBoxKey, PersistentDataType.BYTE)) {
                event.isCancelled = true // Prevent the block from dropping dirt
                println("Haha, lootbox")
                val bucket = chooseBucket()
                println(bucket)
                val item = bucket.random()
                event.player.inventory.addItem(ItemStack(item.first, item.second))
                block.type = Material.AIR // Remove the block
            }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val itemInHand = event.itemInHand
        val blockPlaced = event.blockPlaced

        // Check if the item in hand has the loot box data
        itemInHand.itemMeta?.persistentDataContainer?.get(this.lootBoxKey, PersistentDataType.BYTE)?.let {
            // Transfer the data to the block's PersistentDataContainer if it exists
            if (it == 1.toByte()) {
                val customBlockData = CustomBlockData(blockPlaced, this)
                customBlockData.set(this.lootBoxKey, PersistentDataType.BYTE, 1)
                println(blockPlaced)
            }
        }
    }


    private fun chooseBucket(): List<Pair<Material, Int>> {
        val chance = Random.nextDouble()
        return when {
            chance < 0.50 -> bucket1
            chance < 0.80 -> bucket2
            else -> bucket3
        }
    }
}
