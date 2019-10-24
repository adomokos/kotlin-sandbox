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
        it("can map over") {
            val resultx = Some(3).ap(Some<(Int) -> Int>(::inc))
            // val resultx2 = Some<(Int) -> Int>(::inc).ap(Some(4))
            resultx shouldBe Some(4)

            val addNumbers = fun(x: Int, y: Int) = x + y

            val resultx2 = Option.applicative().map(Some(3), Some(4)) { (x, y) -> addNumbers(x, y) }
            resultx2 shouldBe Some(7)

            val resultx3 = Option.applicative().run { Some(3).ap(Some { x: Int -> x + 1 }) }
            resultx3 shouldBe Some(4)

            val result = tupled(Some(1), Some("Hello"), Some(20.0))
            // Tuple3 is a Functor
            result shouldBe Some(Tuple3(a = 1, b = "Hello", c = 20.0))

            val add3: Option<(Int) -> Int> = Some { x: Int -> x + 3 }
            val result2 = tupled(add3, Some(3)).map { it.a(it.b) }
            val result3 = Some(3).ap(Some<(Int) -> Int> { it -> it + 2 })

            result2 shouldBe Some(6)
            result3 shouldBe Some(5)
        }
    }
})
