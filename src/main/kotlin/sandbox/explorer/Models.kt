package sandbox.explorer

import arrow.core.Left
import arrow.core.Option
import arrow.core.right
import arrow.core.toOption
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.mtl.EitherT
import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Target(AnnotationTarget.FIELD)
annotation class KlaxonDate

data class GitHubUserInfo(
    @Json(name = "login")
    var username: String,

    @Json(name = "public_gists")
    val publicGistCount: Int,

    @Json(name = "public_repos")
    val publicReposCount: Int,

    @Json(name = "followers")
    val followersCount: Int,

    @Json(name = "following")
    val followingCount: Int,

    @Json(name = "id")
    val gitHubId: Int,

    @Json(name = "created_at")
    @KlaxonDate
    val memberSince: LocalDateTime?
) {
    companion object {
        fun deserializeFromJson(userInfoData: String): Option<GitHubUserInfo> =
                createKlaxon().parse<GitHubUserInfo>(userInfoData).toOption()

        fun deserializeFromJson2(userInfoData: String): EitherIO<GitHubUserInfo> =
            EitherT(IO.fx {
                val result = createKlaxon().parse<GitHubUserInfo>(userInfoData)
                result!!.right()
            }.handleError { Left(AppError.JSONDeserializationError) })
    }
}

// ZOMG to parse 8601 UTC Date Time
fun createKlaxon() = Klaxon()
    .fieldConverter(KlaxonDate::class, object : Converter {
        override fun canConvert(cls: Class<*>) = cls == LocalDateTime::class.java

        override fun fromJson(jv: JsonValue) =
                if (jv.string != null) {
                    LocalDateTime.parse(jv.string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                } else {
                    throw KlaxonException("Couldn't parse date: ${jv.string}")
                }

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun toJson(dateValue: Any) =
                """ { "date" : $dateValue } """
    })
