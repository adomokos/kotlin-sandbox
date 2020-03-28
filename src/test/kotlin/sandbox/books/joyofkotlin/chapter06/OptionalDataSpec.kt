package sandbox.books.joyofkotlin.chapter06

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// Optional data is computational context for safely handling optional data
sealed class Option<out A> {
    abstract fun isEmpty(): Boolean
    abstract fun <B> map(f: (A) -> B): Option<B>
    abstract fun <B> flatMap(f: (A) -> Option<B>): Option<B>

    internal object None : Option<Nothing>() {
        override fun isEmpty() = true
        override fun <B> map(f: (Nothing) -> B): Option<B> = None
        override fun <B> flatMap(f: (Nothing) -> Option<B>): Option<B> = None
        override fun toString(): String = "None"
        override fun equals(other: Any?): Boolean =
            other === None
        override fun hashCode(): Int = 0
    }

    internal data class Some<out A>(internal val value: A) : Option <A>() {
        override fun isEmpty() = false
        override fun <B> map(f: (A) -> B): Option<B> = Some(f(value))
        override fun <B> flatMap(f: (A) -> Option<B>): Option<B> = f(value)
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Option<A> =
            when (a) {
                null -> None
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

class OptionalDataSpec : StringSpec() {
    init {
        "can create Option value" {
            val none = Option(null)
            none shouldBe Option.None

            val some = Option(2)
            some shouldBe Option.Some(2)
        }

        "can get the value or return default for None" {
            val some = Option(2)
            some.getOrElse(3) shouldBe 2
        }

        "can work with a fmap function" {
            val some = Option(2)
            some.map { it + 2 } shouldBe Option(4)
        }

        "can work with flatMap" {
            val none = Option(null)
            none.flatMap { Option(2) } shouldBe Option.None

            val some = Option(2)
            some.flatMap { x -> Option(x + 2) } shouldBe Option(4)
        }
    }
}
