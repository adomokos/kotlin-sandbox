package sandbox.explorer

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.monad.monad
import arrow.fx.handleError
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.Reader
import arrow.mtl.ReaderApi
import arrow.mtl.ReaderT
import arrow.mtl.extensions.eithert.applicative.applicative
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.extensions.kleisli.monad.monad
import arrow.mtl.extensions.monadError
import arrow.mtl.fix
import arrow.mtl.map
import com.opencsv.CSVReaderHeaderAware
import java.io.FileReader

// https://jorgecastillo.dev/kotlin-fp-2-monad-transformers

typealias EitherIO<A, B> = EitherT<ForIO, A, B>
typealias EitherIOP<E> = EitherTPartialOf<ForIO, E>
typealias RIO<E, B> = ReaderT<EitherIOP<E>, GetAppContext, B>

data class GetAppContext(
    val csvPath: String
)

object CsvUserImporter {
    suspend fun importUsers(): IO<Either<AppError, List<Person>>> =
            IO.fx {
                val csvReader = CSVReaderHeaderAware(FileReader("resources/users.csv"))
                val records: List<Array<String>> = csvReader.readAll()

                Right(records.map {
                    Person.findOrCreate(
                            emailValue = it[0],
                            firstNameValue = it[1],
                            lastNameValue = it[2],
                            ratingValue = it[3].toInt(),
                            gitHubUsernameValue = it[4]
                    )
                })
            }.handleError { Left(AppError.CsvImportError) }

    suspend fun importUsers3(): Reader<GetAppContext, EitherIO<AppError, List<Person>>> =
            ReaderApi.ask<GetAppContext>().map { ctx ->
                EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
                    val csvReader = CSVReaderHeaderAware(FileReader(ctx.csvPath))
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
            }

    suspend fun importUsers4() =
            ReaderT.monad<ForIO, GetAppContext>(IO.monad()).fx.monad {
                ReaderApi.ask<GetAppContext>().map { ctx ->
                    val csvReader = CSVReaderHeaderAware(FileReader(ctx.csvPath))
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
                }
            }.fix()

    object RIOApi {
        fun monadError() = EitherT.monadError<ForIO, AppError>(IO.monad())

        fun <E> raiseError(e: E): RIO<E, Nothing> =
                RIO.raiseError(EitherT.monadError<ForIO, E>(IO.monad()), e)

        fun <A> just(a: A): RIO<Nothing, A> =
                RIO.just(EitherT.applicative<ForIO, Nothing>(IO.applicative()), a)

        fun ask() = ReaderT.ask<EitherTPartialOf<ForIO, AppError>, GetAppContext>(monadError())
    }

    fun importUsers5(): RIO<AppError, List<Person>> =
            RIOApi.ask().flatMap(RIOApi.monadError()) { ctx ->
                val csvReader = CSVReaderHeaderAware(FileReader(ctx.csvPath))
                val records: List<Array<String>> = csvReader.readAll()

                RIOApi.just(
                    records.map {
                        Person.findOrCreate(
                                emailValue = it[0],
                                firstNameValue = it[1],
                                lastNameValue = it[2],
                                ratingValue = it[3].toInt(),
                                gitHubUsernameValue = it[4]
                        )
                    })
            }
}
