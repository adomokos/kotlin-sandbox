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

        "can traverse a list" {
            val list = List(1, 2, 3, 4)

            val f: (Int) -> Result<Int> = { x -> if (x % 2 == 0) Result(x) else Result() }
            val result = List.traverse(list, f)
            val list2 = List(2, 4, 6)
            val result2 = List.traverse(list2, f)

            result2.toString() shouldBe "Success([2, 4, 6, NIL])"
        }

        "can zip two lists with a function" {
            val list1 = List(1, 2, 3)
            val list2 = List('.', '?', '!', ';')
            val f: (Int) -> (Char) -> String = { x -> { y -> "$x$y" } }

            val result = List.zipWith(list1, list2, f)
            result.toString() shouldBe "[1., 2?, 3!, NIL]"

            val list3 = List<Int>()
            val result2 = List.zipWith(list3, list2, f)
            result2.toString() shouldBe "[NIL]"
        }

        "can calculate all permutations with product" {
            val list1 = List(1, 2, 3)
            val list2 = List('.', '?', '!')
            val f: (Int) -> (Char) -> String = { x -> { y -> "$x$y" } }

            val result = List.product(list1, list2, f)
            result.toString() shouldBe "[1., 1?, 1!, 2., 2?, 2!, 3., 3?, 3!, NIL]"
        }

        "can unzip a list of pairs into a pair of lists" {
            val list = List(Pair("a", 1), Pair("b", 2), Pair("c", 3))
            val (x, y) = List.unzip(list)

            x.toString() shouldBe "[a, b, c, NIL]"
            y.toString() shouldBe "[1, 2, 3, NIL]"
        }

        "retrieve item by index recursively" {
            val list1 = List(1, 2, 3)

            list1.getAt(1) shouldBe Result(2)
            list1.getAtNoNilCheck(2) shouldBe Result(3)
            list1.getAtViaFoldLeft(0) shouldBe Result(1)
        }

        "splits the list by index" {
            val list = List(1, 2, 3)

            val result = list.splitAt(1)
            result.first.toString() shouldBe "[1, NIL]"
            result.second.toString() shouldBe "[2, 3, NIL]"

            val result2 = list.splitAt(3)
            result2.first.toString() shouldBe "[1, 2, 3, NIL]"
            result2.second.toString() shouldBe "[NIL]"
        }
    }
}
