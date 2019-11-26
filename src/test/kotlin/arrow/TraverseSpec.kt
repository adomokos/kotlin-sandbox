package arrow

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

fun parseIntEither(s: String): Either<NumberFormatException, Int> {
    return try {
        s.toInt().right()
    }
    catch (_: Exception) {
        NumberFormatException("Error converting $s to Int").left()
    }
}
class TraverseSpec : DescribeSpec({

    describe("Can traverse through a list and report error") {
        it("can safely convert list of Int candidates") {
            val correctItems = listOf("1", "2", "3")
            val incorrectItems = listOf("1", "2", "Jimmy")

            val result = correctItems.traverse(Either.applicative(), ::parseIntEither).fix()
            when (result) {
                is Either.Left -> throw error("should be right")
                is Either.Right -> result.b shouldBe listOf(1, 2, 3)
            }
        }
    }
})
