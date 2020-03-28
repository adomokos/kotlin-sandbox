package sandbox.books.joyofkotlin

/*
Collections can be classified as:
  * linear collection (like list, there is connection between elements)
  * associative collection (no connection between elements, like maps or sets)
  * graph collection (trees, like binary tree)


 */

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.IllegalArgumentException

class Chapter05Spec : StringSpec() {
    // Sealed classes are implicitly abstract and their constructor
    // is implicitly private
    sealed class List<A> {
        abstract fun isEmpty(): Boolean
        abstract fun setHead(a: A): List<A>
        abstract fun drop(n: Int): List<A>
        abstract fun dropWhile(p: (A) -> Boolean): List<A>
        abstract fun reverse(): List<A>

        private object Nil : List<Nothing>() {
            override fun isEmpty() = true
            override fun setHead(a: Nothing): List<Nothing> =
                throw IllegalArgumentException("setHead called on an empty list")
            override fun drop(n: Int) = drop(this, n)
            override fun toString(): String = "[NIL]"
            override fun dropWhile(p: (Nothing) -> Boolean): List<Nothing> =
                dropWhile(this, p)
            override fun reverse(): List<Nothing> = reverse(List.invoke(), this)
        }

        fun cons(a: A): List<A> = Cons(a, this)

        private class Cons<A>(
            internal val head: A,
            internal val tail: List<A>
        ) : List<A>() {
            override fun isEmpty() = false

            override fun toString(): String = "[${toString("", this)}NIL]"

            override fun setHead(a: A): List<A> = tail.cons(a)

            override fun drop(n: Int): List<A> = drop(this, n)

            override fun dropWhile(p: (A) -> Boolean): List<A> = dropWhile(this, p)

            override fun reverse(): List<A> = reverse(List.invoke(), this)

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
                    a: A, list: List<A> -> Cons(a, list)
                }

            tailrec fun <A> drop(list: List<A>, n: Int): List<A> =
                when (list) {
                    Nil -> list
                    is Cons -> if (n <= 0) list else drop(list.tail, n - 1)
                }

            tailrec fun <A> dropWhile(list: List<A>, p: (A) -> Boolean): List<A> =
                when (list) {
                    Nil -> list
                    is Cons -> if (p(list.head)) dropWhile(list.tail, p) else list
                }

            fun <A> concat(list1: List<A>, list2: List<A>): List<A> =
                when (list1) {
                    Nil -> list2
                    is Cons -> concat(list1.tail, list2).cons(list1.head)
                }

            tailrec fun <A> reverse(acc: List<A>, list: List<A>): List<A> =
                when (list) {
                    Nil -> acc
                    is Cons -> reverse(acc.cons(list.head), list.tail)
                }
        }
    }

    init {
        "can work with singly linked lists" {
            val list = List(1, 2, 3) // this isn't called to the constructor
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

            List.dropWhile(initialList) { it < 3 }.toString() shouldBe "[3, 4, NIL]"
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
    }
}
