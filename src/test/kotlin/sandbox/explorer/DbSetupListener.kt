package sandbox.explorer

import io.kotlintest.TestCase
import io.kotlintest.extensions.TestListener
import java.io.File
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

object DbSetupListener : TestListener {
    override fun beforeTest(testCase: TestCase) {
        val filePath = File("db/explorer-db.sqlt").getAbsolutePath()
        Database.connect("jdbc:sqlite:$filePath", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }
}
