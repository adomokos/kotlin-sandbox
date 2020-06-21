package sandbox.books.joyofkotlin.chapter09

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.IllegalStateException
import sandbox.books.joyofkotlin.chapter05.List
import sandbox.books.joyofkotlin.chapter07.Result

/*
Notes:
* Kotlin is a strict language :-(, everything evaluated immediately.
* Function arguments are passed by value, first they are evaluated, then the evaluated value is passed.
* Kotlin uses laziness for boolean operators (|| and &&, loops and try/catch blocks.

Kotlin provides a way to implement laziness through the use of a delegate:

val first: Boolean by Delegate()
- where Delegate is a class implementing the following function:
  operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean

Kotlin also supplies standard delegates:
val first: Boolean by lazy { ... }
 */

/*
class Lazy {
    operator fun getValue(thisRef: Any?,
                          property: KProperty<*>
    ): Boolean = true
}
*/

class Lazy<out A>(function: () -> A) : () -> A {
    private val value: A by lazy(function)
    override operator fun invoke(): A = value

    // Functor
    fun <B> map(f: (A) -> B): Lazy<B> = Lazy { f(value) }

    // Monad
    fun <B> flatMap(f: (A) -> Lazy<B>): Lazy<B> = f(value)
}

// Lazy is just another computational context
private fun <A, B, C> lift2Generic(f: ((A) -> (B) -> C)): (Lazy<A>) -> (Lazy<B>) -> Lazy<C> = { ls1 ->
        { ls2 ->
            Lazy { f(ls1())(ls2()) }
        }
    }

private fun <A> sequence(lst: List<Lazy<A>>): Lazy<List<A>> =
    Lazy { lst.map { it() } }

private fun <A> sequenceResult(lst: List<Lazy<A>>): Lazy<Result<List<A>>> {
    val result = lst.map { Result.of(it) }
    val result2 = Result.sequence(result)
    return Lazy { result2 }
}

sealed class Stream<out A> {
    abstract fun isEmpty(): Boolean
    abstract fun head(): Result<A>
    abstract fun tail(): Result<Stream<A>>
    abstract fun takeAtMost(n: Int): Stream<A>
    abstract fun dropAtMost(n: Int): Stream<A>
    abstract fun toList(): List<A>
    abstract fun takeWhile(p: (A) -> Boolean): Stream<A>
    abstract fun dropWhile(p: (A) -> Boolean): Stream<A>

    private object Empty : Stream<Nothing>() {
        override fun head(): Result<Nothing> = Result()
        override fun tail(): Result<Nothing> = Result()
        override fun isEmpty(): Boolean = true
        override fun takeAtMost(n: Int): Stream<Nothing> = Empty
        override fun dropAtMost(n: Int): Stream<Nothing> = this
        override fun toList(): List<Nothing> = List()
        override fun takeWhile(p: (Nothing) -> Boolean): Stream<Nothing> = this
        override fun dropWhile(p: (Nothing) -> Boolean): Stream<Nothing> = this
    }

    private class Cons<out A> (
        internal val hd: Lazy<A>,
        internal val tl: Lazy<Stream<A>>
    ) : Stream<A>() {
        override fun head(): Result<A> = Result(hd())
        override fun tail(): Result<Stream<A>> = Result(tl())
        override fun isEmpty(): Boolean = false
        override fun takeAtMost(n: Int): Stream<A> =
            cons(hd, Lazy { tl().takeAtMost(n - 1) })
        override fun dropAtMost(n: Int): Stream<A> =
            when {
                n > 0 -> tl().dropAtMost(n - 1)
                else -> this
            }
        override fun toList(): List<A> {
            tailrec fun <A> toList(list: List<A>, stream: Stream<A>): List<A> =
                when (stream) {
                    Empty -> list
                    is Cons -> toList(list.cons(stream.hd()), stream.tl())
                }
            return toList(List(), this).reverse()
        }
        override fun takeWhile(p: (A) -> Boolean): Stream<A> =
            when {
                p(hd()) -> cons(hd, Lazy { tl().takeWhile(p) })
                else -> Empty
            }
        override fun dropWhile(p: (A) -> Boolean): Stream<A> {
            tailrec fun <A> dropWhile(stream: Stream<A>, p: (A) -> Boolean): Stream<A> =
                when (stream) {
                    is Empty -> stream
                    is Cons -> when {
                        p(stream.hd()) -> dropWhile(stream.tl(), p)
                        else -> stream
                    }
            }

            return dropWhile(this, p)
        }
    }

    companion object {
        fun <A> cons(hd: Lazy<A>, tl: Lazy<Stream<A>>): Stream<A> = Cons(hd, tl)

        operator fun <A> invoke(): Stream<A> = Empty

        fun from(i: Int): Stream<Int> =
            iterate(i) { it + 1 }

        fun <A> repeat(f: () -> A): Stream<A> =
            cons(Lazy { f() }, Lazy { repeat(f) })

        tailrec fun <A> dropAtMostTailRec(n: Int, stream: Stream<A>): Stream<A> =
            when {
                n > 0 -> when (stream) {
                    is Empty -> stream
                    is Cons -> dropAtMostTailRec(n - 1, stream.tl())
                }
                else -> stream
            }

        fun <A> iterate(seed: A, f: (A) -> A): Stream<A> =
            cons(Lazy { seed }, Lazy { iterate(f(seed), f) })
    }
}

class LazinessSpec : StringSpec() {
    init {
        "will throw exception as arguments evaluated before passed to function" {
            val getFirst: () -> Boolean = { true }
            val getSecond: () -> Boolean = { throw IllegalStateException() }

            val result = getFirst() || getSecond()
            result shouldBe true

            fun or(a: Boolean, b: Boolean): Boolean =
                if (a) true else b
            fun and(a: Boolean, b: Boolean): Boolean =
                if (a) b else false

            shouldThrow<IllegalStateException> {
                or(getFirst(), getSecond())
            }

            shouldThrow<IllegalStateException> {
                and(getFirst(), getSecond())
            }
        }

        "can work with a Lazy type, no exception is thrown as fn is lazy" {
            fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean =
                if (a()) true else b()

            val first = Lazy { true }
            val second = Lazy { throw IllegalStateException() } as Lazy<Boolean>

            val result1 = first() || second()
            val result2 = first() || second()
            val result3 = or(first, second)

            result1 shouldBe true
            result2 shouldBe true
            result3 shouldBe true
        }

        "constructs messages with Lazy values" {
            fun constructMessage(
                greetings: Lazy<String>,
                name: Lazy<String>
            ): Lazy<String> =
                Lazy { "${greetings()}, ${name()}!" }

            val greetings = Lazy { "Hello" }
            val name1: Lazy<String> = Lazy { "Mickey" }
            val name2: Lazy<String> = Lazy { "Donald" }
            val defaultMessage: Lazy<String> = Lazy { "No greetings at the moment!" }

            val message1 = constructMessage(greetings, name1)
            val message2 = constructMessage(greetings, name2)

            val result1: String = if (true) message1() else message2()
            result1 shouldBe "Hello, Mickey!"

            val result2: String = if (false) message1() else defaultMessage()
            result2 shouldBe "No greetings at the moment!"

            val curriedConstructMessage: (Lazy<String>) -> (Lazy<String>) -> Lazy<String> =
                { greetingsInput ->
                    { name ->
                        Lazy { "${greetingsInput()}, ${name()}!" }
                    }
                }

            val message3 = curriedConstructMessage(greetings)(name1)
            message3() shouldBe "Hello, Mickey!"
        }

        "can lift2 the arguments into the Lazy context" {
            val consMessage: (String) -> (String) -> String =
                { greetings ->
                    { name ->
                        "$greetings, $name!"
                    }
                }

            val lift2: ((String) -> (String) -> String) ->
            (Lazy<String>) ->
                (Lazy<String>) -> Lazy<String> =
                { f ->
                    { ls1 ->
                        { ls2 ->
                            Lazy { f(ls1())(ls2()) }
                        }
                    }
                }

            val result = lift2(consMessage)(Lazy { "Hello" })(Lazy { "World" })()
            val result2 = lift2Generic(consMessage)(Lazy { "Hello" })(Lazy { "World" })()

            result shouldBe "Hello, World!"
            result2 shouldBe "Hello, World!"
        }

        "can use map as functor on Lazy" {
            val greets: (String) -> String = { "Hello, $it!" }

            val name: Lazy<String> = Lazy { "Mickey" }

            val result = name.map(greets)()
            result shouldBe "Hello, Mickey!"
        }

        "can use flatMap as monad on Lazy" {
            val greets: (String) -> Lazy<String> = { Lazy { "Hello, $it!" } }

            val name: Lazy<String> = Lazy { "John" }

            name.flatMap(greets)() shouldBe "Hello, John!"
        }

        "can use sequence operation on Lazy" {
            val name1: Lazy<String> = Lazy { "Mickey" }
            val name2: Lazy<String> = Lazy { "Donald" }
            val name3: Lazy<String> = Lazy { "Goofy" }

            val list = sequence(List(name1, name2, name3))()

            list.toString() shouldBe "[Mickey, Donald, Goofy, NIL]"
        }

        "creates a Result list from a Lazy sequence" {
            val name1: Lazy<String> = Lazy { "Mickey" }
            val name2: Lazy<String> = Lazy { "Donald" }
            val name3: Lazy<String> = Lazy { "Goofy" }
            val name4 = Lazy {
                throw IllegalStateException("Exception while evaluating name4")
            }

            val list1 = sequenceResult(List(name1, name2, name3))
            val list2 = sequenceResult(List(name1, name2, name3, name4))

            list1().toString() shouldBe "Success([Mickey, Donald, Goofy, NIL])"
            list2().toString() shouldBe "Failure(java.lang.IllegalStateException: Exception while evaluating name4)"
        }

        "works lazily with Streams" {
            val stream = Stream.from(1)
            val list = mutableListOf<Int>()
            stream.head().forEach { list.add(it) }
            stream.tail().flatMap { it.head() }.forEach { list.add(it) }
            stream.tail().flatMap {
                it.tail().flatMap { it.head() }
            }.forEach { list.add(it) }

            list shouldBe listOf(1, 2, 3)
        }

        "repeat provides the same items in a stream" {
            val stream = Stream.repeat { 2 }
            stream.head() shouldBe Result(2)

            val nextItem = stream.tail().flatMap { it.head() }
            nextItem shouldBe Result(2)
        }

        "takeAtMost(n) behaves like take, but lazy" {
            val stream = Stream.from(1)
            val result = stream.takeAtMost(3)
            result.head() shouldBe Result(1)

            val nextItem = stream.tail()
            nextItem.flatMap { it.head() } shouldBe Result(2)

            val thirdItem = nextItem.flatMap { it.tail() }
            thirdItem.flatMap { it.head() } shouldBe Result(3)
        }

        "dropAtMost(n) behaves like drop, but lazy" {
            val stream = Stream.from(2)
            val result = stream.dropAtMost(3)
            result.head() shouldBe Result(5)

            val result2 = Stream.dropAtMostTailRec(50000, stream)
            result2.head() shouldBe Result(50002)
        }

        "toList uses tailrec to convert a stream to list safely" {
            val result = Stream.dropAtMostTailRec(60, Stream.from(0)).takeAtMost(60)
            result.head() shouldBe Result(60)
        }

        "takeWhile extracts values until the condition is true" {
            val stream = Stream.from(2)
            val result = stream.takeWhile { it < 10 }
            result.head() shouldBe Result(2)
            val list = result.toList()
            list.lengthMemoized() shouldBe 8
        }

        "dropWhile skips values until the condition is true" {
            val stream = Stream.from(2)
            val result = stream.dropWhile { it < 10 }
            result.head() shouldBe Result(10)
        }
    }
}
