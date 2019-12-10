package sandbox.arrow

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.fx
import arrow.core.none
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MonadSpec : StringSpec({
    "works with the Option type - Some values" {
        val result = Option.fx {
            val (a) = Some(1)
            val (b) = Some(1 + a)
            val (c) = Some(1 + b)
            a + b + c
        }

        result shouldBe Some(6)
    }

    "works with the Option type - None value" {
        val result = Option.fx {
            val (a) = none<Int>()
            val (b) = Some(1 + a)
            val (c) = Some(1 + b)
            a + b + c
        }

        result shouldBe None
    }
})
