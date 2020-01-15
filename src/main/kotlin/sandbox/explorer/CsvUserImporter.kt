package sandbox.explorer

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import com.opencsv.CSVReaderHeaderAware
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileReader

typealias EitherIO<A> = EitherT<ForIO, AppError, A>

object CsvUserImporter {
    val importUsers =
        EitherIO.monad<ForIO, AppError>(IO.monad()).fx.monad {
            val csvReader = CSVReaderHeaderAware(FileReader("resources/users.csv"))
            val records: List<Array<String>> = csvReader.readAll()

            transaction {
                // addLogger(StdOutSqlLogger)

                records.map {
                    Person.findOrCreate(
                        emailValue = it[0],
                        firstNameValue = it[1],
                        lastNameValue = it[2],
                        ratingValue = it[3].toInt(),
                        gitHubUsernameValue = it[4]
                    )
                }
            }
        }
}
