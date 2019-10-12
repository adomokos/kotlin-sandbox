package inaction.chap08

import inaction.chap08.controlflow.runExamples as runCFExamples
import inaction.chap08.lambdas.runExamples as runLambdaExamples
import inaction.chap08.returnfn.runExamples

// Higher-order functions

fun storedLambdas() {
    val sum: (Int, Int) -> Int = { x, y -> x + y }
    val action: () -> Unit = { println("hello") }

    println(sum(2, 3))
    action()
}

fun whichIsNull() {
    val canReturnNull: (Int, Int) -> Int? = { _, _ -> null }
    canReturnNull(4, 8)

    // val canBeNull: ((Int, Int) -> Int)? = null
}

fun twoAndThree(operation: (Int, Int) -> Int) {
    val result = operation(2, 3)
    println("The result is $result")
}

fun String.myFilter(predicate: (Char) -> Boolean): String {
    val sb = StringBuilder()
    for (index in 0 until length) {
        val element = get(index)
        if (predicate(element)) sb.append(element)
    }
    return sb.toString()
}

// function arguments with default values
fun <T> Collection<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    transform: (T) -> String = { it.toString() }
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(transform(element))
    }
    result.append(postfix)
    return result.toString()
}

fun runJoinToString() {
    val letters = listOf("Alpha", "Beta")
    println(letters.joinToString())
    println(letters.joinToString { it.toLowerCase() })
    println(letters.joinToString(separator = "! ",
        postfix = "! ",
        transform = { it.toUpperCase() }))
}

fun <T> Collection<T>.joinToStringNullable(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    transform: ((T) -> String)? = null
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        val str = transform?.invoke(element)
        ?: element.toString()
        result.append(str)
    }
    result.append(postfix)
    return result.toString()
}

fun runJoinToStringNullable() {
    val letters = listOf("Alpha", "Beta")
    println(letters.joinToStringNullable())
    println(letters.joinToStringNullable { it.toLowerCase() })
    println(letters.joinToStringNullable(
        separator = "! ",
        postfix = "! ",
        transform = { it.toUpperCase() }))
}

fun runChap08() {
    storedLambdas()

    twoAndThree { a, b -> a + b }
    twoAndThree { a, b -> a * b }

    println("ab1c".myFilter { it in 'a'..'z' })
    runJoinToString()
    runJoinToStringNullable()
    runExamples()
    runLambdaExamples()
    runCFExamples()
}
