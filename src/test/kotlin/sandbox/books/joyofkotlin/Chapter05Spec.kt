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
        private object Nil : List<Nothing>() {
            override fun isEmpty() = true
            override fun toString(): String = "[NIL]"
        }

        private class Cons<A>(
            internal val head: A,
            internal val tail: List<A>
        ) : List<A>() {
            override fun isEmpty() = false

            override fun toString(): String = "[${toString("", this)}NIL]"

            private tailrec fun toString(acc: String, list: List<A>): String =
                when (list) {
                    is Nil -> acc
                    is Cons -> toString("$acc${list.head}, ", list.tail)
                }
        }

        companion object {
            @Suppress("UNCHECKED_CAST")
            operator fun <A> invoke(vararg az: A): List<A> =
                az.foldRight(Nil as List<A>) {
                    a: A, list: List<A> -> Cons(a, list)
                }
        }

        private fun cons(a: A): List<A> = Cons(a, this)

        fun setHead(a: A): List<A> =
            when (this) {
                is Nil -> throw IllegalArgumentException("setHead called on an empty list")
                is Cons -> tail.cons(a)
            }
    }

    init {
        "can work with singly linked lists" {
            val list = List(1, 2, 3) // this isn't called to the constructor
            // but to the companion objects `invoke` function
            list.isEmpty() shouldBe false
            list.toString() shouldBe "[1, 2, 3, NIL]"
        }

        "can add an element to the front" {
            val initialList = List(1, 2)
            val newList = initialList.setHead(3)

            newList.toString() shouldBe "[3, 2, NIL]"
        }
    }
}
