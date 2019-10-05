package arrow

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import arrow.core.*
import arrow.core.extensions.option.functor.*

fun addOne(n: Int): Int {
    return n + 1
}

fun inc(n: Int) = n + 1

object FunctorSpec: Spek({
    describe("Functors") {
        val optionFunctor by memoized { Option.functor() }

        it ("can map over Option") {
            val result = Option(1).map { 1 * 2 }
            assertEquals(Some(2), result)

            val noneResult = none<Int>().map { it * 2 }
            assertEquals(None, noneResult)

            // Functor with Option
            val x = Option(1).map(::inc)
            assertEquals(Some(2), x)

            // Functor with Either
            val y = Either.Right(5).map(::inc)
            assertEquals(Either.Right(6), y)
        }

        it ("works with Kind<F,A>#map combinator") {
            val result = optionFunctor.run { Option(1).map { it + 1 } }

            assertEquals(Some(2), result)
        }

        it ("can lift a function into the Functor context") {
            // val addOne = {n: Int -> n + 1}
            val lifted = optionFunctor.lift(::addOne)
            assertEquals(Some(2), lifted(Option(1)))
        }
    }
})
