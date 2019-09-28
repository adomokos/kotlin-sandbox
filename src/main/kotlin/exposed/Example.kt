package exposed

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object People : IntIdTable() {
	// val id = integer("id").primaryKey()
	val email = varchar("email", length = 255)
	val firstName = varchar("firstname", length = 255).nullable()
	val lastName = varchar("lastname", length = 255).nullable()
	val createdAt = datetime("created_at")
}

object GithubInfos : IntIdTable() {
	val peopleId = integer("people_id")
	val login = varchar("login", length=255).uniqueIndex()
	val name = varchar("name", length=255)
	val accountCreatedAt = datetime("account_created_at")
}

fun selectAllPeople() {
	People.selectAll().forEach {
		println(it)
	}
}

fun numberOfPeople(): Int {
	return People.selectAll().count()
}

fun findPerson(personId: Int): ResultRow? {
	return People.select { People.id eq personId }.singleOrNull()
}

fun runExamples() {
	val filePath = File("db/explorer-db.sqlt").getAbsolutePath()
	Database.connect("jdbc:sqlite:${filePath}", "org.sqlite.JDBC")

	transaction {
		addLogger(StdOutSqlLogger)

		selectAllPeople()
		println("Number of People: ${numberOfPeople()}")

		println("Found person with id 1: ${findPerson(1)}")
	}
}
