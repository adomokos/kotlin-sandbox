package arrow

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import arrow.core.*
import arrow.core.extensions.fx

object MonadSpec: Spek({
    describe("Monads") {
        it ("works with the Option type - Some values") {
            val result = Option.fx {
                val (a) = Some(1)
                val (b) = Some(1+a)
                val (c) = Some(1+b)
                a + b + c
            }

            assertEquals(Some(6), result)
        }

        it ("works with the Option type - None value") {
            val result = Option.fx {
                val (a) = none<Int>()
                val (b) = Some(1+a)
                val (c) = Some(1+b)
                a + b + c
            }

            assertEquals(None, result)
        }
    }
})
