/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package sandbox.explorer

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import arrow.mtl.value
import java.io.File
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import sandbox.explorer.logic.CsvUserImporter
import sandbox.explorer.logic.PeopleProcessor

fun main(args: Array<String>) = App.run(false).value().fix().unsafeRunSync()

object App {
    fun connectToDatabase(): Database {
        val filePath = File("db/explorer-db.sqlt").getAbsolutePath()
        val db = Database.connect("jdbc:sqlite:$filePath", "org.sqlite.JDBC")
        db.useNestedTransactions = true
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        return db
    }

    fun run(parallel: Boolean) =
        EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
            val db = App.connectToDatabase()

            val people = ! CsvUserImporter.importUsers

            val parallelRunner =
                EitherT(PeopleProcessor
                    .processPeopleParallel(people)
                    .map { item ->
                        item
                            .traverse(Either.applicative()) { it }
                            .fix()
                            .map { it.fix().toList() }
                    })

            val runner =
                PeopleProcessor.processPeople(people).fix()

            if (parallel) {
                ! parallelRunner
            } else {
                ! runner
            }
        }
}
