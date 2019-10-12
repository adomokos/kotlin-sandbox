package arrow.effects

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.unsafe

fun helloWorld(): String = "Hello World"

// suspend composition
suspend fun sayGoodBye(): Unit =
    println("Good bye World!")

suspend fun sayHello(): Unit =
    println(helloWorld())

fun greet(): IO<Unit> =
    IO.fx {
        !effect { sayHello() }
        !effect { sayGoodBye() }
    }
/*
	// Lazy evaluation
	IO.fx {
		val pureHello = effect { sayHello() }
		val pureGoodBye = effect { sayGoodBye() }
	}
*/

fun sayInIO(s: String): IO<Unit> =
    IO { println(s) }

fun greet2(): IO<Unit> =
    IO.fx {
        sayInIO("Hello World").bind()
    }

/*
	Executing effectful programs
	Can be: blocking and non-blocking.
	Since they have side effect -> they are unsafe.
	Usage of `unsafe` is reserved for the end of the world,
	and may be the only impure execution of a well-typed
	functional program.
*/

fun runEffects() {
    unsafe { runBlocking { greet() } }
}
