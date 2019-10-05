package arrow

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import arrow.*
import arrow.fx.*
import arrow.typeclasses.*
import arrow.fx.extensions.fx

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

object MonadComprehensionsSpec: Spek({
    describe("Monad Comprehensions") {
        it ("returns the last element") {
            assertEquals(1, returnLastElement())
        }

        it ("returns the result of 2 operations") {
            assertEquals(2, secondOperation())
        }

		it ("returns without the bind") {
            assertEquals(2, withoutBind())
		}
    }
})
