package arrow

import arrow.syntax.function.bind
import arrow.syntax.function.partially3
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

val strongHtml: (String, String, String) -> String =
    { body, id, style -> "<strong id='$id' style='$style'>$body</strong>" }

val redStrongHtml: (String, String) -> String =
    strongHtml.partially3("font: red")

val blueStrongHtml: (String, String) -> String =
    strongHtml.partially3("font: red")

class PartialSpec : DescribeSpec({
    describe("Partials") {
        it("can invoke functions partially") {
            val redHtml = redStrongHtml("Red Sonja", "movie1")
            redHtml shouldBe
                "<strong id='movie1' style='font: red'>Red Sonja</strong>"
        }

        it("can use bind, which is an alias to partially1") {
            val footer: (String) -> String = { content -> "<footer>$content</footer>" }
            val fixFooter: () -> String = footer.bind("Functional Kotlin")

            fixFooter() shouldBe "<footer>Functional Kotlin</footer>"
        }
    }
})
