package sandbox.arrow.concurrent

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.unsafe
import kotlinx.coroutines.newSingleThreadContext

@kotlinx.coroutines.ObsoleteCoroutinesApi
val contextA = newSingleThreadContext("A")

suspend fun printThreadName(): Unit =
    println(Thread.currentThread().name)

@kotlinx.coroutines.ObsoleteCoroutinesApi
val program = IO.fx {
    continueOn(contextA)
    !effect { printThreadName() }
    continueOn(dispatchers().default())
    !effect { printThreadName() }
}

// Fibers
suspend fun threadName(): String =
    Thread.currentThread().name

val program2 = IO.fx {
    val fiberA = !effect { threadName() }.fork(dispatchers().default())
    val fiberB = !effect { threadName() }.fork(dispatchers().default())
    val threadA = !fiberA.join()
    val threadB = !fiberB.join()
    !effect { println(threadA) }
    !effect { println(threadB) }
}

// parMapN
data class ThreadInfo(
    val threadA: String,
    val threadB: String
)

val program3 = IO.fx {
    val (threadA: String, threadB) =
        !dispatchers().default().parMapN(
            effect { threadName() },
            effect { threadName() },
            ::ThreadInfo
        )
    !effect { println(threadA) }
    !effect { println(threadB) }
}

// parTraverse
suspend fun threadName(i: Int): String =
    "$i on ${Thread.currentThread().name}"

val program4 = IO.fx {
    val result: List<String> = !
    listOf(1, 2, 3).parTraverse { i ->
        effect { threadName(i) }
    }
    !effect { println(result) }
}

// parSequence
val program5 = IO.fx {
    val result: List<String> = !listOf(
        effect { threadName() },
        effect { threadName() },
        effect { threadName() }
    ).parSequence()

    !effect { println(result) }
}

@kotlinx.coroutines.ObsoleteCoroutinesApi
fun runConcurrent() {
    unsafe { runBlocking { program } }
    unsafe { runBlocking { program2 } }
    unsafe { runBlocking { program3 } }
    unsafe { runBlocking { program4 } }
    unsafe { runBlocking { program5 } }
}
