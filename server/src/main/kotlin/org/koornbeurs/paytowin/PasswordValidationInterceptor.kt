import io.github.cdimascio.dotenv.dotenv
import io.grpc.*

class PasswordValidationInterceptor() : ServerInterceptor {
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