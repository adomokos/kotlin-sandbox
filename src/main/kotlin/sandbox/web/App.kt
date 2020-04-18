package sandbox.web

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import sandbox.explorer.App

data class Employee(
    val id: Long,
    val firstName: String,
    val lastName: String
)

fun run(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        App.connectToDatabase()

        configureJsonSerialization()

        install(CallLogging) // Log requests

        routing {
            get("/") {
                call.respondText("Hello World!", ContentType.Text.Plain)
            }
            get("/employees") {
                call.respond(findEmployees())
            }
        }
    }
    server.start(wait = true)
}

private fun Application.configureJsonSerialization() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
}

private fun findEmployees(): List<Employee> =
    listOf(
        Employee(1, "John", "Smith"),
        Employee(2, "Paul", "Brown")
    )
