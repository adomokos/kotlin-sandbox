package sandbox.arrow

import arrow.core.Tuple2
import arrow.fx.IO
import arrow.fx.extensions.fx
import io.github.serpro69.kfaker.Faker
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

// parMapN
data class UserInfo(
    val firstName: String,
    val lastName: String
)

private val faker = Faker()

fun findUserInfo(i: Int): IO<UserInfo> =
    // Simulate DB calls
    IO.fx {
        // Thread.sleep(2_000)
        when (i) {
            1 -> {
                UserInfo("John", "Lennon")
            }
            2 -> {
                UserInfo("Paul", "McCartney")
            }
            else -> {
                UserInfo(faker.name.firstName(), faker.name.lastName())
            }
        }
    }

val program1 = IO.fx {
    val fiberA = !effect { findUserInfo(1) }.fork(dispatchers().default())
    val fiberB = !effect { findUserInfo(2) }.fork(dispatchers().default())
    val fiberC = !effect { findUserInfo(3) }.fork(dispatchers().default())
    val (userInfo1) = !fiberA.join()
    val (userInfo2) = !fiberB.join()
    val (userInfo3) = !fiberC.join()
    !effect { listOf(userInfo1, userInfo2, userInfo3) }
}

val program2 = IO.fx {
    val result =
        !dispatchers().default().parMapN(
            !effect { findUserInfo(1) },
            !effect { findUserInfo(2) },
            ::Tuple2
        )
    result
}

// parTraverse
val program3 = IO.fx {
    val result = !
        listOf(1, 2).parTraverse { i ->
            findUserInfo(i)
        }
    result
}

class ConcurrentDataSpec : DescribeSpec({
    describe("Concurrent Data Manipulation") {
        it("can fetch data concurrently with forked IO") {
            IO.fx {
                val users = !program1
                users.size shouldBe 3
            }.unsafeRunSync()
        }

        it("can fetch data with parMapN") {
            IO.fx {
                val users = !program2
                val firstNames = listOf(users.a.firstName, users.b.firstName)

                firstNames.sorted() shouldBe listOf("John", "Paul")
            }.unsafeRunSync()
        }

        it("can fetch data with parTraverse") {
            IO.fx {
                val users = !program3
                val firstNames = users.map { it.firstName }

                firstNames.sorted() shouldBe listOf("John", "Paul")
            }.unsafeRunSync()
        }
    }
})
