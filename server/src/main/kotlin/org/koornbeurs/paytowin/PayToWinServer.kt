package org.koornbeurs.paytowin

import PasswordValidationInterceptor
import com.paytowin.grpc.PayToWinGrpcKt
import com.paytowin.grpc.Paytowin
import com.paytowin.grpc.Paytowin.EffectRequest
import com.paytowin.grpc.Paytowin.EffectRequest.EffectCase
import com.paytowin.grpc.Paytowin.EffectResponse
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.atomic.AtomicReference


class PayToWinServer(private val port: Int, private val bukkitServer: PayToWin) {
    internal class PayToWinService(
        private val playerChannels: AtomicReference<ArrayList<Channel<Collection<String>>>>,
        private val bukkitServer: PayToWin,
        private val keepInventoryPlayers: HashSet<UUID>
    ) :
        PayToWinGrpcKt.PayToWinCoroutineImplBase() {
        override fun getPlayers(request: Paytowin.PlayersRequest): Flow<Paytowin.PlayersResponse> {

            val channel = Channel<Collection<String>>(20);

            playerChannels.get().add(channel)

            runBlocking {
                channel.send(Bukkit.getOnlinePlayers().filterNotNull().map { player: Player -> player.name })
            }

            return flow {
                for (players in channel) {
                    emit(Paytowin.PlayersResponse.newBuilder().addAllPlayers(players).build())
                }
            };
        }

        override suspend fun applyEffect(request: Paytowin.EffectRequest): Paytowin.EffectResponse {
            val bukkitPlayer =
                Bukkit.getPlayer(request.player) ?: return EffectResponse.newBuilder().setSuccess(false).build()

            val effectApplier = EffectApplier(bukkitPlayer, bukkitServer)

            when (request.effectCase) {
                Paytowin.EffectRequest.EffectCase.TOOL -> effectApplier.tool(request.tool)
                Paytowin.EffectRequest.EffectCase.POTION -> effectApplier.potion(request.potion)
                Paytowin.EffectRequest.EffectCase.SPAWNENTITY -> {
                    Bukkit.getScheduler().runTask(bukkitServer, Runnable {
                        for (i in 0..request.spawnEntity.amount) {
                            if (request.spawnEntity.entity == Paytowin.MinecraftEntityWrapper.MinecraftEntity.LIGHTNING) {
                                bukkitPlayer.world.strikeLightning(bukkitPlayer.location)
                            } else if (request.spawnEntity.entity == Paytowin.MinecraftEntityWrapper.MinecraftEntity.ENDER_DRAGON) {
                                val dragon = bukkitPlayer.world.spawnEntity(
                                    bukkitPlayer.location,
                                    EntityType.ENDER_DRAGON
                                ) as EnderDragon
                                dragon.phase = EnderDragon.Phase.CIRCLING
                            } else if (request.spawnEntity.entity == Paytowin.MinecraftEntityWrapper.MinecraftEntity.PRIMED_TNT) {
                                val tnt = bukkitPlayer.world.spawnEntity(
                                    bukkitPlayer.location,
                                    EntityType.PRIMED_TNT
                                ) as TNTPrimed
                                tnt.fuseTicks = 10
                            } else {
                                EntityType.fromName(request.spawnEntity.entity.toString())
                                    ?.let { bukkitPlayer.world.spawnEntity(bukkitPlayer.location, it) }
                            }
                        }
                    })
                }

                Paytowin.EffectRequest.EffectCase.DATALESS -> {
                    when (request.dataless) {
                        Paytowin.DatalessEffect.RandomPotions -> {
                            RandomPotionApplier(bukkitServer, 40, 10, bukkitPlayer).runTaskTimer(bukkitServer, 0L, 20L)
                        }

                        Paytowin.DatalessEffect.Spinny -> {
                            SpinnyApplier(bukkitServer, 30, bukkitPlayer).runTaskTimer(bukkitServer, 0L, 20L)
                        }

                        Paytowin.DatalessEffect.PutInAdventure -> {
                            Bukkit.getScheduler().runTask(bukkitServer, Runnable {
                                bukkitPlayer.gameMode = org.bukkit.GameMode.ADVENTURE
                                Bukkit.getScheduler().runTaskLater(bukkitServer, Runnable {
                                    bukkitPlayer.gameMode = org.bukkit.GameMode.SURVIVAL
                                }, 20 * 5)
                            })
                        }

                        Paytowin.DatalessEffect.EnableKeepinventory -> {
                            val playerUUID: UUID = bukkitPlayer.getUniqueId()
                            keepInventoryPlayers.add(playerUUID)

                            object : BukkitRunnable() {
                                override fun run() {
                                    // This will run after 5 minutes and remove the player from the set
                                    keepInventoryPlayers.remove(playerUUID)
                                }
                            }.runTaskLater(this.bukkitServer, (20 * 60 * 5).toLong())
                        }

                        Paytowin.DatalessEffect.KillAllEnderDragons -> {
                            Bukkit.getScheduler().runTask(bukkitServer, Runnable {
                                bukkitPlayer.world.entities.filter { it.type == EntityType.ENDER_DRAGON }
                                    .forEach { it.remove() }
                            })
                        }

                        Paytowin.DatalessEffect.LootBox -> {
                            Bukkit.getScheduler().runTask(bukkitServer, Runnable {
                                val item = ItemStack(Material.DIRT)
                                val meta = item.itemMeta
                                meta.displayName(Component.text("Loot Box"))
                                meta.persistentDataContainer.set(bukkitServer.lootBoxKey, PersistentDataType.BYTE, 1)
                                item.setItemMeta(meta)
                                println(meta)
                                bukkitPlayer.inventory.addItem(item)
                            })
                        }

                        else -> {
                            return EffectResponse.newBuilder().setSuccess(false)
                                .build()
                        }
                    }
                }

                Paytowin.EffectRequest.EffectCase.ITEM -> {
                    effectApplier.item(request.item.itemName, request.item.amount)
                }

                else -> return EffectResponse.newBuilder().setSuccess(false).build()
            }

            // You might need to adjust the response handling according to your requirement

            Bukkit.broadcast(Component.text("${request.requester} has applied effect ${getPretty(request)} to ${request.player}"))
            return EffectResponse.newBuilder().setSuccess(true).build()
        }
    }

    private val playerChannels = AtomicReference(ArrayList<Channel<Collection<String>>>());

    private val keepInventoryPlayers = HashSet<UUID>()

    val server: Server = ServerBuilder
        .forPort(port)
        .addService(PayToWinService(playerChannels, bukkitServer, keepInventoryPlayers))
        .addService(ProtoReflectionService.newInstance())
        .intercept(PasswordValidationInterceptor())
        .build()

    fun start(): Thread {
        val thread = Thread(Runnable {
            server.start()
            println("Server started, listening on $port")
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    println("*** shutting down gRPC server since JVM is shutting down")
                    this@PayToWinServer.stop()
                    println("*** server shut down")
                },
            )
        })
        thread.start()
        return thread
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    suspend fun sendUpdatedUserList() {
        val players = Bukkit.getOnlinePlayers().filterNotNull().map { player: Player -> player.name }
        this.playerChannels.get().forEach { channel ->
            run {
                channel.send(players)
            }
        }
    }

    fun playerDead(event: PlayerDeathEvent) {
        val player = event.entity
        if (keepInventoryPlayers.contains(player.uniqueId)) {
            event.keepInventory = true
            event.drops.clear() // Optional: clears items that would have been dropped
            event.droppedExp = 0 // Optional: prevents experience from dropping
        }
    }
}

fun getPretty(request: EffectRequest): String {
    when (request.effectCase) {
        EffectCase.TOOL -> return "Tool: ${request.tool.name}"
        EffectCase.POTION -> return "Potion: ${request.potion.name} for ${request.potion.duration} at level ${request.potion.amplifier}"
        EffectCase.SPAWNENTITY -> return "Mob: ${request.spawnEntity.entity}x${request.spawnEntity.amount}"
        EffectCase.DATALESS -> when (request.dataless) {
            Paytowin.DatalessEffect.RandomPotions -> return "Random Potions"
            Paytowin.DatalessEffect.Spinny -> return "Spinny"
            Paytowin.DatalessEffect.PutInAdventure -> return "Put in Adventure"
            Paytowin.DatalessEffect.EnableKeepinventory -> return "Enable Keepinventory"
            Paytowin.DatalessEffect.KillAllEnderDragons -> return "Kill All Ender Dragons"
            else -> return "Unknown"
        }


        EffectCase.ITEM -> return "Item: ${request.item.itemName}x${request.item.amount}"
        else -> return "Unknown"
    }
}