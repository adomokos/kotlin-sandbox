package sandbox.arrow.datatypes

import arrow.core.Either
import arrow.core.Eval
import arrow.core.Id
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.filterOrElse

fun playWithId() {
    val id = Id("foo")

    println(id.extract())
}

// Option is like Maybe
fun playWithOption() {
    val myFactory: Option<Int> = Some(42)
    val myConstructor = Option(42)
    val emptyOptional = Option.empty<Int>()
    val fromNullable = Option.fromNullable(null)

    when (myFactory) {
        is Some -> println(myFactory.t)
        is None -> println("value not found")
    }

    println(myFactory == myConstructor)
    println(emptyOptional == fromNullable)
}

// Either
fun playWithEither() {
    val rightOnly: Either<String, Int> = Either.right(42)
    val leftOnly: Either<String, Int> = Either.left("foo")

    println(rightOnly.isRight())
    println(leftOnly.isLeft())
    when (rightOnly) {
        is Either.Left -> println("found a left value")
        is Either.Right -> println("found a right value ${rightOnly.b}")
    }
}

// Eval - is a Monad designed to control the evaluation of options
fun playWithEval() {
    val now = Eval.now(1)
    var counter: Int = 0

    val map = now.map { x -> counter++; x + 1 }
    println(counter)
    val extract = map.value()
    println(extract)
    println(counter)
}

// Using the Option type
fun parseInput(s: String): Option<Int> = Option.fromNullable(s.toIntOrNull())

fun isEven(x: Int): Boolean = x % 2 == 0
fun divideByTwo(x: Int): Int = x / 2
fun squareNumber(x: Int): Int = x * x

fun computeWithOption(input: String): String {
    val result = parseInput(input)
        .filter(::isEven)
        .map(::divideByTwo)
        .map(::squareNumber)

        return when (result) {
            is None -> "Not an even number"
            is Some -> "The result of the number is ${result.t}"
        }
}

// Using the same example, but with Either
// Define an error type
sealed class ComputeProblem {
    object OddNumber : ComputeProblem()
    object NotANumber : ComputeProblem()
}

fun parseInputAsEither(s: String): Either<ComputeProblem, Int> =
    Either.cond(s.toIntOrNull() != null, { -> s.toInt() }, { -> ComputeProblem.NotANumber })

fun computeWithEither(input: String): Either<ComputeProblem, Int> {
    return parseInputAsEither(input)
        .filterOrElse(::isEven) { -> ComputeProblem.OddNumber }
        .map(::divideByTwo)
        .map(::squareNumber)
}

fun extractAnswer(result: Either<ComputeProblem, Int>): String {
    return when (result) {
        is Either.Right -> "The result is ${result.b}"
        is Either.Left -> when (result.a) {
            is ComputeProblem.NotANumber -> "Wrong input! Not a number!"
            is ComputeProblem.OddNumber -> "It is an odd number!"
        }
    }
}

fun runDataTypes() {
    playWithId()
    playWithOption()
    playWithEither()
    playWithEval()
    println(computeWithOption("8"))
    println(computeWithOption("a"))
    println(extractAnswer(computeWithEither("8")))
    println(extractAnswer(computeWithEither("7")))
    println(extractAnswer(computeWithEither("a")))
}
