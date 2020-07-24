package sandbox.arrow

import arrow.core.Option
import arrow.core.andThen
import sandbox.arrow.effects.runEffects

fun compositionExample() {
    val add5 = { i: Int -> i + 5 }
    val multiplyBy2 = { i: Int -> i * 2 }
    val idOdd = { x: Int -> x % 2 != 0 }

    val composed: (Int) -> Option<Int> = { i: Int ->
        Option.just(i)
            .filter(idOdd)
            .map(add5.andThen(multiplyBy2))
    }

    println(composed(3))
    println(composed(4))
}

fun runExamples() {
    runEffects()
    // runDataTypes()
    // compositionExample()

    // println("-- run concurrent examples --")
    // runConcurrent()
}
