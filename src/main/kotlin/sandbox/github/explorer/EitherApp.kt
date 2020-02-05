package sandbox.github.explorer

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.leftIfNull
import arrow.core.right
import com.beust.klaxon.KlaxonException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object EitherApp {
    sealed class AppError {
        data class UserNotFound(val errorInfo: String) : AppError()
        data class GitHubConnectionFailed(val errorInfo: String) : AppError()
        data class UserDataJsonParseFailed(val errorInfo: String) : AppError()
        data class UserSaveFailed(val errorInfo: String) : AppError()
    }

    // 1. Call GitHub, pull info about the user
    private fun callApi(username: String): Either<AppError, String> {
        val client = HttpClient.newBuilder().build()
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create("${getGitHubUrl()}/$username"))
                .build()

        val result = {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 404) {
                AppError.UserNotFound("The user $username was not found on GitHub")
                    .left()
            } else {
                response.body().right()
            }
        }()

        return result
    }

    // 2. Deserialize the JSON response into UserInfo?
    private fun extractUserInfo(userInfoData: String): Either<AppError, Entities.UserInfo> =
        try {
            Entities.UserInfo.deserializeFromJson(userInfoData)
                .right()
                .leftIfNull { AppError.UserDataJsonParseFailed("Parsed result is null") }
        } catch (ex: KlaxonException) {
            AppError.UserDataJsonParseFailed(ex.message ?: "No message").left()
        }

    // 3. Run the transform logic
    private fun addStarRating(userInfo: Entities.UserInfo): Entities.UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
    }

    // 4. Save the user in a data store
    fun saveUserInfo(userInfo: Entities.UserInfo): Either<AppError, Entities.UserInfo> =
        optionSaveRecord(userInfo).toEither { AppError.UserSaveFailed("Couldn't save the user with the DAO") }

    private fun getUserInfo(username: String): Either<AppError, Entities.UserInfo> =
        callApi(username)
            .flatMap(::extractUserInfo)
            .map(::addStarRating)
            .flatMap(::saveUserInfo)


    private fun handleAppError(error: Throwable): Unit = println("app failed \uD83D\uDCA5: $error")
    private fun handleFailure(error: AppError): Unit = println("The app error is: $error")
    private fun handleSuccess(userInfo: Entities.UserInfo): Unit = println("The result is: $userInfo")

    fun run(args: Array<String>) {
        val username = args.firstOrNull()

        try {
            val result = getUserInfo(username ?: "adomokos")

            when (result) {
                is Either.Left -> handleFailure(result.a)
                is Either.Right -> handleSuccess(result.b)
            }
        } catch (err: Exception) {
            println("Fatal error occurred: $err")
        }
    }
}
