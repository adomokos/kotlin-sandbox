package sandbox.explorer

import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class EntitiesSpec : StringSpec() {
    override fun listeners(): List<TestListener> = listOf(DbSetupListener)

    private fun addPerson() =
        People.insert {
            it[email] = "john@example.com"
            it[firstName] = "John"
            it[lastName] = "Smith"
            it[rating] = 1
            it[gitHubUsername] = "jdsmith"
        } // get People.id

    init {
        "can insert and query People records" {
            transaction {
                addLogger(StdOutSqlLogger)

                val person = addPerson()
                val personId = person get People.id
                personId.value shouldBe 1

                var retrievedPerson = Person.findById(personId)
                retrievedPerson!!.id shouldBe personId

                rollback()
            }
        }

        "can insert and query GitHubMetrics records" {
            transaction {
                addLogger(StdOutSqlLogger)

                val aPerson = Person.findOrCreate(
                    emailValue = "john@example.com",
                    firstNameValue = "John",
                    lastNameValue = "Smith",
                    ratingValue = 1,
                    gitHubUsernameValue = "jdsmith"
                )

                GitHubMetric.new {
                    login = "jdsmith"
                    name = "John Smith"
                    publicGistsCount = 24
                    publicReposCount = 15
                    followersCount = 81
                    followingCount = 19
                    accountCreatedAt = DateTime(2020, 1, 1, 12, 0, 0, 0)
                    person = aPerson
                }

                val ghCount = GitHubMetric.count()
                ghCount shouldBe 1

                rollback()
            }
        }
    }
}
