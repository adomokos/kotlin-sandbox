package sandbox.books.joyofkotlin.chapter04

import arrow.core.NonEmptyList
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

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
fun <T, U> foldLeft(list: List<T>, z: U, f: (U, T) -> U): U {
    tailrec fun foldLeft(list: List<T>, acc: U): U =
        if (list.isEmpty())
            acc
        else
            foldLeft(list.tail(), f(acc, list.head()))

    return foldLeft(list, z)
}

fun <T> List<T>.head(): T =
    if (this.isEmpty())
        throw IllegalArgumentException("head called on empty list")
    else
        this[0]

fun <T> List<T>.tail(): List<T> =
    if (this.isEmpty())
        throw IllegalArgumentException("tail called on empty list")
    else
        this.drop(1)

class RecursionSpec : StringSpec() {
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
                    append(s, list[0])
                )

        return tailrecToString(list, "")
    }

    fun prepend(c: Char, s: String): String = "$c$s"

    fun toString(list: List<Char>): String {
        fun toString(list: List<Char>, s: String): String =
            if (list.isEmpty())
                s
            else
                toString(
                    list.subList(0, list.size - 1),
                    prepend(list[list.size - 1], s)
                )
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
    // Sum list by recursion

    fun recursiveSum(list: List<Int>): Int =
        if (list.isEmpty())
            0
        else
            list.head() + recursiveSum(list.tail())

    // Or with Arrow's NonEmptyList
    fun nolRecursiveSum(list: NonEmptyList<Int>): Int =
        if (list.size == 1)
            list.head
        else
            list.head + nolRecursiveSum(NonEmptyList.fromListUnsafe(list.tail))

    fun tailrecSum(list: List<Int>): Int {
        tailrec fun sumTail(list: List<Int>, acc: Int): Int =
            if (list.isEmpty())
                acc
            else
                sumTail(list.tail(), acc + list.head())

        return sumTail(list, 0)
    }

    fun sumInts(list: List<Int>) = foldLeft(list, 0, Int::plus)
    fun stringMaker(list: List<Char>) = foldLeft(list, "", String::plus)
    fun <T> makeString(list: List<T>, delim: String) =
        foldLeft(list, "") { s, t ->
            if (s.isEmpty()) "$t" else "$s$delim$t"
        }

    fun <T> prepend(list: List<T>, elem: T): List<T> =
        listOf(elem) + list

    fun <T> reverse(list: List<T>): List<T> =
        foldLeft(list, listOf(), ::prepend)

    fun rangeMaker(start: Int, end: Int): List<Int> {
        val result: MutableList<Int> = mutableListOf()
        var index = start
        while (index < end) {
            result.add(index)
            index++
        }
        return result
    }

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

        "can recursively sum list" {
            val list = listOf(1, 2, 3, 4)
            recursiveSum(list) shouldBe 10

            val nolList = NonEmptyList.of(1, 2, 3, 4)
            nolRecursiveSum(nolList) shouldBe 10

            tailrecSum(list) shouldBe 10
        }

        "can use generic foldLeft" {
            val list = listOf(1, 2, 3, 4)
            sumInts(list) shouldBe 10

            val chars = listOf('h', 'e', 'l', 'l', 'o')
            stringMaker(chars) shouldBe "hello"

            makeString(list, ",") shouldBe "1,2,3,4"
        }

        "can reverse a list" {
            val list = listOf(1, 2, 3, 4)
            reverse(list) shouldBe listOf(4, 3, 2, 1)
        }

        "can create a range" {
            val range = rangeMaker(2, 6)

            range shouldBe listOf(2, 3, 4, 5)
        }
    }
}
