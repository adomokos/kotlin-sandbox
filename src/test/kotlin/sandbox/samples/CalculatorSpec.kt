package sandbox.samples

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

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

    "startsWith should test for a prefix" {
        "world".shouldStartWith("wor")
    }
})
