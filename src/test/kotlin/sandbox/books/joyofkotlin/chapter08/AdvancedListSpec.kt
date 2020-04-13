package sandbox.books.joyofkotlin.chapter08

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import sandbox.books.joyofkotlin.chapter05.List
import sandbox.books.joyofkotlin.chapter07.Result

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

        "can use foldRight to find the first element - not very efficient" {
            val list = List(1, 2, 3)
            List.headSafe(list) shouldBe Result(1)
        }

        "can map Success items into a resulting list" {
            val list = List(Result(1), Result(), Result(2))
            List.flattenResult(list).toString() shouldBe "[1, 2, NIL]"
        }

        "can sequence through a list of Results" {
            val list = List(Result(1), Result(), Result(2))
            val result = List.sequence(list)

            result shouldBe Result.Empty

            val list2 = List(Result(1), Result(2), Result(3))
            val result2 = List.sequence(list2)

            result2.toString() shouldBe "Success([1, 2, 3, NIL])"

            val result3 = List.sequence2(list2)

            result3.toString() shouldBe "Success([1, 2, 3, NIL])"
        }
    }
}
