package samples

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

fun <T : Comparable<T>> quickSort(numbers: List<T>): List<Any?> =
    when {
        numbers.isEmpty() -> numbers
        else -> {
            val head = numbers.first()
            quickSort(numbers.filter {it < head }) +
                listOf(head) +
                quickSort(numbers.filter { it > head })
        }
    }

class QuickSortSpec : DescribeSpec({
    describe("QuickSort") {
        it("can sort an empty list") {
            quickSort(listOf<Int>()) shouldBe listOf<Int>()
        }

        it("can sort a list of Ints") {
            quickSort(listOf(5, 3, 4, 7, 9, 8)) shouldBe listOf(3, 4, 5, 7, 8, 9)
        }

        it("can sort a list of chars in a string") {
            quickSort("Hello World!"
                        .split(""))
                        .joinToString("") shouldBe " !HWdelor"
        }
    }
})
