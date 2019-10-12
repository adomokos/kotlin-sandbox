package inaction.chap09

/*
`Reified type parameters` allow you to refer at runtime to the
specific types used as type arguments in an inline function call.

`Declaration-site variance` lets you specify whether a generic type
with a type argument is a subtype of a supertype of another generic
type with the same base type.

Java does, Kotlin does not support raw types.
*/

fun sliceWithChar() {
    val letters = ('a'..'z').toList()
    println(letters.slice<Char>(0..2))
    println(letters.slice(10..13))
}

fun callGenericHigherOrderFunction() {
    val members = listOf("John", "Paul", "George")
    val otherMembers = mutableListOf<String>("Paul", "Linda", "Mick")

    // fun <T> List<T>.filter(predicate: (T) -> Boolean): List<T>
    println(members.filter { it !in otherMembers })
}

// Type parameter constraint
fun <T : Number> oneHalf(value: T): Double {
    return value.toDouble() / 2.0
}

fun <T : Comparable<T>> max(first: T, second: T): T {
    return if (first > second) first else second
}

// Multiple type parameters
fun <T> ensureTrailingPeriod(seq: T)
where T : CharSequence, T : Appendable {
    if (!seq.endsWith(',')) {
        seq.append('.')
    }
}

fun runTrailingDotExample() {
    val helloWorld = StringBuilder("Hello World")
    ensureTrailingPeriod(helloWorld)
    println(helloWorld)
}

// Nullable parameter type
class Processor<T> {
    fun process(value: T): Int? {
        // T is nullable, we need to call it with safe-call operator
        return value?.hashCode()
    }
}

// Non nullable parameter type
class NonNullProcessor<T : Any> { // Any guarantees to be not null
    fun process(value: T): Int {
        return value.hashCode()
    }
}

fun runProcessor() {
    val processor = Processor<String?>()
    println(processor.process("hello"))
    println(processor.process(null))

    val nonNullP = NonNullProcessor<String>()
    println(nonNullP.process("hello"))
    // println(nonNullP.process(null)) // must be not null
}

fun runChap09() {
    // val exampleList: List<String> = listOf("John", 2)
    sliceWithChar()
    callGenericHigherOrderFunction()
    println(oneHalf(3))

    println(max("kotlin", "java"))
    println(max(1, 10))
    // println(max(1, "kotlin")) // This throws an exception
    runTrailingDotExample()

    runProcessor()
}
