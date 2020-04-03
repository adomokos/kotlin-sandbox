package sandbox.explorer

import arrow.core.Left
import arrow.fx.fix
import com.beust.klaxon.KlaxonException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import java.io.File

class ModelsSpec : StringSpec({
    "can deserialize a UserInfo from json string" {
        val userInfoData: String = File("./resources/github-user-info.json").readText(Charsets.UTF_8)

        val userInfo = GitHubUserInfo.deserializeFromJson(userInfoData)

        userInfo.map { it.username shouldBe "adomokos" }
    }

    "won't work with invalid data" {
        val exception = shouldThrow<KlaxonException> {
            GitHubUserInfo.deserializeFromJson("something")
        }
        exception.message should startWith("Unexpected character at position 0: 's'")
    }

    "can deserialize a UserInfo from json string with Either returned type" {
        val userInfoData: String = File("./resources/github-user-info.json").readText(Charsets.UTF_8)

        val userInfo = GitHubUserInfo.deserializeFromJson2(userInfoData).value().fix().unsafeRunSync()

        userInfo.map { it.username shouldBe "adomokos" }
    }

    "returns Left if any error occurs" {
        val userInfo =
            GitHubUserInfo.deserializeFromJson2("something").value().fix().unsafeRunSync()

        userInfo shouldBe Left(AppError.JSONDeserializationError)
    }
})
