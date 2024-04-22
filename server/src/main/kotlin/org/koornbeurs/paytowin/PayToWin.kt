package org.koornbeurs.paytowin

import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
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

val bucket1 = listOf(
    ItemStack(Material.OAK_LOG, 32),
    ItemStack(Material.TORCH, 32),
    ItemStack(Material.IRON_NUGGET, 32),
    ItemStack(Material.SWEET_BERRIES, 16),
    ItemStack(Material.COW_SPAWN_EGG, 1),
    ItemStack(Material.SHEEP_SPAWN_EGG, 1)
) to "Tier 1"

val bucket2 = listOf(
    ItemStack(Material.GOLD_INGOT, 4),
    ItemStack(Material.COAL, 16),
    ItemStack(Material.IRON_INGOT, 12),
    ItemStack(Material.BAKED_POTATO, 8),
    ItemStack(Material.POTATO, 4),
    ItemStack(Material.REDSTONE, 16),
    ItemStack(Material.MOOSHROOM_SPAWN_EGG, 1),
) to "Tier 2"

val bucket3 = listOf(
    ItemStack(Material.DIAMOND, 4),
    ItemStack(Material.EMERALD, 16),
    ItemStack(Material.NETHERITE_INGOT, 1),
    ItemStack(Material.GOLDEN_CARROT, 12),
    ItemStack(Material.ENDER_PEARL, 8),
    run {
        val item = ItemStack(Material.STICK, 1)
        val meta = item.itemMeta
        meta.addEnchant(Enchantment.KNOCKBACK, 10, true)
        meta.displayName(Component.text("Bye-bye Stick"))
        item.setItemMeta(meta)
        item
    },
    ItemStack(Material.MOOSHROOM_SPAWN_EGG, 1),
    ItemStack(Material.VILLAGER_SPAWN_EGG, 1)
) to "Tier 3"

class PayToWin : JavaPlugin(), Listener {

    private var threadHandle: Thread? = null
    private val server = PayToWinServer(50051, this)
    lateinit var lootBoxKey: NamespacedKey
    lateinit var cantExplode: NamespacedKey

    override fun onEnable() {
        threadHandle = server.start()
        println(1234)
        lootBoxKey = NamespacedKey(this, "lootBox")
        cantExplode = NamespacedKey(this, "cantExplode")
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
        if (event.entity.persistentDataContainer.has(cantExplode)) {
            event.blockList().clear()
        }
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
                val item = bucket.first.random()
                println(item)
                event.player.inventory.addItem(item)
                event.player.sendMessage("Congratulations, you got a ${bucket.second} Loot Box")
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


    private fun chooseBucket(): Pair<List<ItemStack>, String> {
        val chance = Random.nextDouble()
        return when {
            chance < 0.50 -> bucket1
            chance < 0.80 -> bucket2
            else -> bucket3
        }
    }
}
