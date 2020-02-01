package sandbox.books.joyofkotlin

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

/*
Corecursion is composing computations steps by using the output of one step as the input of the next one, starting with
the first step.
Recursion is the same operation, but starts with the last step. A function is recursive if it calls itself as part of  a
computation, otherwise it's nto a true recursion.

This is a corecursion function:
fun hello() {
    println("Hello, World!")
    hello()
}

Unlike Java, Kotlin eliminates Tail Call Elimination (TCE).
*/

fun append(s: String, c: Char): String = "$s$c"

fun toString(list: List<Char>, s: String): String =
    if (list.isEmpty())
        s
    else
        toString(list.subList(1, list.size), append(s, list[0]))

fun tailrecToString(list: List<Char>): String {
    tailrec fun tailrecToString(list: List<Char>, s: String): String =
        if (list.isEmpty())
            s
        else
            tailrecToString(
                list.subList(1, list.size),
                append(s, list[0]))

    return tailrecToString(list, "")
}

fun prepend(c: Char, s: String): String = "$c$s"

fun toString(list: List<Char>): String {
    fun toString(list: List<Char>, s: String): String =
        if (list.isEmpty())
            s
        else
            toString(list.subList(0, list.size - 1),
                prepend(list[list.size - 1], s))
    return toString(list, "")
}

fun sum(n: Int): Int {
    var sum = 0
    var idx = 0
    while (idx <= n) {
        sum += idx
        idx += 1
    }

    return sum
}

fun sum2(n: Int): Int {
    tailrec fun sum(s: Int, i: Int): Int =
        if (i > n)
            s
        else
            sum(s + i, i + 1)

    return sum(0, 0)
}

fun inc(n: Int) = n + 1
fun dec(n: Int) = n - 1

tailrec fun add(x: Int, y: Int): Int =
    if (y == 0)
        x
    else
        add(inc(x), dec(y))

class Chapter04Spec : StringSpec() {
    init {
        "can use append to combine a list into a String" {
            val charList = listOf('a', 'b', 'c', 'd')
            val result = toString(charList, "")
            result shouldBe "abcd"

            val result2 = toString(listOf(), "")
            result2 shouldBe ""
        }

        "can use prepend to combine a list into a String" {
            val charList = listOf('a', 'b', 'c', 'd')
            val result = toString(charList)
            result shouldBe "abcd"

            val result2 = toString(listOf())
            result2 shouldBe ""
        }

        "can sum up the numbers" {
            val result = sum(10)
            result shouldBe 55

            val result2 = sum2(10)
            result2 shouldBe 55
        }

        "can add two numbers" {
            val result = add(5, 4)
            result shouldBe 9
        }
    }
}
