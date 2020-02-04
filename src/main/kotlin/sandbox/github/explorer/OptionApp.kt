package sandbox.github.explorer

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object OptionApp {
    sealed class AppError {
        data class UserNotFound(val errorInfo: String) : AppError()
        data class GitHubConnectionFailed(val errorInfo: String) : AppError()
        data class UserDataJsonParseFailed(val errorInfo: String) : AppError()
    }

    fun extractUserInfo(userInfoData: String): Option<UserInfo> =
        Option.fromNullable(UserInfo.deserializeFromJson(userInfoData))

    fun saveUserInfo(userInfo: UserInfo): Option<UserInfo> =
        optionSaveRecord(userInfo)

    fun addStarRating(userInfo: UserInfo): UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
    }

    fun getUserInfo(username: String): Option<UserInfo> {
        val maybeApiData = callApi(username)
        return maybeApiData.flatMap { apiData ->
            val maybeUserInfo = extractUserInfo(apiData)
            maybeUserInfo.flatMap { userInfo ->
                val userInfoData = addStarRating(userInfo)
                saveUserInfo(userInfoData)
            }
        }
    }

    fun callApi(username: String): Option<String> {
        val client = HttpClient.newBuilder().build()
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create("https://api.github.com/users/$username"))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return if (response.statusCode() == 404) {
            None
        } else {
            Some(response.body())
        }
    }

    fun run(args: Array<String>) {
        val username = args.firstOrNull()

        try {
            println(getUserInfo(username ?: "adomokos"))
        } catch (ex: Exception) {
            println("Error occurred: $ex")
        }
    }
}
