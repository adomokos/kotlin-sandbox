package sandbox.arrow

import arrow.core.Either
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.fx
import arrow.core.fix
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.fx.rx2.ObservableK
// import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

typealias Maybe<T> = Option<T>
typealias Just<T> = Some<T>

data class ECountry(val code: String)
data class EAddress(val id: Int, val country: Maybe<ECountry>)
data class EPerson(val id: Int, val name: String, val address: Maybe<EAddress>)

// Errors modeled as algebraic data types
// expressed in Kotlin as a sealed hierarchy
sealed class BizError {
    data class PersonNotFound(val personId: Int) : BizError()
    data class AddressNotFound(val personId: Int) : BizError()
    data class CountryNotFound(val addressId: Int) : BizError()
}

typealias PersonNotFound = BizError.PersonNotFound
typealias AddressNotFound = BizError.AddressNotFound
typealias CountryNotFound = BizError.CountryNotFound

// This can lead to callback hell
fun getCountryCodeCH(maybePerson: Either<BizError, EPerson>): Either<BizError, String> =
    maybePerson.flatMap { person ->
        person.address.toEither { AddressNotFound(person.id) }.flatMap { address ->
            address.country.fold({ CountryNotFound(address.id).left() },
                { it.code.right() })
        }
    }

// Use monad comprehension
fun getCountryCode(maybePerson: Either<BizError, EPerson>): Either<BizError, String> =
    Either.fx<BizError, String> {
        val (person) = maybePerson
        val (address) = person.address.toEither { AddressNotFound(person.id) }
        val (country) = address.country.toEither { CountryNotFound(address.id) }
        country.code
    }.fix()

val personDB: Map<Int, EPerson> = mapOf(
    1 to EPerson(
        id = 1,
        name = "Alfredo Lambda",
        address = Just(
            EAddress(
                id = 1,
                country = Just(
                    ECountry(
                        code = "ES"
                    )
                )
            )
        )
    )
)

val addressDB: Map<Int, EAddress> = mapOf(
    1 to EAddress(
        id = 1,
        country = Just(
            ECountry(
                code = "ES"
            )
        )
    )
)

fun findPerson(personId: Int): ObservableK<Either<BizError, EPerson>> =
    ObservableK.just(
        Maybe.fromNullable(personDB.get(personId)).toEither { PersonNotFound(personId) }
    ) // mock implementation for simplicity

fun findCountry(addressId: Int): ObservableK<Either<BizError, ECountry>> =
    ObservableK.just(
        Maybe.fromNullable(addressDB.get(addressId))
            .flatMap { it.country }
            .toEither { CountryNotFound(addressId) }
    ) // mock implementation for simplicity

class EitherTSpec : StringSpec({
    "can validate with EitherT Monad Transformer".config(enabled = false) {
    }
})
