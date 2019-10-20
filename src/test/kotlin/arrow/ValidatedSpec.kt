package arrow

import arrow.core.Either
import arrow.core.Invalid
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Valid
import arrow.core.Validated
import arrow.core.flatMap
import arrow.core.invalid
import arrow.core.left
import arrow.core.right
import arrow.core.valid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

data class ConnectionParams(val url: String, val port: Int)

sealed class ConfigError {
    data class MissingConfig(val field: String) : ConfigError()
    data class ParseConfig(val field: String) : ConfigError()
}

abstract class Read<A> {
    abstract fun read(s: String): Option<A>

    companion object {
        val stringRead: Read<String> =
            object : Read<String>() {
                override fun read(s: String): Option<String> = Option(s)
            }

        val intRead: Read<Int> =
            object : Read<Int>() {
                override fun read(s: String): Option<Int> =
                    if (s.matches(Regex("-?[0-9]+"))) {
                        Option(s.toInt())
                    } else {
                        None
                    }
            }
    }
}

data class Config(val map: Map<String, String>) {
    fun <A> parse(read: Read<A>, key: String): Validated<ConfigError, A> {
        val v = Option.fromNullable(map[key])
        return when (v) {
            is Some -> {
                val s = read.read(v.t)
                when (s) {
                    is Some -> s.t.valid()
                    is None -> ConfigError.ParseConfig(key).invalid()
                }
            }
            is None -> Validated.Invalid(ConfigError.MissingConfig(key))
        }
    }
}

fun <E, A, B, C> parallelValidate(
    v1: Validated<E, A>,
    v2: Validated<E, B>,
    f: (A, B) -> C
): Validated<NonEmptyList<E>, C> {
    return when {
        v1 is Validated.Valid && v2 is Validated.Valid
            -> Validated.Valid(f(v1.a, v2.a))
        v1 is Validated.Valid && v2 is Validated.Invalid
            -> v2.toValidatedNel()
        v1 is Validated.Invalid && v2 is Validated.Valid
            -> v1.toValidatedNel()
        v1 is Validated.Invalid && v2 is Validated.Invalid
            -> Validated.Invalid(NonEmptyList(v1.e, listOf(v2.e)))
        else -> throw IllegalStateException("Not possible value")
    }
}

fun validateUrlAndPort(config: Config) =
    parallelValidate(
        config.parse(Read.stringRead, "url"),
        config.parse(Read.intRead, "port")
    ) { url, port ->
        ConnectionParams(url, port)
    }

fun positive(field: String, i: Int): Either<ConfigError, Int> {
    return if (i >= 0) i.right()
    else ConfigError.ParseConfig(field).left()
}

class ValidatedSpec : StringSpec({
    "can validate multiple fields with correct input" {
        val config = Config(mapOf("url" to "127.0.0.1", "port" to "1337"))

        val valid = validateUrlAndPort(config)

        val expectedCP = ConnectionParams(
            url = "127.0.0.1",
            port = 1337
        )
        valid shouldBe Valid(expectedCP)
    }

    "can validate multiple fields with incorrect input" {
        val config = Config(mapOf("url" to "127.0.0.1", "port" to "NaN"))

        val result = validateUrlAndPort(config)
        val expectedError = ConfigError.ParseConfig(field = "port")

        result shouldBe Invalid(NonEmptyList(expectedError))
    }

    "can use Either for simpler validation" {
        val config = Config(mapOf("house_number" to "-42"))
        val houseNumber = config.parse(Read.intRead, "house_number").withEither {
            either -> either.flatMap { positive("house_number", it) }
        }

        houseNumber shouldBe Invalid(ConfigError.ParseConfig(field = "house_number"))
    }
})
