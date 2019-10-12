package arrow

import arrow.core.*
import arrow.core.extensions.fx
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

data class RawUser(
    val fullName: String,
    val email: String,
    val phone: String,
    val streetAddress: String,
    val city: String,
    val zipCode: String
)

fun generateRawUsers() = listOf(
    RawUser(
        fullName = "Roth Drake",
        email = "vestibulum.nec@eratEtiam.net",
        phone = "1-230-665-4456",
        streetAddress = "P.O. Box 980, 4942 Mattis. St.",
        city = "Gellik",
        zipCode = "10691"
    ),
    RawUser(
        fullName = "Andrew",
        email = "and@skidCiam.com",
        phone = "1-721-480-0788",
        streetAddress = "P.O. Box 701, 2269 Orci. Road",
        city = "Limelette",
        zipCode = "22598"
    ),
    RawUser(
        fullName = "Kevin Kaufman",
        email = "sem@necorciDonec.ca",
        phone = "1-609-284-0788",
        streetAddress = "Ap #840-3698 Ipsom. Ave",
        city = "Fircarolo",
        zipCode = "25265"
    )
)

data class DomainUser(
    val person: Person,
    val phoneNumber: PhoneNumber
)

data class Person(
    val firstName: String,
    val lastName: String
)

data class PhoneNumber(
    val countryCode: Int,
    val areaCode: Int,
    val prefix: Int,
    val lineNumber: Int
)

// Transformation functions
fun personFrom(name: String): Either<Exception, Person> {
    val names = name.split(" ")
    if (names.size == 2) {
        val firstName = names[0]
        val lastName = names[1]

        return Right(Person(firstName, lastName))
    }

    return Left(Exception("Can't extract first and last names from $name"))
}

val incorrectPattern = """(\d)-(\d{3})-(\d{3})-(.{4})""".toRegex()

fun phoneNumberFrom(phone: String): Either<Exception, PhoneNumber> {
    val matched = incorrectPattern.matchEntire(phone)
    matched?.let {
        val values = it.groupValues.toList().takeLast(4)
        return Either.fx {
            val (countryCode) = values[0].safeToInt()
            val (areaCode) = values[1].safeToInt()
            val (prefix) = values[2].safeToInt()
            val (lineNumber) = values[3].safeToInt()

            PhoneNumber(countryCode, areaCode, prefix, lineNumber)
        }
    }


    return Left(Exception("$phone is not the accepted format!"))
}

fun String.safeToInt(): Either<Exception, Int> =
    if (this.matches(Regex("-?[0-9]+"))) Either.Right(this.toInt())
    else Either.Left(NumberFormatException("$this is not a number"))

fun domainUserFrom(rawUser: RawUser): Either<Exception, DomainUser> {
    return Either.fx {
        val (maybePerson) = personFrom(rawUser.fullName)
        val (maybePhoneNumber) = phoneNumberFrom(rawUser.phone)
        DomainUser(maybePerson, maybePhoneNumber)
    }
}

fun runTransformation() =
    generateRawUsers()
        .map(::domainUserFrom)

// Tests

class MonadRealWorldSpec : StringSpec({
    "validates a User conversion with monad binding chains" {
        val result = runTransformation()

        result.size shouldBe 3

        val firstUser = result[0]
        val expectedUser = 
            DomainUser(
                person=Person(firstName="Roth",
                              lastName="Drake"),
                phoneNumber=PhoneNumber(countryCode=1,
                                        areaCode=230,
                                        prefix=665,
                                        lineNumber=4456))
        firstUser shouldBe Right(expectedUser)

        // TODO: user arrow matcher here
        // val secondUser = result[1]

        // when (secondUser) {
            // is Either.Left -> assertTrue(true)
            // is Either.Right -> fail("2nd user wasn't a Left<Exception>")
        // }
    }
})
