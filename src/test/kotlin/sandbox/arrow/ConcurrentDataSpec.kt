package sandbox.arrow

import arrow.core.Tuple2
import arrow.fx.IO
import arrow.fx.extensions.fx
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

// parMapN
data class UserInfo(
    val firstName: String,
    val lastName: String
)

fun findUserInfo(i: Int): IO<UserInfo> =
    // Simulate DB calls
    IO.fx {
        if (i == 1) {
            UserInfo("John", "Lennon")
        } else {
            UserInfo("Paul", "McCartney")
        }
    }

val program1 = IO.fx {
    val fiberA = !effect { findUserInfo(1) }.fork(dispatchers().default())
    val fiberB = !effect { findUserInfo(2) }.fork(dispatchers().default())
    val (userInfo1) = !fiberA.join()
    val (userInfo2) = !fiberB.join()
    !effect { listOf(userInfo1, userInfo2) }
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
                val (users) = program1

                users.size shouldBe 2
            }
        }

        it("can fetch data with parMapN") {
            IO.fx {
                val (users) = program2
                val firstNames = listOf(users.a.firstName, users.b.lastName)

                firstNames.sorted() shouldBe listOf("John", "Paul")
            }
        }

        it("can fetch data with parTraverse") {
            IO.fx {
                val (users) = program3
                val firstNames = users.map { it.firstName }

                firstNames.sorted() shouldBe listOf("John", "Paul")
            }
        }
    }
})
