package sandbox.arrow

import arrow.core.Option
import arrow.core.Some
import arrow.core.Tuple3
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.apply.tupled
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class ApplicativeSpec : DescribeSpec({
    describe("for Option") {
        it("can map over a function wrapped in Option") {
            val result = Some(3).ap(Some<(Int) -> Int>(::inc))
            result shouldBe Some(4)
        }

        it("can map over applicative instance") {
            val addNumbers = fun(x: Int, y: Int) = x + y

            val result = Option.applicative().map(Some(3), Some(4)) { (x, y) -> addNumbers(x, y) }
            result shouldBe Some(7)
        }

        it("can be executed with tupled") {
            val add3: Option<(Int) -> Int> = Some { x: Int -> x + 3 }
            val result = tupled(add3, Some(3)).map { it.a(it.b) }
            val result2 = Some(3).ap(Some<(Int) -> Int> { it + 2 })

            result shouldBe Some(6)
            result2 shouldBe Some(5)
        }

        it("works with Kind<F, A>#ap combinator") {
            val applicative = Option.applicative()
            val result = applicative.run { Some(3).ap(Some { x: Int -> x + 1 }) }
            result shouldBe Some(4)
        }

        it("works with tupled") {
            val result = tupled(Some(1), Some("Hello"), Some(20.0))
            // Tuple3 is a Functor
            result shouldBe Some(Tuple3(a = 1, b = "Hello", c = 20.0))
        }
    }
})
