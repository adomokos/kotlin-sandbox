package sandbox.explorer

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime

object People : IntIdTable("people") {
    val email = varchar("email", length = 255)
    val firstName = varchar("firstname", length = 255)
    val lastName = varchar("lastname", length = 255)
    var rating = integer("rating")
    val gitHubUsername = varchar("git_hub_username", length = 255)
}

object GitHubMetrics : IntIdTable("git_hub_metrics") {
    val login = varchar("login", length = 255)
    val name = varchar("name", length = 255)
    val publicGistsCount = integer("public_gists_count")
    val publicReposCount = integer("public_repos_count")
    val followersCount = integer("followers_count")
    val followingCount = integer("following_count")
    val accountCreatedAt = registerColumn<DateTime>("account_created_at", MyDateColumnType(true))
    val person = reference("person_id", People)
}

class Person(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Person>(People) {
        @JvmStatic
        fun findOrCreate(
            emailValue: String,
            firstNameValue: String,
            lastNameValue: String,
            ratingValue: Int,
            gitHubUsernameValue: String
        ): Person =
                Person.find {
                    People.email eq emailValue and (People.gitHubUsername eq gitHubUsernameValue)
                }.singleOrNull()
                        ?: Person.new {
                            email = emailValue
                            firstName = firstNameValue
                            lastName = lastNameValue
                            rating = ratingValue
                            gitHubUsername = gitHubUsernameValue
                        }
    }

    var email by People.email
    var firstName by People.firstName
    var lastName by People.lastName
    var rating by People.rating
    var gitHubUsername by People.gitHubUsername

    override fun toString(): String {
        return "Person{ $email, $firstName, $lastName, $rating, $gitHubUsername }"
    }
}

class GitHubMetric(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GitHubMetric>(GitHubMetrics)

    var login by GitHubMetrics.login
    var name by GitHubMetrics.name
    var publicGistsCount by GitHubMetrics.publicGistsCount
    var publicReposCount by GitHubMetrics.publicReposCount
    var followersCount by GitHubMetrics.followersCount
    var followingCount by GitHubMetrics.followingCount
    var accountCreatedAt by GitHubMetrics.accountCreatedAt
    var person by Person referencedOn(GitHubMetrics.person)
}
