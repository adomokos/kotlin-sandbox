package web

import arrow.Kind
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.fix
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
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

// Ideas, code from here:
// https://medium.com/siili-automotive/how-to-build-rest-api-with-arrow-ktor-and-requery-in-kotlin-e5438c951418

data class Employee(
    val id: Long,
    val firstName: String,
    val lastName: String
)

interface EmployeeRepository<F> {
    fun findAll(): Kind<F, List<Employee>>
}

class IOEmployeeRepository : EmployeeRepository<ForIO> {
    override fun findAll(): IO<List<Employee>> = IO {
        findEmployees()
    }
}

interface Suspendable<F> {
    suspend fun <A : Any> Kind<F, A>.suspended(): A
}

fun IO.Companion.suspendable(): Suspendable<ForIO> = object : Suspendable<ForIO> {
    override suspend fun <A : Any> Kind<ForIO, A>.suspended() =
        this.fix().suspended()
}

class EmployeeRouting<F>(
    private val repository: EmployeeRepository<F>,
    suspendable: Suspendable<F>
) : Suspendable<F> by suspendable {

    fun Routing.employees(path: String) {
        get(path) {
            call.respond(
                repository
                    .findAll()
                    .suspended()
            )
        }
    }
}

fun main(args: Array<String>) {
    val employeeRouting =
        EmployeeRouting(IOEmployeeRepository(), IO.suspendable())

    val server = embeddedServer(Netty, port = 8080) {
        configureJsonSerialization()
        install(CallLogging) // Log requests

        install(Routing) {
            with(employeeRouting) { employees("/employees") }
        }

        routing {
            get("/") {
                call.respondText("Hello World!", ContentType.Text.Plain)
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
