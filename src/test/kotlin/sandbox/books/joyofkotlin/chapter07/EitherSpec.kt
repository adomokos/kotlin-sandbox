package sandbox.books.joyofkotlin.chapter07

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.Serializable
import java.lang.IllegalStateException
import java.lang.NullPointerException
import sandbox.books.joyofkotlin.chapter05.List as LList

sealed class Either<out A, out B> {
    abstract fun <C> map(f: (B) -> C): Either<A, C>
    abstract fun <C> flatMap(f: (B) -> Either<@UnsafeVariance A, C>): Either<A, C>
    abstract fun getOrElse(defaultValue: () -> @UnsafeVariance B): B
    abstract fun orElse(defaultValue: () -> Either<@UnsafeVariance A, @UnsafeVariance B>): Either<A, B>

    internal data class Left<out A, out B>(val value: A) : Either<A, B>() {
        override fun toString(): String = "Left($value)"
        override fun <C> map(f: (B) -> C): Either<A, C> = Left(value)
        override fun <C> flatMap(f: (B) -> Either<@UnsafeVariance A, C>): Either<A, C> =
            Left(value)
        override fun getOrElse(defaultValue: () -> @UnsafeVariance B): B = defaultValue()
        override fun orElse(defaultValue: () -> Either<@UnsafeVariance A, @UnsafeVariance B>) = defaultValue()
    }

    internal data class Right<out A, out B>(private val value: B) : Either<A, B>() {
        override fun toString(): String = "Right($value)"
        override fun <C> map(f: (B) -> C): Either<A, C> = Right(f(value))
        override fun <C> flatMap(f: (B) -> Either<@UnsafeVariance A, C>): Either<A, C> = f(value)
        override fun getOrElse(defaultValue: () -> @UnsafeVariance B): B = value
        override fun orElse(defaultValue: () -> Either<@UnsafeVariance A, @UnsafeVariance B>) = this
    }

    companion object {
        fun <A, B> left(value: A): Either<A, B> = Left(value)
        fun <A, B> right(value: B): Either<A, B> = Right(value)
    }
}

sealed class Result<out A> : Serializable {
    abstract fun <B> map(f: (A) -> B): Result<B>
    abstract fun <B> flatMap(f: (A) -> Result<B>): Result<B>
    abstract fun mapFailure(message: String): Result<A>

    internal data class Failure<out A>(internal val exception: RuntimeException) : Result<A>() {
        override fun toString(): String = "Failure(${exception.message})"
        override fun <B> map(f: (A) -> B): Result<B> = Failure(exception)
        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = Failure(exception)
        override fun mapFailure(message: String): Result<A> =
            Failure(java.lang.RuntimeException(message, exception))
    }

    internal data class Success<out A>(internal val value: A) : Result<A>() {
        override fun toString(): String = "Success($value)"
        override fun <B> map(f: (A) -> B): Result<B> =
            try {
                Success(f(value))
            } catch (e: RuntimeException) {
                Failure(e)
            } catch (e: Exception) {
                Failure(RuntimeException(e))
            }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> =
            try {
                f(value)
            } catch (e: RuntimeException) {
                Failure(e)
            } catch (e: Exception) {
                Failure(RuntimeException(e))
            }
        override fun mapFailure(message: String): Result<A> = this
    }

    fun getOrElse(defaultValue: @UnsafeVariance A): A =
        when (this) {
            is Success -> this.value
            else -> defaultValue
        }

    fun getOrElse(defaultValue: () -> @UnsafeVariance A): A =
        when (this) {
            is Success -> this.value
            else -> defaultValue()
        }

    fun orElse(defaultValue: () -> Result<@UnsafeVariance A>): Result<A> =
        when (this) {
            is Success -> this
            else -> try {
                defaultValue()
            } catch (e: RuntimeException) {
                Result.failure<A>(e)
            } catch (e: Exception) {
                Result.failure<A>(RuntimeException(e))
            }
        }

    internal object Empty : Result<Nothing>() {
        override fun <B> map(f: (Nothing) -> B): Result<B> = Empty
        override fun <B> flatMap(f: (Nothing) -> Result<B>): Result<B> = Empty
        override fun toString(): String = "Empty"
        override fun mapFailure(message: String): Result<Nothing> = this
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Result<A> =
            when (a) {
                null -> Failure(NullPointerException())
                else -> Success(a)
            }
        operator fun <A> invoke(): Result<A> = Empty

        fun <A> failure(message: String): Result<A> = Failure(IllegalStateException(message))
        fun <A> failure(exception: RuntimeException): Result<A> = Failure(exception)
        fun <A> failure(exception: Exception): Result<A> = Failure(IllegalStateException(exception))
    }
}

class EitherSpec : StringSpec() {
    fun <A : Comparable<A>> max(list: LList<A>): Either<String, A> =
        when (list) {
            is LList.Nil -> Either.left("max called on an empty list")
            is LList.Cons -> Either.right(list.foldLeft(list.head) { x ->
                { y -> if (x > y) x else y }
            })
        }

    fun <K, V> Map<K, V>.getResult(key: K) =
        when {
            this.containsKey(key) -> Result(this[key])
            else -> Result.failure("Key `$key` not found in map")
        }

    fun <K, V> Map<K, V>.getResultWithDefault(key: K) =
        when {
            this.containsKey(key) -> Result(this[key])
            else -> Result.Empty
        }

    data class Toon private constructor(
        val firstName: String,
        val lastName: String,
        val email: Result<String>
    ) {

        companion object {
            operator fun invoke(firstName: String, lastName: String) =
                Toon(firstName, lastName, Result.failure("$firstName $lastName has no email"))

            operator fun invoke(firstName: String, lastName: String, email: String) =
                Toon(firstName, lastName, Result(email))
        }
    }

    data class Toon2 private constructor(
        val firstName: String,
        val lastName: String,
        val email: Result<String>
    ) {

        companion object {
            operator fun invoke(firstName: String, lastName: String) =
                Toon2(firstName, lastName, Result.Empty)

            operator fun invoke(firstName: String, lastName: String, email: String) =
                Toon2(firstName, lastName, Result(email))
        }
    }

    init {
        "can work with custom Either type" {
            val list = LList(3, 5, 8, 4, 1)

            max(list) shouldBe Either.right<String, Int>(8)
        }

        "can work with map over its fields" {
            val leftValue = Either.left<String, Int>("error")
            leftValue.map { x: Int -> x * 2 } shouldBe Either.left<String, Int>("error")

            val rightValue = Either.right<String, Int>(2)
            rightValue.map { x: Int -> x * 3 } shouldBe Either.right<String, Int>(6)
        }

        "can work with flatMap" {
            val leftValue = Either.left<String, Int>("error")
            val doubleFn = { x: Int -> Either.right<String, Int>(x * 2) }
            leftValue.flatMap { x: Int -> doubleFn(x) } shouldBe Either.left<String, Int>("error")

            val rightValue = Either.right<String, Int>(2)
            rightValue.flatMap { x: Int -> doubleFn(x) } shouldBe Either.right<String, Int>(4)
        }

        "can extract value with getOrElse" {
            val leftValue = Either.left<String, Int>("error")
            leftValue.getOrElse { 8 } shouldBe 8

            val rightValue = Either.right<String, Int>(2)
            rightValue.getOrElse { 8 } shouldBe 2
        }

        "computation can return a Result object" {
            val result = Result(5)
            result.map { it * 3 } shouldBe Result(15)

            val failure = Result.failure<Int>("error")
            failure.map { it * 3 }.toString() shouldBe "Failure(error)"
        }

        "returns the defaultValue if failure, value otherwise" {
            val failure = Result.failure<Int>("error")
            failure.getOrElse(4) shouldBe 4

            val result = Result(5)
            result.getOrElse(3) shouldBe 5
        }

        "uses Result in toons" {
            val toons: Map<String, Toon> = mapOf(
                "Mickey" to Toon("Mickey", "Mouse", "mickey@disney.com"),
                "Minnie" to Toon("Minnie", "Mouse"),
                "Donald" to Toon("Donald", "Duck", "donald@disney.com")
            )

            val toon = toons.getResult("Mickey").flatMap { it.email }
            toon shouldBe Result("mickey@disney.com")

            val noEmailToon = toons.getResult("Minnie").flatMap { it.email }
            noEmailToon.toString() shouldBe "Failure(Minnie Mouse has no email)"
        }

        "uses Empty in toons when no email was found" {
            val toons: Map<String, Toon2> = mapOf(
                "Mickey" to Toon2("Mickey", "Mouse", "mickey@disney.com"),
                "Minnie" to Toon2("Minnie", "Mouse"),
                "Donald" to Toon2("Donald", "Duck", "donald@disney.com")
            )

            val toon = toons.getResultWithDefault("Mickey").flatMap { it.email }
            toon shouldBe Result("mickey@disney.com")

            val emptyEmail = toons.getResultWithDefault("Minnie").flatMap { it.email }
            emptyEmail shouldBe Result.Empty
        }

        "map failures into a new one with mapFailure" {
            val failure = Result.failure<Int>("Not good")

            failure.mapFailure("switched to this").toString() shouldBe "Failure(switched to this)"

            val success = Result(3)
            success.mapFailure("hello").toString() shouldBe "Success(3)"
        }
    }
}
