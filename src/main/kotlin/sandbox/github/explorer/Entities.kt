package sandbox.github.explorer

import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        override fun toString(): String =
            """UserInfo(username = ${this.username},
|         publicReposCount = ${this.publicReposCount},
|         gitHubId = ${this.gitHubId},
|         memberSince = ${this.memberSince})""".trimMargin()

        companion object {
            fun deserializeFromJson(userInfoData: String): UserInfo? {
                if (userInfoData.isBlank()) {
                    return null
                }

                return try {
                    createKlaxon().parse<UserInfo>(userInfoData)
                } catch (ex: KlaxonException) {
                    Util.printlnRed("KlaxonException: ${ex.message}")
                    null
                }
            }
        }
    }

    // ZOMG to parse 8601 UTC Date Time
    private fun createKlaxon() = Klaxon()
        .fieldConverter(
            KlaxonDate::class,
            object : Converter {
                override fun canConvert(cls: Class<*>) = cls == LocalDateTime::class.java

                override fun fromJson(jv: JsonValue) =
                    if (jv.string.isNullOrBlank()) {
                        // Log this as an error
                        throw KlaxonException("Couldn't parse date: ${jv.string}")
                    } else {
                        LocalDateTime.parse(jv.string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                    }

                @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                override fun toJson(dateValue: Any) =
                    """ { "date" : $dateValue } """
            }
        )
}
