package github.explorer

import arrow.core.Either
import arrow.core.Right
import arrow.core.leftIfNull
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import com.beust.klaxon.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// https://jorgecastillo.dev/please-try-to-use-io

sealed class AppError {
    data class UserDataJsonParseFailed(val data: String) : AppError()
}

@Target(AnnotationTarget.FIELD)
annotation class KlaxonDate

data class UserInfo(
    @Json(name = "login")
    val username: String,

    @Json(name = "public_repos")
    val publicRepos: Int,

    @Json(name ="id")
    val gitHubId: Int,

    @Json(name = "created_at")
    @KlaxonDate
    val memberSince: LocalDateTime?
)

// ZOMG to parse 8601 UTC Date Time
private fun createKlaxon() = Klaxon()
    .fieldConverter(KlaxonDate::class, object: Converter {
        override fun canConvert(cls: Class<*>) = cls == LocalDateTime::class.java

        override fun fromJson(jv: JsonValue) =
            if (jv.string != null) {
                LocalDateTime.parse(jv.string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            } else {
                throw KlaxonException("Couldn't parse date: ${jv.string}")
            }

        override fun toJson(dateValue: Any)
                = """ { "date" : $dateValue } """
    })

fun extractUserInfo(userInfoData: String): Either<AppError, UserInfo> =
    Right(createKlaxon().parse<UserInfo>(userInfoData))
        .leftIfNull { AppError.UserDataJsonParseFailed(userInfoData) }

class ApiClient {
    fun getUserInfo(username: String): IO<Either<AppError, UserInfo>> =
        IO.fx {
            val (userData) = callApi(username)
            extractUserInfo(userData)
        }

    fun callApi(username: String): IO<String> =
        IO {
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/users/$username"))
                .build()
            val response = client.send(request, BodyHandlers.ofString())
            response.body()
        }
    }

fun handleFailure(error: Throwable): Unit = println("The error is: $error")
fun handleSuccess(userInfo: UserInfo): Unit = println("The result is: $userInfo")

@Suppress("UNUSED_PARAMETER")
fun run(args: Array<String>) {
    val apiClient = ApiClient()

    val program = apiClient.getUserInfo("adomokos")
        .map { it }.map {
            result ->
                when (result) {
                    is Either.Left -> "app failed: ${result.a}"
                    is Either.Right -> handleSuccess(result.b)
                }
        }
        .handleError { error ->
            handleFailure(error)
        }

    // Run the program asynchronously
    program.unsafeRunAsync{ result ->
        result.fold({ error -> println("Error: $error") }, {} )
    }

    // program.unsafeRunSync()
}
