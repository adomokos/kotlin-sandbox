package sandbox.books.joyofkotlin.chapter09

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.IllegalStateException
import sandbox.books.joyofkotlin.chapter05.List

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
    }
}
