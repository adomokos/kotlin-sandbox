package sandbox.explorer.logic

import arrow.core.Left
import arrow.fx.fix
import io.kotlintest.assertions.arrow.either.shouldNotBeLeft
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import sandbox.explorer.AppError
import sandbox.explorer.DbSetupListener
import sandbox.explorer.Person

class CsvUserImporterSpec : StringSpec() {
    override fun listeners(): List<TestListener> = listOf(DbSetupListener)

    init {
        "returns Left when the file path is not found" {
            val result =
                CsvUserImporter.readUserData("somefile.csv").value().fix().unsafeRunSync()

            result shouldBe Left(AppError.CsvImportError)
        }

        "reads the GitHub users' info from a csv file, persist them in DB" {
            transaction {
                addLogger(StdOutSqlLogger)

                transaction {
                    addLogger(StdOutSqlLogger)

                    val people =
                        CsvUserImporter.importUsers.value().fix().unsafeRunSync()

                    // println(Person.all().forEach { println(it) })
                    people.shouldNotBeLeft()

                    people.map { it.size shouldBe 3 }
                    Person.count() shouldBe 3

                    rollback()
                }

                Person.count() shouldBe 0
            }
        }
    }
}