package sandbox.explorer.logic

import arrow.core.flatMap
import arrow.fx.fix
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import sandbox.explorer.DbSetupListener
import sandbox.explorer.Factories
import sandbox.explorer.GitHubUserInfo
import sandbox.explorer.Person

class GitHubMetricConverterSpec : StringSpec() {
    override fun listeners(): List<TestListener> = listOf(DbSetupListener)

    init {
        "converts the JSON data to GitHubMetric and persists it in the database" {
            val userInfoData: String = File("./resources/github-user-info.json").readText(Charsets.UTF_8)

            val gitHubUserInfo = GitHubUserInfo.deserializeFromJson2(userInfoData).value().fix().unsafeRunSync()

            transaction {
                addLogger(StdOutSqlLogger)

                val aPerson: Person = Factories.addPerson()

                aPerson.id.value shouldBe 1

                val result = gitHubUserInfo.flatMap { gitHubUserInfoValue ->
                    GitHubMetricConverter.convertAndSaveData(gitHubUserInfoValue, aPerson).value().fix().unsafeRunSync()
                }

                result.shouldBeRight()

                rollback()
            }
        }
    }
}
