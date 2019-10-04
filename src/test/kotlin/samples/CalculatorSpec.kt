package samples

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

class Calculator {
    fun add(a: Int, b: Int): Int {
        return a + b
    }

	fun subtract(a: Int, b: Int): Int {
		return a - b
	}

	fun multiply(a: Int, b: Int): Int {
		return a * b
	}
}

object CalculatorTest: Spek({
    describe("Calculator") {
        val calculator by memoized { Calculator() }

        it ("can add two numbers") {
            assertEquals(3, calculator.add(1, 2))
        }

		it ("can subtract two numbers") {
			assertEquals(1, calculator.subtract(3, 2))
		}

		it ("can multiply two numbers") {
			assertEquals(6, calculator.multiply(2, 3))
		}
    }
})
