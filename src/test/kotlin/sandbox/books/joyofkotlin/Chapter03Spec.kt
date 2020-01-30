package sandbox.books.joyofkotlin

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class Chapter03Spec : StringSpec() {
    data class Product(
        val name: String,
        val price: Double,
        val weight: Double
    )

    data class OrderLine(val product: Product, val count: Int) {
        fun weight() = product.weight * count
        fun amount() = product.price * count
    }

    data class Price(val value: Double) {
        operator fun plus(price: Price) =
            Price(this.value + price.value)

        operator fun times(num: Int) =
            Price(this.value * num)
    }

    data class Weight(val value: Double) {
        operator fun plus(weight: Price) =
            Price(this.value + weight.value)
    }

    val zeroPrice = Price(0.0)
    val zeroWeight = Weight(0.0)
    // val priceAddition = { x, y -> x + y }

    data class EnhancedProduct(
        val name: String,
        val price: Price,
        val weight: Weight
    )

    init {
        "calculates total price and weight incorrectly" {
            val toothPaste = Product("Tooth paste", 1.5, 0.5)
            val toothBrush = Product("Tooth brush", 3.5, 0.3)

            val orderLines = listOf(
                OrderLine(toothPaste, 2),
                OrderLine(toothBrush, 3)
            )

            val weight = orderLines.sumByDouble { it.amount() }
            val price = orderLines.sumByDouble { it.weight() }

            price shouldBe 1.9
            weight shouldBe 13.5
        }

        "calculates total price from two Price objects" {
            val totalPrice = Price(1.0) + Price(2.0)
            totalPrice shouldBe Price(3.0)

            val timesPrice = Price(3.0) * 4
            timesPrice shouldBe Price(12.0)
        }

        "calculates the correct total price and weight" {
            val toothPaste =
                EnhancedProduct("Tooth paste",
                                Price(1.5),
                                Weight(0.5))
            val toothBrush =
                EnhancedProduct("Tooth brush",
                                Price(3.5),
                                Weight(0.3))
        }
    }
}
