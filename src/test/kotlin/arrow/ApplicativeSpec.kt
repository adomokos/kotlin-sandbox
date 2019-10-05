package arrow

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import arrow.core.*
import arrow.core.extensions.option.apply.*

object ApplicativeSpec: Spek({
    describe("Applicatives") {
        it ("works with the Option type") {
            val result = tupled(Some(1), Some("Hello"), Some(20.0))
            // Tuple3 is a Functor
            assertEquals(result, Some(Tuple3(a=1, b="Hello", c=20.0)))

            val add3: Option<(Int) -> Int> = Some({ x: Int -> x + 3 })
            val result2 = tupled(add3, Some(3)).map { it.a(it.b) }
            val result3 = Some(3).ap(Some<(Int) -> Int>({ it -> it + 2 }))

            assertEquals(Some(6), result2)
            assertEquals(Some(5), result3)
        }
    }
})
