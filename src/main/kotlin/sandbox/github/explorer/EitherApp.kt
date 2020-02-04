package sandbox.github.explorer

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.leftIfNull
import arrow.core.right
import arrow.fx.IO
import arrow.fx.handleError
import com.beust.klaxon.KlaxonException
import java.net.ConnectException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import sandbox.github.explorer.Entities.UserInfo

// https://jorgecastillo.dev/please-try-to-use-io

object EitherApp {
    sealed class AppError {
        data class UserNotFound(val errorInfo: String) : AppError()
        data class GitHubConnectionFailed(val errorInfo: String) : AppError()
        data class UserDataJsonParseFailed(val errorInfo: String) : AppError()
    }

    private fun extractUserInfo(userInfoData: String): Either<AppError, UserInfo> =
        try {
            UserInfo.deserializeFromJson(userInfoData)
                .right()
                .leftIfNull { AppError.UserDataJsonParseFailed("Parsed result is null") }
        } catch (ex: KlaxonException) {
            AppError.UserDataJsonParseFailed(ex.message ?: "No message").left()
        }

    private fun addStarRating(userInfo: UserInfo): UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
        // return Either.right(userInfo)
    }

    private fun getUserInfo(username: String): IO<Either<AppError, UserInfo>> =
        callApi(username)
            .map {
                it.flatMap(::extractUserInfo)
                    .map(::addStarRating)
            }

    private fun callApi(username: String): IO<Either<AppError, String>> {
        val client = HttpClient.newBuilder().build()
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create("https://api.github.com/users/$username"))
                .build()

        val result = try {
            val response = client.send(request, BodyHandlers.ofString())

            if (response.statusCode() == 404) {
                AppError.UserNotFound("The user $username was not found on GitHub")
                    .left()
            } else {
                response.body().right()
            }
        } catch (_: ConnectException) {
            AppError.GitHubConnectionFailed("Couldn't reach github.com").left()
        }

        return IO { result }
    }

    private fun handleAppError(error: Throwable): Unit = println("app failed \uD83D\uDCA5: $error")
    private fun handleFailure(error: AppError): Unit = println("The app error is: $error")
    private fun handleSuccess(userInfo: UserInfo): Unit = println("The result is: $userInfo")

    fun run(args: Array<String>) {
        val username = args.firstOrNull()

        val program = getUserInfo(username ?: "adomokos")
            .map { result ->
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
    }
}
