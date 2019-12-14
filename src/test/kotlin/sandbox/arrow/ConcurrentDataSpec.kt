package sandbox.arrow

import arrow.core.Tuple5
import arrow.fx.IO
import arrow.fx.extensions.fx
import io.github.serpro69.kfaker.Faker
import io.kotlintest.matchers.collections.shouldContainAll
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
    val (userInfo1) = !fiberA.join()
    val (userInfo2) = !fiberB.join()
    !effect { listOf(userInfo1, userInfo2) }
}

val program2 = IO.fx {
    val result =
        !dispatchers().default().parMapN(
            findUserInfo(1),
            findUserInfo(2),
            findUserInfo(3),
            findUserInfo(4),
            findUserInfo(5),
            ::Tuple5
        )

    result
}

fun findFirstNamesFromProgram2(userInfos: Tuple5<UserInfo, UserInfo, UserInfo, UserInfo, UserInfo>): List<String> =
    listOf(userInfos.a.firstName, userInfos.b.firstName)

// parTraverse
val program3 = IO.fx {
    val result = !
        (1..16).toList().parTraverse { i ->
            findUserInfo(i)
        }
    result
}

fun firstNamesFromUserInfos(userInfos: List<UserInfo>): List<String> =
    userInfos.map { it.firstName }

class ConcurrentDataSpec : DescribeSpec({
    describe("Concurrent Data Manipulation") {
        it("can fetch data concurrently with forked IO").config(enabled = false) {
            IO.fx {
                val users = !program1
                users.size shouldBe 2
            }.unsafeRunSync()
        }

        it("can fetch data with parMapN") {
            val firstNames = program2.map(::findFirstNamesFromProgram2).unsafeRunSync()

            firstNames.sorted() shouldBe listOf("John", "Paul")
        }

        it("can fetch data with parTraverse").config(enabled = false) {
            val result =
                program3
                    .map(::firstNamesFromUserInfos)
                    .attempt()
                    .unsafeRunSync()

            result.map {
                it shouldContainAll(listOf("John", "Paul"))
            }
        }
    }
})
