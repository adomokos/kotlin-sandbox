package sandbox.explorer

import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import java.io.File
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

object DbSetupListener : TestListener {
    override suspend fun beforeTest(testCase: TestCase) {
        val filePath = File("db/explorer-db.sqlt").getAbsolutePath()
        val db = Database.connect("jdbc:sqlite:$filePath", "org.sqlite.JDBC")
        db.useNestedTransactions = true
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }
}
