package sandbox.books.joyofkotlin.chapter04

import arrow.syntax.collections.tail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.IllegalArgumentException
import java.math.BigInteger

class MemoizationSpec : StringSpec() {
    fun fibo(limit: Int): String =
        when {
            limit < 1 -> throw IllegalArgumentException()
            limit == 1 -> "1"
            else -> {
                var fibo1 = BigInteger.ONE
                var fibo2 = BigInteger.ONE
                var fibonacci: BigInteger
                var builder = StringBuilder("1,1")
                for (i in 2 until limit) {
                    fibonacci = fibo1.add(fibo2)
                    builder.append(",").append(fibonacci) // Accumulates the present result in the accumulator
                    fibo1 = fibo2 // Stores f(n-1) for the next pass
                    fibo2 = fibonacci // Stores f(n) for the next pass
                }
                builder.toString()
            }
        }

    private fun <T> makeString(list: List<T>, separator: String): String =
        when {
            list.isEmpty() -> ""
            list.tail().isEmpty() -> list.first().toString()
            else -> list.first().toString() + foldLeft(list.tail(), "") {
                    x, y -> x + separator + y
            }
        }

    fun fibo2(number: Int): String {
        tailrec fun fibo(
            acc: List<BigInteger>,
            acc1: BigInteger,
            acc2: BigInteger,
            x: BigInteger
        ): List<BigInteger> =
            when (x) {
                BigInteger.ZERO -> acc
                BigInteger.ONE -> acc + (acc1 + acc2)
                else -> fibo(acc + (acc1 + acc2), acc2, acc1 + acc2, x - BigInteger.ONE)
            }
        val list = fibo(listOf(),
                                        BigInteger.ONE,
                                        BigInteger.ZERO,
                                        BigInteger.valueOf(number.toLong()))
        return makeString<BigInteger>(list, ", ")
    }

    init {
        "can keep calculated values" {
            val result = fibo(5)

            result shouldBe "1,1,2,3,5"
        }

        "can calculate list of Ints and then convert to string" {
            val result = fibo2(5)

            result shouldBe "1, 1, 2, 3, 5"
        }
    }
}
