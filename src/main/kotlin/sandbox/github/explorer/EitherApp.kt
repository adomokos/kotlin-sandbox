package sandbox.github.explorer

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.leftIfNull
import arrow.core.right
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import sandbox.github.explorer.Entities.UserInfo

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

        val result = {
            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create("${Util.gitHubUrl}/$username"))
                    .build()

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
    private fun deserializeData(userInfoData: String): Either<AppError, UserInfo> =
        UserInfo.deserializeFromJson(userInfoData)
            .right()
            .leftIfNull { AppError.UserDataJsonParseFailed("Parsed result is null") }

    // 3. Run the transform logic
    private fun addStarRating(userInfo: UserInfo): UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
    }

    // 4. Save the user in a data store
    fun saveUserInfo(userInfo: UserInfo): Either<AppError, UserInfo> =
        Util.optionSaveRecord(userInfo).toEither { AppError.UserSaveFailed("Couldn't save the user with the DAO") }

    private fun getUserInfo(username: String): Either<AppError, UserInfo> =
        callApi(username)
            .flatMap(::deserializeData)
            .map(::addStarRating)
            .flatMap(::saveUserInfo)

    private fun handleFailure(resultFailure: Either<AppError, UserInfo>): Unit =
        println("The app error is: $resultFailure")
    private fun handleSuccess(resultSuccess: Either<AppError, UserInfo>): Unit =
        println("The result is: $resultSuccess")

    fun run(args: Array<String>) {
        val username = args.firstOrNull()

        try {
            val result = getUserInfo(username ?: "adomokos")

            when (result) {
                is Either.Left -> handleFailure(result)
                is Either.Right -> handleSuccess(result)
            }
        } catch (err: Exception) {
            println("Fatal error occurred: $err")
        }
    }
}
