package arrow

import arrow.core.*
import arrow.core.extensions.fx
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

fun sumNel(nel: NonEmptyList<Int>): Int =
    nel.foldLeft(0) { acc, n -> acc + n }

class NonEmptyListSpec : StringSpec({
    "can not be initialized without elements" {
        val nel = NonEmptyList.of(1, 2, 3, 4, 5)
        nel.size shouldBe 5
    }

    "can get the first element" {
        val nel = NonEmptyList.of(1, 2, 3, 4, 5)
        nel.head shouldBe 1
    }

    "can fold left and right" {
        val nel = NonEmptyList.of(1,1,1,1)
        sumNel(nel) shouldBe 4
    }

    "can compute over the contents of multiple" {
        val nelOne: NonEmptyList<Int> = NonEmptyList.of(1,2)
        val nelTwo: NonEmptyList<Int> = NonEmptyList.of(3,4)

        val result = nelOne.flatMap { one ->
            nelTwo.map { two ->
                one + two
            }
        }

        result shouldBe NonEmptyList.of(4,5,5,6)
    }

    "can be used with monad bindings" {
        val nelOne: NonEmptyList<Int> = NonEmptyList.of(1)
        val nelTwo: NonEmptyList<Int> = NonEmptyList.of(2)
        val nelThree: NonEmptyList<Int> = NonEmptyList.of(3)

        val res = NonEmptyList.fx {
            val (one) = nelOne
            val (two) = nelTwo
            val (three) = nelThree
            one + two + three
        }

        res shouldBe NonEmptyList.of(6)
    }
})
