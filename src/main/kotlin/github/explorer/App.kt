package github.explorer

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.leftIfNull
import arrow.core.right
import arrow.fx.IO
import arrow.fx.handleError
import com.beust.klaxon.Json
import com.beust.klaxon.KlaxonException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDateTime

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
    ApiClient().callApi(username).map {
        it.flatMap(::extractUserInfo)
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
