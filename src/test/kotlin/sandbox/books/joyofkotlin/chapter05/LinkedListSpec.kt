package sandbox.books.joyofkotlin.chapter05

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.IllegalArgumentException
import sandbox.books.joyofkotlin.chapter07.Result

/*
Collections can be classified as:
  * linear collection (like list, there is connection between elements)
  * associative collection (no connection between elements, like maps or sets)
  * graph collection (trees, like binary tree)
 */

// Sealed classes are implicitly abstract and their constructor
// is implicitly private
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
        override fun lengthMemoized(): Int = 0
        override fun headSafe() = Result<Nothing>()
    }

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

        fun <B> foldLeft(acc: B, f: (B) -> (A) -> B): B =
            List.foldLeft(acc, this, f)

        private tailrec fun toString(acc: String, list: List<A>): String =
            when (list) {
                Nil -> acc
                is Cons -> toString("$acc${list.head}, ", list.tail)
            }

        override fun lengthMemoized(): Int = length

        override fun headSafe() = Result(head)
    }

    fun cons(a: A): List<A> =
        Cons(a, this)

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

        tailrec fun <A, B> coFoldRight(
            acc: B,
            list: List<A>,
            identity: B,
            f: (A) -> (B) -> B
        ): B =
            when (list) {
                List.Nil -> acc
                is List.Cons -> coFoldRight(f(list.head)(acc), list.tail, identity, f)
            }

        // This is stack safe and corecursive
        tailrec fun <A, B> foldLeft(acc: B, list: List<A>, f: (B) -> (A) -> B): B =
            when (list) {
                Nil -> acc
                is Cons -> foldLeft(f(acc) (list.head), list.tail, f)
            }

        fun <A> lastSafe(list: List<A>): Result<A> =
            foldLeft(Result(), list) { _: Result<A> -> { y: A -> Result(y) } }

        // A different way to call headSafe
        fun <A> headSafe(list: List<A>): Result<A> =
            foldRight(list, Result()) { x: A -> { _: Result<A> -> Result(x) } }

        @Suppress("UNCHECKED_CAST")
        fun <A> flattenResult(list: List<Result<A>>): List<A> =
            list.flatMap { ra -> ra.map { List(it) }.getOrElse(Nil as List<A>) }

        @Suppress("UNCHECKED_CAST")
        fun <A> flatten(list: List<List<A>>): List<A> =
            list.foldRight(Nil as List<A>) { x -> x::concat }

        @Suppress("UNCHECKED_CAST")
        fun <A> sequence(list: List<Result<A>>): Result<List<A>> =
            list.foldRight(Result(Nil as List<A>)) { x ->
                { y: Result<List<A>> ->
                    Result.map2(x, y) { a -> { b: List<A> -> b.cons(a) } }
                }
            }

        @Suppress("UNCHECKED_CAST")
        fun <A> sequence2(list: List<Result<A>>): Result<List<A>> =
            list.filter { !it.isEmpty() }
                .foldRight(Result(Nil as List<A>)) { x ->
                    { y: Result<List<A>> ->
                        Result.map2(x, y) { a -> { b: List<A> ->
                            b.cons(a)
                        } }
                    }
                }

        @Suppress("UNCHECKED_CAST")
        fun <A, B> traverse(list: List<A>, f: (A) -> Result<B>): Result<List<B>> =
            list.foldRight((Result(Nil as List<B>))) { x ->
                { y: Result<List<B>> ->
                    Result.map2(f(x), y) { a -> { b: List<B> -> b.cons(a) } }
                }
            }
    }

    fun <B> flatMap(f: (A) -> List<B>): List<B> = flatten(map(f))

    fun <B> foldRight(identityVal: B, f: (A) -> (B) -> B): B =
        List.foldRight(this, identityVal, f)

    fun <B> coFoldRight(identity: B, f: (A) -> (B) -> B): B =
        List.coFoldRight(identity, this.reverse(), identity, f)

    @Suppress("UNCHECKED_CAST")
    fun <B> map(f: (A) -> B): List<B> =
        coFoldRight(Nil as List<B>) { h -> { t: List<B> -> Cons(f(h), t) } }

    fun concat(list: List<A>): List<A> = List.concat(this, list)

    @Suppress("UNCHECKED_CAST")
    fun filter(p: (A) -> Boolean): List<A> =
        coFoldRight(Nil as List<A>) { h -> { t: List<A> ->
            if (p(h)) Cons(h, t) else t }
        }
}

class LinkedListSpec : StringSpec() {
    private fun product(ints: List<Int>): Int =
        when (ints) {
            is List.Cons -> ints.head * product(ints.tail)
            else -> 1
        }

    private fun sum(ints: List<Int>): Int =
        when (ints) {
            is List.Cons -> ints.head + sum(ints.tail)
            else -> 0
        }

    fun sum1(list: List<Int>): Int = List.foldRight(list, 0) { x -> { y -> x + y } }
    fun product1(list: List<Int>): Int = List.foldRight(list, 1) { x -> { y -> x * y } }
    fun listLength(list: List<Int>): Int = List.foldRight(list, 0) { { it + 1 } }

    fun sum2(list: List<Int>): Int = List.foldLeft(0, list) { x -> { y -> x + y } }
    fun product2(list: List<Int>): Int = List.foldLeft(1, list) { x -> { y -> x * y } }
    fun listLength2(list: List<Int>): Int = List.foldLeft(0, list) { i -> { i + 1 } }

    init {
        "can work with singly linked lists" {
            val list = List(
                1,
                2,
                3
            ) // this isn't called to the constructor
            // but to the companion objects `invoke` function
            list.isEmpty() shouldBe false
            list.toString() shouldBe "[1, 2, 3, NIL]"
        }

        "can set an element to the head of the list" {
            val initialList = List(1, 2)
            val newList = initialList.setHead(3)

            newList.toString() shouldBe "[3, 2, NIL]"
        }

        "can drop elements from a list" {
            val initialList = List(1, 2, 3, 4)
            val lighterList = initialList.drop(2)

            lighterList.toString() shouldBe "[3, 4, NIL]"
        }

        "can use drop function from sealed object" {
            val initialList = List(1, 2, 3, 4)

            List.drop(initialList, 2).toString() shouldBe "[3, 4, NIL]"
        }

        "can drop elements with dropWhile()" {
            val initialList = List(1, 2, 3, 4)

            List.dropWhile(initialList) { it < 3 }
                .toString() shouldBe "[3, 4, NIL]"
        }

        "can concat lists" {
            val list1 = List(1, 2, 3)
            val list2 = List(8, 10)

            List.concat(list1, list2).toString() shouldBe "[1, 2, 3, 8, 10, NIL]"
        }

        "can reverse a list" {
            val list1 = List(1, 2, 3)

            list1.reverse().toString() shouldBe "[3, 2, 1, NIL]"
        }

        "can calculate the product of a list" {
            val list = List(1, 2, 3, 4)

            product(list) shouldBe 24
            product1(list) shouldBe 24
            product2(list) shouldBe 24
        }

        "can calculate the sum a list" {
            val list = List(1, 2, 3, 4)

            sum(list) shouldBe 10
            sum1(list) shouldBe 10
            sum2(list) shouldBe 10
        }

        "can calculate the length" {
            val list = List(1, 2, 3, 4)

            listLength(list) shouldBe 4
            listLength2(list) shouldBe 4
        }

        "can map over a list with a function" {
            val list = List(1, 2, 3, 4)
            val result = list.map { it + 2 }

            result.toString() shouldBe "[3, 4, 5, 6, NIL]"
        }

        "can flatMap a list" {
            val list = List(1, 2, 3)
            val f: (Int) -> List<Int> = { x -> List(x, -x) }
            val result = list.flatMap(f)

            result.toString() shouldBe "[1, -1, 2, -2, 3, -3, NIL]"
        }
    }
}
