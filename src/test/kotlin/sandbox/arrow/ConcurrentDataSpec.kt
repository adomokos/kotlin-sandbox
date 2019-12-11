package sandbox.arrow

import arrow.fx.IO
import arrow.fx.extensions.fx
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

// parMapN
data class UserInfo(
    val firstName: String,
    val lastName: String
)

data class UserInfos(
    val userInfo1: UserInfo,
    val userInfo2: UserInfo
)

suspend fun findUserInfo(i: Int): IO<UserInfo> =
    // Simulate DB calls
    IO.fx {
        if (i == 1) {
            UserInfo("John", "Lennon")
        } else {
            UserInfo("Paul", "McCartney")
        }
    }

/*
val program3 = IO.fx {
    val (user1: UserInfo, user2: UserInfo) =
        !dispatchers().default().parMapN(
            effect { findUserInfo(1) },
            effect { findUserInfo(2) },
            ::UserInfos

        )
    !effect { println(user1) }
    // !effect { println(threadB) }
}
*/

val program2 = IO.fx {
    val fiberA = !effect { findUserInfo(1) }.fork(dispatchers().default())
    val fiberB = !effect { findUserInfo(2) }.fork(dispatchers().default())
    val (userInfo1) = !fiberA.join()
    val (userInfo2) = !fiberB.join()
    !effect { listOf(userInfo1, userInfo2) }
}

class ConcurrentDataSpec : DescribeSpec({
    describe("Concurrent Data Manipulation") {
        it("can fetch data concurrently") {
            IO.fx {
                val (users) = program2

                users.size shouldBe 2
            }
        }
    }
})
