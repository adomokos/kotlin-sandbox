package sandbox.books.joyofkotlin.chapter07

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import sandbox.books.joyofkotlin.chapter05.List as LList

sealed class Either<out A, out B> {
    internal data class Left<out A, out B>(val value: A) : Either<A, B>() {
        override fun toString(): String = "Left($value)"
    }

    internal data class Right<out A, out B>(private val value: B) : Either<A, B>() {
        override fun toString(): String = "Right($value)"
    }

    companion object {
        fun <A, B> left(value: A): Either<A, B> = Left(value)
        fun <A, B> right(value: B): Either<A, B> = Right(value)
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

    init {
        "can work with custom Either type" {
            val list = LList(3, 5, 8, 4, 1)

            max(list) shouldBe Either.right<String, Int>(8)
        }
    }
}
