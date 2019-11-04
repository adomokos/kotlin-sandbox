package github.explorer

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.leftIfNull
import arrow.core.right
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// https://jorgecastillo.dev/please-try-to-use-io

sealed class AppError {
    data class UserNotFound(val errorInfo: String) : AppError()
    data class UserDataJsonParseFailed(val errorInfo: String) : AppError()
}

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
)

// ZOMG to parse 8601 UTC Date Time
private fun createKlaxon() = Klaxon()
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

private fun extractUserInfo(userInfoData: String): Either<AppError, UserInfo> =
    try {
        Either.right(createKlaxon().parse<UserInfo>(userInfoData))
            .leftIfNull { AppError.UserDataJsonParseFailed("Parsed result is null") }
    } catch (ex: KlaxonException) {
        Either.left(AppError.UserDataJsonParseFailed(ex.message ?: "No message"))
    }

    /*
    Try { createKlaxon().parse<UserInfo>(userInfoData) }
        .toEither { AppError.UserDataJsonParseFailed("Couldn't parse") }
        .leftIfNull { AppError.UserDataJsonParseFailed("Parsed result is null") }
    */

private fun addStarRating(userInfo: UserInfo): UserInfo {
    if (userInfo.publicReposCount > 20) {
        userInfo.username = userInfo.username + " ⭐"
    }
    return userInfo
    // return Either.right(userInfo)
}

private fun getUserInfo(username: String): IO<Either<AppError, UserInfo>> =
    IO.fx {
        val (apiResult) = ApiClient().callApi(username)
        apiResult
            .flatMap(::extractUserInfo)
            .map(::addStarRating)
    }

class ApiClient {
    fun callApi(username: String): IO<Either<AppError, String>> {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/users/$username"))
            .build()
        val response = client.send(request, BodyHandlers.ofString())

        val result = if (response.statusCode() == 404) {
            Either.Left(AppError.UserNotFound("The user $username was not found on GitHub"))
        } else {
            response.body().right()
        }

        return IO { result }
    }
}

private fun handleAppError(error: Throwable): Unit = println("app failed \uD83D\uDCA5: $error")
private fun handleFailure(error: AppError): Unit = println("The app error is: $error")
private fun handleSuccess(userInfo: UserInfo): Unit = println("The result is: $userInfo")

fun run(args: Array<String>) {
    val username = args.firstOrNull()

    val program = getUserInfo(username ?: "adomokos")
        .map {
            result ->
                when (result) {
                    is Either.Left -> handleFailure(result.a)
                    is Either.Right -> handleSuccess(result.b)
                }
        }
        .handleError { error ->
            handleAppError(error)
        }

    // Run the program asynchronously
    program.unsafeRunAsync { result ->
        result.fold({ error -> println("Error: $error") }, {})
    }

    // program.unsafeRunSync()
}
