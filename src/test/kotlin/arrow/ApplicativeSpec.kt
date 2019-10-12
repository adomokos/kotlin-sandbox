package arrow

import arrow.core.Option
import arrow.core.Some
import arrow.core.Tuple3
import arrow.core.extensions.option.apply.tupled
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ApplicativeSpec : StringSpec({
    "works with the Option type" {
        val result = tupled(Some(1), Some("Hello"), Some(20.0))
        // Tuple3 is a Functor
        result shouldBe Some(Tuple3(a = 1, b = "Hello", c = 20.0))

        val add3: Option<(Int) -> Int> = Some { x: Int -> x + 3 }
        val result2 = tupled(add3, Some(3)).map { it.a(it.b) }
        val result3 = Some(3).ap(Some<(Int) -> Int> { it -> it + 2 })

        result2 shouldBe Some(6)
        result3 shouldBe Some(5)
    }
})
