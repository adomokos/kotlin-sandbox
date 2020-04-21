package grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import sandbox.arrow.datatypes.playWithOption
import grpc.examples.helloworld.GreeterGrpcKt
import grpc.examples.helloworld.HelloReply
import grpc.examples.helloworld.HelloRequest

class HelloWorldServer constructor(
    private val port: Int
) {
    val server: Server = ServerBuilder
        .forPort(port)
        .addService(HelloWorldService())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@HelloWorldServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class HelloWorldService : GreeterGrpcKt.GreeterCoroutineImplBase() {
        override suspend fun sayHello(request: HelloRequest) = HelloReply
            .newBuilder()
            .setMessage("Hello ${request.name}")
            .build()
    }
}

suspend fun main() {
    val port = 50051
    val server = HelloWorldServer(port)
    server.start()
    server.blockUntilShutdown()
}

//suspend fun main(args: Array<String>) {
//    playWithOption()
//}
