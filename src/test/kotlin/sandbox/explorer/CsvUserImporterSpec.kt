package sandbox.explorer

import arrow.core.Either
import arrow.core.fix
import arrow.core.value
import arrow.fx.extensions.io.applicative.map
import arrow.fx.fix
import arrow.mtl.value
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class CsvUserImporterSpec : StringSpec() {
    override fun listeners(): List<TestListener> = listOf(DbSetupListener)

    init {
        "reads the GitHub users' info from a csv file" {
            transaction {
                addLogger(StdOutSqlLogger)

                val people: Either<AppError, List<Person>> = runBlocking { CsvUserImporter.importUsers() }.unsafeRunSync()

                // println(Person.all().forEach { println(it) })

                people.map { it.size shouldBe 3 }
                Person.count() shouldBe 3

                rollback()
            }
        }

        "reads the GitHub users' info from a csv file with mtl" {
            transaction {
                addLogger(StdOutSqlLogger)

                val people: Either<AppError, List<Person>> =
                        runBlocking { CsvUserImporter.importUsers2().value().fix() }.unsafeRunSync()

                people.map { it.size shouldBe 3 }

                Person.count() shouldBe 3
                // println(Person.all().forEach { println(it) })

                rollback()
            }
        }

        "reads the GitHub users' info from a csv file with Reader" {
            transaction {
                addLogger(StdOutSqlLogger)

                val appContext = GetAppContext(csvPath = "resources/users.csv")
                val people: Either<AppError, List<Person>> =
                        runBlocking {
                            val output = CsvUserImporter.importUsers3().run(appContext).value()

                            output.value().fix()
                        }.unsafeRunSync()

                people.map { it.size shouldBe 3 }

                Person.count() shouldBe 3
                // println(Person.all().forEach { println(it) })

                rollback()
            }
        }

        "reads the GitHub users' info from a csv file with ReaderT" {
            transaction {
                addLogger(StdOutSqlLogger)

                val appContext = GetAppContext(csvPath = "resources/users.csv")
                val people =
                        runBlocking {
                            val output = CsvUserImporter.importUsers4().run(appContext)
                            output.map {
                                it.run(appContext)
                            }
                        }.unsafeRunSync().fix().value()

                people.size shouldBe 3

                Person.count() shouldBe 3
                // println(Person.all().forEach { println(it) })

                rollback()
            }
        }

        "reads the GitHub users' info from a csv file with ReaderT properly" {
            transaction {
                addLogger(StdOutSqlLogger)

                val appContext = GetAppContext(csvPath = "resources/users.csv")
                val app = CsvUserImporter.importUsers5().run(appContext)

                val result = app.value().fix().unsafeRunSync()

                result.map { people ->
                    people.size shouldBe 3
                }

                Person.count() shouldBe 3

                rollback()
            }
        }
    }
}
