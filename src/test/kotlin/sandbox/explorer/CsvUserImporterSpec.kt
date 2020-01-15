package sandbox.explorer

import arrow.fx.fix
import arrow.mtl.value
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class CsvUserImporterSpec : StringSpec() {
    override fun listeners(): List<TestListener> = listOf(DbSetupListener)

    init {
        "reads the GitHub users' info from a csv file" {
            transaction {
                addLogger(StdOutSqlLogger)

                transaction {
                    addLogger(StdOutSqlLogger)

                    val people =
                        CsvUserImporter.importUsers.value().fix().unsafeRunSync()

                    // println(Person.all().forEach { println(it) })

                    people.map { it.size shouldBe 3 }
                    Person.count() shouldBe 3

                    rollback()
                }

                Person.count() shouldBe 0
            }
        }
    }
}