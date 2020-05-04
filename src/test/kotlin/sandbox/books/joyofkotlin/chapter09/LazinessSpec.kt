package sandbox.books.joyofkotlin.chapter09

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.lang.IllegalStateException

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
    }
}
