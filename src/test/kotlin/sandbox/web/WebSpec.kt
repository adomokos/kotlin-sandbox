package sandbox.web

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.core.fix
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import java.nio.charset.StandardCharsets.UTF_8

class WebSpec : StringSpec() {
    class MockEmployeeRepository(private val list: List<Employee>) : EmployeeRepository<ForId> {
        override fun findAll(): Id<List<Employee>> = Id.just(list)
    }

    private fun Id.Companion.suspendable(): Suspendable<ForId> = object : Suspendable<ForId> {
        override suspend fun <A : Any> Kind<ForId, A>.suspended(): A {
            val id = this.fix()
            return id.extract()
        }
    }

    private val employees = listOf(Employee(1, "John", "Smith"))
    private val routingForUnitTests =
        EmployeeRouting(MockEmployeeRepository(employees), Id.suspendable())

    fun Application.testApp() {
        install(ContentNegotiation) {
            jackson {}
        }

        install(Routing) {
            with(routingForUnitTests) { employees("/test/employees") }
        }
    }

    init {
        "can stub out data access in Ktor test" {
            withTestApplication({ testApp() }) {
                with(handleRequest(HttpMethod.Get, "/test/employees")) {
                    response.status() shouldBe HttpStatusCode.OK
                    response.contentType() shouldBe ContentType.Application.Json.withCharset(UTF_8)
                    response.content shouldBe
                        """[{"id":1,"firstName":"John","lastName":"Smith"}]"""
                }
            }
        }
    }
}
