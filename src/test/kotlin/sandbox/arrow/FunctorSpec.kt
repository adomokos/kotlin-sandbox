package sandbox.arrow

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Right
import arrow.core.Some
import arrow.core.extensions.either.functor.functor
import arrow.core.extensions.fx
import arrow.core.extensions.option.functor.functor
import arrow.core.none
import arrow.fx.IO
import arrow.fx.extensions.fx
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

fun addOne(n: Int): Int {
    return n + 1
}

fun inc(n: Int) = n + 1

class FunctorSpec : DescribeSpec({
    val optionFunctor = Option.functor()
    val eitherFunctor = Either.functor<String>()

    describe("for Option") {
        it("can map over") {
            val result = Option(1).map { it * 2 }
            result shouldBe Some(2)

            val noneResult = none<Int>().map { it * 2 }
            noneResult shouldBe None

            // Functor with Option
            val x = Option(1).map(::inc)
            x shouldBe Some(2)
        }

        it("works with Kind<F,A>#map combinator") {
            val result = optionFunctor.run { Option(1).map { it + 1 } }

            result shouldBe Some(2)
        }

        it("can lift a function into the Functor context") {
            val lifted = optionFunctor.lift(::addOne)
            lifted(Option(1)) shouldBe Some(2)
        }

        it("can extract from the Functor context with fx") {
            val result = Option(1).map { it * 2 }

            Option.fx {
                val (calcResult) = result
                calcResult shouldBe 2
            }
        }
    }

    describe("for Either") {
        it("can map over") {
            var result = Either.right(1).map { it * 2 }
            result shouldBe Right(2)

            var leftResult = Either.left("Will not work").map(::inc)
            leftResult shouldBe Either.Left("Will not work")
        }

        it("works with Kind<F, A>#map combinator") {
            val result = eitherFunctor.run { Either.right(2).map { it + 1 } }
            result shouldBe Either.Right(3)
        }

        it("can lift a function into the Functor context") {
            val lifted = eitherFunctor.lift(::addOne)
            lifted(Either.right(2)) shouldBe Either.Right(3)
        }
    }

    describe("for IO") {
        it("can map over") {
            var ioCalc = IO<Int> { 1 }.map { it * 2 }
            IO.fx {
                val (result) = ioCalc
                result shouldBe Right(2)
            }
        }
    }
})
