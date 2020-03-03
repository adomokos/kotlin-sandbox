package sandbox.arrow

import arrow.syntax.function.curried
import arrow.syntax.function.pipe
import arrow.syntax.function.reverse
import arrow.syntax.function.uncurried
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CurryingSpec : DescribeSpec({
    // Currying without arrow
    val addTax = { taxRate: Double ->
        { price: Double ->
            price + price * taxRate
        }
    }

    fun <A, B, C> partialA(a: A, f: (A) -> (B) -> C): (B) -> C = f(a)

    fun <A, B, C> partialB(b: B, f: (A) -> (B) -> C): (A) -> C = {
        a: A -> f(a)(b)
    }

    fun <A, B, C> explicitCurried(): (A) -> (B) -> (C) -> String = {
        a: A -> {
            b: B -> {
                c: C -> "$a $b $c"
            }
        }
    }

    val cStrong: (String, String, String) -> String =
        { body, id, style -> "<strong id=\"$id\" style=\"$style\">$body</strong>" }

    val curriedStrong: (style: String) -> (id: String) -> (body: String) -> String =
        cStrong.reverse().curried()

    val greenStrong: (id: String) -> (body: String) -> String =
        curriedStrong("color: green")

    val uncurriedGreenStrong: (id: String, body: String) -> String = greenStrong.uncurried()

    describe("Currying without arrow") {
        it("can be invoked with two function calls") {
            val result = addTax(0.09)(100.0)
            result shouldBe 109.0
        }

        it("applying partially returns a function when invoked with 1 argument") {
            val taxApplied = addTax(0.09)
            taxApplied(100.0) shouldBe 109.0
        }

        it("can curry with a generic function") {
            val partialAVal = partialA(3, { a: Int -> { b: Int -> a * b } })
            partialAVal(4) shouldBe 12
        }

        it("can apply the second argument") {
            val partialBVal = partialB(10, { a: Int -> { b: Int -> b - a } })
            partialBVal(4) shouldBe 6
        }

        it("can curry with 3 arguments") {
            val result = explicitCurried<String, String, String>() // ("oh")("my")("hey")
        }
    }

    describe("Currying: (A, B) -> R transformed into a chain of (A) -> (B) -> R") {
        it("can call functions curried") {
            val result = greenStrong("movie5")("Green Inferno")
            result shouldBe "<strong id=\"movie5\" style=\"color: green\">Green Inferno</strong>"

            val result2 = uncurriedGreenStrong("movie6", "Green Hornet")
            result2 shouldBe "<strong id=\"movie6\" style=\"color: green\">Green Hornet</strong>"
        }

        it("can be invoked with pipes") {
            val result = "Fried Green Tomatoes" pipe ("movie7" pipe greenStrong)
            result shouldBe "<strong id=\"movie7\" style=\"color: green\">Fried Green Tomatoes</strong>"
        }
    }
})
