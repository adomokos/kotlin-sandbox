package sandbox.explorer.logic

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import com.opencsv.CSVReaderHeaderAware
import java.io.FileReader
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import sandbox.explorer.AppError
import sandbox.explorer.Person

object CsvUserImporter {
    fun readUserData(fileName: String): IO<Either<AppError, List<Array<String>>>> =
        IO.fx {
            val csvReader = CSVReaderHeaderAware(FileReader(fileName))
            val records: List<Array<String>> = csvReader.readAll()

            Right(records)
        }.handleError { Left(AppError.CsvImportError) }

    private fun persistUserInfo(userData: List<Array<String>>): IO<Either<AppError, List<Person>>> =
        IO.fx {
            val result = transaction {
                addLogger(StdOutSqlLogger)
                userData.map {
                    Person.findOrCreate(
                        emailValue = it[0],
                        firstNameValue = it[1],
                        lastNameValue = it[2],
                        ratingValue = it[3].toInt(),
                        gitHubUsernameValue = it[4]
                    )
                }
            }
            Right(result)
        }.handleError { err -> Left(
            AppError.PersonInsertError(
                err.message ?: "No message"
            )
        ) }

    val importUsers: IO<Either<AppError, List<Person>>> =
        IO.fx {
            val eitherUserData = !readUserData("resources/users.csv")
            val result2 = when (eitherUserData) {
                is Either.Left -> eitherUserData
                is Either.Right -> !persistUserInfo(eitherUserData.b)
            }
            result2
        }
}
