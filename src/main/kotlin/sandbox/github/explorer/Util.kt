package sandbox.github.explorer

import arrow.core.Option
import arrow.core.Some
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import sandbox.github.explorer.Entities.UserInfo

// ZOMG to parse 8601 UTC Date Time
fun createKlaxon() = Klaxon()
    .fieldConverter(Entities.KlaxonDate::class, object : Converter {
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

fun saveRecord(userInfo: UserInfo): UserInfo? {
    println(":: Saved user info ::")
    return userInfo
}

fun optionSaveRecord(userInfo: UserInfo): Option<UserInfo> {
    println(":: Saved user info ::")
    return Some(userInfo)
}

val gitHubUrl: String =
    System.getenv("GITHUB_URL") ?: "https://api.github.com/users"
