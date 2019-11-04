package arrow

import arrow.core.Option
import arrow.core.Some
import arrow.core.Tuple3
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.apply.tupled
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class ApplicativeSpec : DescribeSpec({
    describe("for Option") {
        it("can map over Option") {
            val resultx = Some(3).ap(Some<(Int) -> Int>(::inc))
            resultx shouldBe Some(4)

            val addNumbers = fun(x: Int, y: Int) = x + y

            val resultx2 = Option.applicative().map(Some(3), Some(4)) { (x, y) -> addNumbers(x, y) }
            resultx2 shouldBe Some(7)

            val add3: Option<(Int) -> Int> = Some { x: Int -> x + 3 }
            val result2 = tupled(add3, Some(3)).map { it.a(it.b) }
            val result3 = Some(3).ap(Some<(Int) -> Int> { it -> it + 2 })

            result2 shouldBe Some(6)
            result3 shouldBe Some(5)
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
