package samples

import io.kotlintest.matchers.startWith
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

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

class CalculatorSpec : StringSpec({
    "can add two numbers" {
        val calc = Calculator()
        val result = calc.add(1, 3)
        result shouldBe 4
    }

    "f:startsWith should test for a prefix" {
        "world" should startWith("wor")
    }
})

// object CalculatorTest: Spek({
    // describe("Calculator") {
        // val calculator by memoized { Calculator() }

        // it ("can add two numbers") {
            // assertEquals(3, calculator.add(1, 2))
        // }

		// it ("can subtract two numbers") {
			// assertEquals(1, calculator.subtract(3, 2))
		// }

		// it ("can multiply two numbers") {
			// assertEquals(6, calculator.multiply(2, 3))
		// }
    // }
// })
