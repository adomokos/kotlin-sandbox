package sandbox.arrow

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.fix
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

fun returnLastElement(): Int {
    return IO.fx {
        1
    }.fix().unsafeRunSync()
}

fun secondOperation(): Int {
    return IO.fx {
        val (a) = IO.invoke { 1 }
        a + 1
    }.fix().unsafeRunSync()
}

fun withoutBind(): Int {
    return IO.invoke { 1 }
        .flatMap { result ->
            IO.just(result + 1)
        }
        .fix().unsafeRunSync()
}

class MonadComprehensionSpec : StringSpec({
    "returns the last element" {
        returnLastElement() shouldBe 1
    }

    "returns the result of 2 operations" {
        secondOperation() shouldBe 2
    }

    "returns without the bind" {
        withoutBind() shouldBe 2
    }
})
