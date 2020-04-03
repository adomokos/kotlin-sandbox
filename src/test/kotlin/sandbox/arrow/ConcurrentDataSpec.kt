package sandbox.arrow

import arrow.core.Tuple5
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.concurrent.parMapN
import io.github.serpro69.kfaker.Faker
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers

// parMapN
data class UserInfo(
    val firstName: String,
    val lastName: String
)

private val faker = Faker()

fun findUserInfo(i: Int): IO<UserInfo> =
    // Simulate DB calls
    IO.fx {
//        Thread.sleep(300)
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
    val userInfo1 = !fiberA.join()
    val userInfo2 = !fiberB.join()
    !effect { listOf(userInfo1, userInfo2) }
}

val program2 = IO.fx {
    val result =
        !parMapN(
            Dispatchers.Default,
            findUserInfo(1),
            findUserInfo(2),
            findUserInfo(3),
            findUserInfo(4),
            findUserInfo(5),
            { (a, b, c, d, e) -> Tuple5(a, b, c, d, e) }
        )

    result
}

fun findFirstNamesFromProgram2(userInfos: Tuple5<UserInfo, UserInfo, UserInfo, UserInfo, UserInfo>): List<String> =
    listOf(userInfos.a.firstName, userInfos.b.firstName)

// parTraverse
val program3 = IO.fx {
    val result =
        !(1..15).toList().parTraverse { i ->
            findUserInfo(i)
        }
    result
}

val program4 = IO.fx {
    val result: List<UserInfo> = !listOf(
        findUserInfo(1),
        findUserInfo(2),
        findUserInfo(3)
    ).parSequence()

    result
}

fun firstNamesFromUserInfos(userInfos: List<UserInfo>): List<String> =
    userInfos.map { it.firstName }

class ConcurrentDataSpec : DescribeSpec({
    describe("Concurrent Data Manipulation") {
        it("can fetch data concurrently with forked IO").config(enabled = true) {
            IO.fx {
                val users = !program1
                users.size shouldBe 2
            }.unsafeRunSync()
        }

        it("can fetch data with parMapN").config(enabled = true) {
            val firstNames = program2.map(::findFirstNamesFromProgram2).unsafeRunSync()

            firstNames.sorted() shouldBe listOf("John", "Paul")
        }

        it("can fetch data with parTraverse").config(enabled = true) {
            val result =
                program3
                    .map(::firstNamesFromUserInfos)
                    .attempt()
                    .unsafeRunSync()

            result.map {
                it.shouldContainAll(listOf("John", "Paul"))
            }
        }

        it("can fetch data with parTraverse, described with fx").config(enabled = true) {
            IO.fx {
                val users = !program3
                val firstNames = firstNamesFromUserInfos(users)
                firstNames.shouldContainAll(listOf("John", "Paul"))
            }.unsafeRunSync()
        }

        it("can fetch data with parSequence").config(enabled = true) {
            IO.fx {
                val users = !program4
                val firstNames = firstNamesFromUserInfos(users)
                firstNames.shouldContainAll(listOf("John", "Paul"))
            }.unsafeRunSync()
        }
    }
})
