package org.koornbeurs.paytowin

import PasswordValidationInterceptor
import com.paytowin.grpc.PayToWinGrpcKt
import com.paytowin.grpc.Paytowin
import com.paytowin.grpc.Paytowin.EffectResponse
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicReference

class PayToWinServer(private val port: Int, private val bukkitServer: PayToWin) {
    internal class PayToWinService(
        private val playerChannels: AtomicReference<ArrayList<Channel<Collection<String>>>>,
        private val bukkitServer: PayToWin
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
                Paytowin.EffectRequest.EffectCase.DATALESS -> {
                    when (request.dataless) {
                        Paytowin.DatalessEffect.RandomPotions -> {
                            RandomPotionApplier(bukkitServer, 40, 10, bukkitPlayer).runTaskTimer(bukkitServer, 0L, 20L)
                        }

                        else -> return EffectResponse.newBuilder().setSuccess(false)
                            .build()
                    }
                }

                else -> return EffectResponse.newBuilder().setSuccess(false).build()
            }

            // You might need to adjust the response handling according to your requirement

            return EffectResponse.newBuilder().setSuccess(true).build()
        }
    }

    private val playerChannels = AtomicReference(ArrayList<Channel<Collection<String>>>());


    val server: Server = ServerBuilder
        .forPort(port)
        .addService(PayToWinService(playerChannels, bukkitServer))
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
}