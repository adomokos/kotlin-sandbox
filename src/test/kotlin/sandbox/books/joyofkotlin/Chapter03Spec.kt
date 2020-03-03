package sandbox.books.joyofkotlin

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

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

        companion object {
            val identity = Price(0.0)

            operator fun invoke(value: Double) =
                if (value > 0)
                    Price(value)
                else
                    throw IllegalArgumentException(
                        "Price must be positive or null"
                    )
        }
    }

    data class Weight(val value: Double) {
        operator fun plus(weight: Weight) =
            Weight(this.value + weight.value)

        operator fun times(num: Int) =
            Weight(this.value * num)

        companion object {
            val identity = Weight(0.0)

            operator fun invoke(value: Double) =
                if (value > 0)
                    Weight(value)
                else
                    throw IllegalArgumentException(
                        "Price must be positive or null"
                    )
        }
    }
    // val priceAddition = { x, y -> x + y }

    data class EnhancedProduct(
        val name: String,
        val price: Price,
        val weight: Weight
    )

    data class EnhancedOrderLine(val product: EnhancedProduct, val count: Int) {
        fun weight() = product.weight * count
        fun amount() = product.price * count
    }

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

            val orderLines = listOf(
                EnhancedOrderLine(toothPaste, 2),
                EnhancedOrderLine(toothBrush, 3)
            )

            val weight: Weight = orderLines.fold(Weight.identity) {
                    a, b -> a + b.weight()
            }

            val price: Price = orderLines.fold(Price.identity) {
                    a, b -> a + b.amount()
            }

            weight shouldBe Weight(1.9)
            price shouldBe Price(13.5)
        }
    }
}
