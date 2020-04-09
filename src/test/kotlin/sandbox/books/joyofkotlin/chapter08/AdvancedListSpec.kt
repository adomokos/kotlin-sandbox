package sandbox.books.joyofkotlin.chapter08

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.IllegalArgumentException
import sandbox.books.joyofkotlin.chapter07.Result

sealed class List<A> {
    abstract fun isEmpty(): Boolean
    abstract fun setHead(a: A): List<A>
    abstract fun drop(n: Int): List<A>
    abstract fun dropWhile(p: (A) -> Boolean): List<A>
    abstract fun reverse(): List<A>
    abstract fun lengthMemoized(): Int
    abstract fun headSafe(): Result<A>

    object Nil : List<Nothing>() {
        override fun isEmpty() = true
        override fun setHead(a: Nothing): List<Nothing> =
            throw IllegalArgumentException("setHead called on an empty list")
        override fun drop(n: Int) =
            drop(this, n)
        override fun toString(): String = "[NIL]"
        override fun dropWhile(p: (Nothing) -> Boolean): List<Nothing> =
            dropWhile(this, p)
        override fun reverse(): List<Nothing> =
            reverse(
                invoke(),
                this
            )
        override fun headSafe() = Result<Nothing>()

        override fun lengthMemoized(): Int = 0
    }

    fun cons(a: A): List<A> =
        Cons(a, this)

    class Cons<A>(
        internal val head: A,
        internal val tail: List<A>
    ) : List<A>() {
        private val length: Int = tail.lengthMemoized() + 1

        override fun isEmpty() = false

        override fun toString(): String = "[${toString("", this)}NIL]"

        override fun setHead(a: A): List<A> = tail.cons(a)

        override fun drop(n: Int): List<A> =
            drop(this, n)

        override fun dropWhile(p: (A) -> Boolean): List<A> =
            dropWhile(this, p)

        override fun reverse(): List<A> =
            reverse(
                invoke(),
                this
            )

        override fun lengthMemoized(): Int = length

        override fun headSafe() = Result(head)

        fun <B> foldLeft(acc: B, f: (B) -> (A) -> B): B =
            List.foldLeft(acc, this, f)

        private tailrec fun toString(acc: String, list: List<A>): String =
            when (list) {
                Nil -> acc
                is Cons -> toString("$acc${list.head}, ", list.tail)
            }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun <A> invoke(vararg az: A): List<A> =
            az.foldRight(Nil as List<A>) {
                    a: A, list: List<A> ->
                Cons(a, list)
            }

        tailrec fun <A> drop(list: List<A>, n: Int): List<A> =
            when (list) {
                Nil -> list
                is Cons -> if (n <= 0) list else drop(
                    list.tail,
                    n - 1
                )
            }

        tailrec fun <A> dropWhile(list: List<A>, p: (A) -> Boolean): List<A> =
            when (list) {
                Nil -> list
                is Cons -> if (p(list.head)) dropWhile(
                    list.tail,
                    p
                ) else list
            }

        fun <A> concat(list1: List<A>, list2: List<A>): List<A> =
            when (list1) {
                Nil -> list2
                is Cons -> concat(
                    list1.tail,
                    list2
                ).cons(list1.head)
            }

        tailrec fun <A> reverse(acc: List<A>, list: List<A>): List<A> =
            when (list) {
                Nil -> acc
                is Cons -> reverse(
                    acc.cons(list.head),
                    list.tail
                )
            }

        fun <A, B> foldRight(
            list: List<A>,
            identityVal: B,
            f: (A) -> (B) -> B
        ): B =
            when (list) {
                Nil -> identityVal
                is Cons -> f(list.head) (foldRight(list.tail, identityVal, f))
            }

        // This is stack safe and corecursive
        tailrec fun <A, B> foldLeft(acc: B, list: List<A>, f: (B) -> (A) -> B): B =
            when (list) {
                Nil -> acc
                is Cons -> foldLeft(f(acc) (list.head), list.tail, f)
            }

        fun <A> lastSafe(list: List<A>): Result<A> =
            foldLeft(Result(), list) { _: Result<A> -> { y: A -> Result(y) } }
    }
}

class AdvancedListSpec : StringSpec() {
    init {
        "can return the memoized length of the List" {
            val list = List(3, 2, 5, 4)
            list.lengthMemoized() shouldBe 4

            List<Int>().lengthMemoized() shouldBe 0
        }

        "can safely access the head value" {
            val list = List(1, 2, 3)
            list.headSafe() shouldBe Result(1)

            List<Int>().headSafe() shouldBe Result()
        }

        "lastSafe function grabs the last element safely" {
            val list = List(1, 2, 3)
            List.lastSafe(list) shouldBe Result(3)

            val emptyList = List<Int>()
            List.lastSafe(emptyList) shouldBe Result()
        }
    }
}
