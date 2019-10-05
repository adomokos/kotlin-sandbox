package exposed

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object People : Table("people") {
    val id = integer("id").autoIncrement().primaryKey()
    val email = varchar("email", length = 255)
    val firstName = varchar("firstname", length = 255).nullable()
    val lastName = varchar("lastname", length = 255).nullable()
}

// Dao
// class Person(id: EntityID<Int>) : Entity<Int>(id) {
    // companion object : EntityClass<Int, Person>(People)

    // var email by People.email
    // var firstName by People.firstName
    // var lastName by People.lastName
// }

// Dto
data class PersonDto(
    val id: Int?,
    val email: String,
    val firstName: String?,
    val lastName: String?
)

fun mapToPersonDto(it: ResultRow) = PersonDto(
    id = it[People.id],
    email = it[People.email],
    firstName = it[People.firstName],
    lastName = it[People.lastName]
)

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

fun findPerson(personId: Int): PersonDto? {
    return People.select { People.id eq personId }
        .map { mapToPersonDto(it) } // returns a standard Kotlin List
        .firstOrNull()
}

fun insertPerson(person: PersonDto): Int {
    return People.insert {
        it[email] = person.email
        it[firstName] = person.firstName
        it[lastName] = person.lastName
    } get People.id
}

fun runExamples() {
    val filePath = File("db/explorer-db.sqlt").getAbsolutePath()
    Database.connect("jdbc:sqlite:${filePath}", "org.sqlite.JDBC")

    val newPerson = PersonDto(
        id = null,
        email = "john@example.com",
        firstName = "John",
        lastName = "Smith"
    )

    transaction {
        addLogger(StdOutSqlLogger)

        selectAllPeople()
        println("Number of People: ${numberOfPeople()}")

        println("Found person with id 1: ${findPerson(1)}")

        val personId = insertPerson(newPerson)
        println("Insert a person, new ID is: ${personId}")

        rollback()
    }
}
