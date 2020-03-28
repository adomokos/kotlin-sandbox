package sandbox.books.joyofkotlin.chapter06

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.math.pow

// Optional data is computational context for safely handling optional data
sealed class Option<out A> {
    abstract fun isEmpty(): Boolean
    abstract fun <B> map(f: (A) -> B): Option<B>
    abstract fun <B> flatMap(f: (A) -> Option<B>): Option<B>

    internal class None<out A> : Option<A>() {
        override fun isEmpty() = true
        override fun <B> map(f: (A) -> B): Option<B> = None()
        override fun <B> flatMap(f: (A) -> Option<B>): Option<B> = None()
        override fun toString(): String = "None"
        override fun hashCode(): Int = 0
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }
    }

    internal data class Some<out A>(internal val value: A) : Option<A>() {
        override fun isEmpty() = false
        override fun <B> map(f: (A) -> B): Option<B> = Some(f(value))
        override fun <B> flatMap(f: (A) -> Option<B>): Option<B> = f(value)
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Option<A> =
            when (a) {
                null -> None()
                else -> Some(a)
            }
    }

    fun getOrElse(default: @UnsafeVariance A): A =
        when (this) {
            is None -> default
            is Some -> value
        }

    fun getOrElseWithFn(default: () -> @UnsafeVariance A): A =
        when (this) {
            is None -> default()
            is Some -> value
        }
}

data class Toon(
    val firstName: String,
    val lastName: String,
    val email: Option<String> = Option.None()
) {
    companion object {
        operator fun invoke(
            firstName: String,
            lastName: String,
            email: String? = null
        ) =
            Toon(firstName, lastName, Option(email))
    }
}

fun <K, V> Map<K, V>.getOption(key: K) =
    Option(this[key])

fun mean(list: List<Double>): Option<Double> =
    when {
        list.isEmpty() -> Option()
        else -> Option(list.sum() / list.size)
    }

fun variance(list: List<Double>): Option<Double> =
    mean(list).flatMap { m ->
        mean(list.map { x ->
            (x - m).pow(2.0)
        })
    }

class OptionalDataSpec : StringSpec() {
    init {
        "can create Option value" {
            val none = Option(null)
            none shouldBe Option.None()

            val some = Option(2)
            some shouldBe Option.Some(2)
        }

        "can get the value or return default for None" {
            val some = Option(2)
            some.getOrElse(3) shouldBe 2
        }

        "can work with a fmap function" {
            val none = Option(null)
            none.map { x: Int -> x + 2 } shouldBe Option.None()

            val some = Option(2)
            some.map { it + 2 } shouldBe Option(4)
        }

        "can work with flatMap" {
            val none = Option(null)
            none.flatMap { x: Int -> Option(x + 2) } shouldBe Option.None()

            val some = Option(2)
            some.flatMap { x -> Option(x + 2) } shouldBe Option(4)
        }

        "finds items from a map" {
            val toons: Map<String, Toon> = mapOf(
                "Mickey" to Toon("Mickey", "Mouse", "mickey@disney.com"),
                "Minnie" to Toon("Minnie", "Mouse"),
                "Donald" to Toon("Donald", "Duck", "donald@disney.com")
            )

            val mickeyEmail = toons.getOption("Mickey").flatMap { it.email }
            val minnieEmail = toons.getOption("Minnie").flatMap { it.email }
            val donaldEmail = toons.getOption("Donald").flatMap { it.email }

            mickeyEmail shouldBe Option("mickey@disney.com")
            minnieEmail shouldBe Option()
            donaldEmail shouldBe Option.Some("donald@disney.com")
        }

        "can calculate variance" {
            val doubleList = listOf(1.1, 2.3, 3.2, 2.5)
            variance(doubleList).map { (it * 1000).toInt() } shouldBe Option(571)

            val doubleList2 = listOf(1.0, 1000.0, 2.0)
            variance(doubleList2).map { (it * 1000).toInt() } shouldBe Option(221556222)

            val doubleList3 = listOf(0.0, 0.0, 0.0)
            variance(doubleList3).map { (it * 1000).toInt() } shouldBe Option(0)

            val doubleList4 = listOf<Double>()
            variance(doubleList4) shouldBe Option()
        }
    }
}
