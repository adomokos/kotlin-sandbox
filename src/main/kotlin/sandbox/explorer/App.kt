/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package sandbox.explorer

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.fx.handleError
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.value
import java.io.File
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import sandbox.explorer.logic.CsvUserImporter
import sandbox.explorer.logic.PeopleProcessor

enum class RunMode {
    NORMAL, PARALLEL
}

fun main(args: Array<String>) =
    IO.fx {
        val runMode: RunMode = if (args.any() && args.first() == "parallel") {
            RunMode.PARALLEL
        } else {
            RunMode.NORMAL
        }

        val result = ! App.run(runMode).value().fix()

        when (result) {
            is Either.Left -> println("Error occurred - ${result.a}")
            is Either.Right -> println("Success!! - ${result.b}")
        }
    }
    .handleError { err -> println("::: Fatal error occurred: ${err.message} ") }
    .unsafeRunSync()

object App {
    fun connectToDatabase(): Database {
        val filePath = File("db/explorer-db.sqlt").getAbsolutePath()
        val db = Database.connect("jdbc:sqlite:$filePath", "org.sqlite.JDBC")
        db.useNestedTransactions = true
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        return db
    }

    fun run(runMode: RunMode) =
        EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
            App.connectToDatabase()

            val people = ! CsvUserImporter.importUsers

            val result = ! if (runMode == RunMode.PARALLEL) {
                println("::: Running in parallel :::")
                PeopleProcessor.processPeopleParallel(people)
            } else {
                println("::: Running normal :::")
                PeopleProcessor.processPeople(people)
            }
            result
        }
}
