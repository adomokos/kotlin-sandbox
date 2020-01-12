package sandbox.arrow

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import com.opencsv.CSVReaderHeaderAware
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.FileReader
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import sandbox.explorer.AppError
import sandbox.explorer.Person

typealias EitherIO<A, B> = EitherT<ForIO, A, B>

class EitherTSpec : StringSpec() {
    private fun importUsers(): EitherIO<AppError, List<Person>> =
        EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
            val csvReader = CSVReaderHeaderAware(FileReader("resources/users.csv"))
            val records: List<Array<String>> = csvReader.readAll()

            records.map {
                Person.findOrCreate(
                    emailValue = it[0],
                    firstNameValue = it[1],
                    lastNameValue = it[2],
                    ratingValue = it[3].toInt(),
                    gitHubUsernameValue = it[4]
                )
            }
        }.fix()

    init {
        "can validate with EitherT Monad Transformer".config(enabled = true) {
            transaction {
                addLogger(StdOutSqlLogger)

                val people: Either<AppError, List<Person>> =
                    runBlocking { importUsers().value().fix() }.unsafeRunSync()

                people.map { it.size shouldBe 3 }

                Person.count() shouldBe 3
                // println(Person.all().forEach { println(it) })

                rollback()
            }
        }
    }
}
