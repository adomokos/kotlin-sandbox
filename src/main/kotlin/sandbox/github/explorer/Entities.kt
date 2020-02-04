package sandbox.github.explorer

import com.beust.klaxon.Json
import java.time.LocalDateTime

object Entities {
    @Target(AnnotationTarget.FIELD)
    annotation class KlaxonDate

    data class UserInfo(
        @Json(name = "login")
        var username: String,

        @Json(name = "public_repos")
        val publicReposCount: Int,

        @Json(name = "id")
        val gitHubId: Int,

        @Json(name = "created_at")
        @KlaxonDate
        val memberSince: LocalDateTime?
    ) {
        companion object {
            fun deserializeFromJson(userInfoData: String): UserInfo? =
                createKlaxon().parse<UserInfo>(userInfoData)
        }
    }
}
