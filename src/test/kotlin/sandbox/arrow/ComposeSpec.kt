package sandbox.arrow

import arrow.core.andThen
import arrow.core.compose
import arrow.syntax.function.forwardCompose
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

val p: (String) -> String = { body -> "<p>$body</p>" }
val span: (String) -> String = { body -> "<span>$body</span>" }
val div: (String) -> String = { body -> "<div>$body</div>" }
val strong: (String) -> String = { body -> "<strong>$body</strong>" }

val divStrong: (String) -> String = div compose strong
val spanP: (String) -> String = p forwardCompose span
val divSpan: (String) -> String = div andThen span

// Using compose and forwardCompose to create channels

data class Quote(val value: Double, val client: String, val item: String, val quantity: Int)
data class Bill(val value: Double, val client: String) {
    override fun toString(): String =
        "Bill $value - $client"
}
data class PickingOrder(val item: String, val quantity: Int) {
    override fun toString(): String =
        "PickingOrder $item - $quantity"
}

fun calculatePrice(quote: Quote): Pair<Bill, PickingOrder> =
    Bill(quote.value * quote.quantity, quote.client) to PickingOrder(quote.item, quote.quantity)

fun filterBills(billAndOrder: Pair<Bill, PickingOrder>): Pair<Bill, PickingOrder>? {
    val (bill, _) = billAndOrder
    return if (bill.value >= 100) {
        billAndOrder
    } else {
        null
    }
}

fun warehouse(order: PickingOrder): String =
    "\nProcessing order = $order"

fun accounting(bill: Bill) =
    "\nProcessing bill = $bill"

fun splitter(billAndOrder: Pair<Bill, PickingOrder>?): String {
    val result = StringBuilder()
    if (billAndOrder != null) {
        result.append(warehouse(billAndOrder.second))
        result.append(accounting(billAndOrder.first))
    }
    return result.toString()
}

class ComposeSpec : DescribeSpec({
    describe("Functions can be composed") {
        it("can compose two functions") {
            val result = divStrong("hello")
            result shouldBe "<div><strong>hello</strong></div>"
        }

        it("can compose two functions with 'forwardCompose'") {
            val result = spanP("hello")
            result shouldBe "<span><p>hello</p></span>"
        }

        it("works with 'andThen' which is an alias to 'forwardCompose'") {
            val result = divSpan("hello")
            result shouldBe "<span><div>hello</div></span>"
        }

        it("works with complex workflow") {
            val salesSystem: (Quote) -> String =
                ::calculatePrice andThen ::filterBills andThen :: splitter

            val result =
                salesSystem(Quote(20.0, "Foo", "Shoes", 1))
            result shouldBe ""

            val result2 =
                salesSystem(Quote(20.0, "Bar", "Shoes", 200))
            result2 shouldBe
                "\nProcessing order = PickingOrder Shoes - 200\nProcessing bill = Bill 4000.0 - Bar"

            val result3 =
                salesSystem(Quote(2000.0, "Foo", "Motorbike", 1))
            result3 shouldBe
                "\nProcessing order = PickingOrder Motorbike - 1\nProcessing bill = Bill 2000.0 - Foo"
        }
    }
})
