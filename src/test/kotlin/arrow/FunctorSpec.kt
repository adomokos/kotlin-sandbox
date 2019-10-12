package arrow

import arrow.core.*
import arrow.core.extensions.option.functor.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

fun addOne(n: Int): Int {
    return n + 1
}

fun inc(n: Int) = n + 1

class FunctorSpec : StringSpec({
    "can map over Option" {
        val result = Option(1).map { 1 * 2 }
        result shouldBe Some(2)

        val noneResult = none<Int>().map { it * 2 }
        noneResult shouldBe None

        // Functor with Option
        val x = Option(1).map(::inc)
        x shouldBe Some(2)

        // Functor with Either
        val y = Either.Right(5).map(::inc)
        y shouldBe Either.Right(6)
    }

    "works with Kind<F,A>#map combinator" {
        val optionFunctor = Option.functor()
        val result = optionFunctor.run { Option(1).map { it + 1 } }

        result shouldBe Some(2)
    }

    "can lift a function into the Functor context" {
        val optionFunctor = Option.functor()
        // val addOne = {n: Int -> n + 1}
        val lifted = optionFunctor.lift(::addOne)
        lifted(Option(1)) shouldBe Some(2)
    }
})
