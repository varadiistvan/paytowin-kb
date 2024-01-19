package org.koornbeurs.paytowin

import com.google.protobuf.Empty
import com.paytowin.grpc.PayToWinGrpcKt
import com.paytowin.grpc.Paytowin
import io.grpc.*
import java.util.concurrent.atomic.AtomicReference

class PayToWinServer(private val port: Int) {
    var logged_in: AtomicReference<String> = AtomicReference("")

    internal class PayToWinService(private val loggedInRef: AtomicReference<String>) : PayToWinGrpcKt.PayToWinCoroutineImplBase() {
        override suspend fun login(request: Paytowin.LoginRequest): Empty {
            println(loggedInRef.get())
            return super.login(request)
        }
    }

    internal class IpAddressInterceptor(private val loggedInRef: AtomicReference<String>) : ServerInterceptor {
        override fun <ReqT : Any?, RespT : Any?> interceptCall(
            call: ServerCall<ReqT, RespT>?,
            headers: Metadata?,
            next: ServerCallHandler<ReqT, RespT>?
        ): ServerCall.Listener<ReqT>? {
            val clientIp = call?.attributes?.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)?.toString()
            clientIp?.let {
                loggedInRef.set(it)
            }
            println("Client IP: $clientIp")
            return next?.startCall(call, headers)
        }
    }

    val server: Server = ServerBuilder
        .forPort(port)
        .addService(PayToWinService(logged_in))
        .intercept(IpAddressInterceptor(logged_in))
        .build()

    fun start(): Thread {
        val thread = Thread(Runnable {server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@PayToWinServer.stop()
                println("*** server shut down")
            },
        )})
        thread.start()
        return thread
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}