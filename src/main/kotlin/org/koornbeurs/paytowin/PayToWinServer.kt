package org.koornbeurs.paytowin

import com.paytowin.grpc.PayToWinGrpcKt
import com.paytowin.grpc.Paytowin
import com.paytowin.grpc.Paytowin.EffectResponse
import io.github.cdimascio.dotenv.dotenv
import io.grpc.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.atomic.AtomicReference

class PayToWinServer(private val port: Int, private val bukkitServer: Plugin) {
    internal class PayToWinService(
        private val playerChannels: AtomicReference<ArrayList<Channel<Collection<String>>>>,
        private val bukkitServer: Plugin
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
            val player = request.player

            val bukkitPlayer = Bukkit.getPlayer(player)
            // Schedule the task to run on the main thread
            Bukkit.getScheduler().runTask(bukkitServer, Runnable {
                bukkitPlayer?.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 20 * 10, 10))
            })

            // You might need to adjust the response handling according to your requirement

            return EffectResponse.newBuilder().setSuccess(bukkitPlayer != null).build()
        }
    }

    private val playerChannels = AtomicReference(ArrayList<Channel<Collection<String>>>());

    internal class PasswordValidationInterceptor() : ServerInterceptor {
        override fun <ReqT : Any?, RespT : Any?> interceptCall(
            call: ServerCall<ReqT, RespT>?,
            headers: io.grpc.Metadata?,
            next: ServerCallHandler<ReqT, RespT>?
        ): ServerCall.Listener<ReqT>? {
            val clientIp = call?.attributes?.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)?.toString()

            val password = headers?.get(io.grpc.Metadata.Key.of("password", io.grpc.Metadata.ASCII_STRING_MARSHALLER))

            if (password == null || password != dotenv()["GRPC_PWD"]) {
                call?.close(Status.PERMISSION_DENIED, io.grpc.Metadata());
            }

            return next?.startCall(call, headers)
        }
    }

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