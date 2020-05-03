package sandbox.books.joyofkotlin.chapter05

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

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
