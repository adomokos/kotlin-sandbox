package sandbox.github.explorer

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import sandbox.github.explorer.Entities.UserInfo

object NullableApp {
    fun extractUserInfo(userInfoData: String): UserInfo? =
        UserInfo.deserializeFromJson(userInfoData)

    fun saveUserInfo(userInfo: UserInfo): UserInfo? =
        saveRecord(userInfo)

    fun addStarRating(userInfo: UserInfo): UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
    }

    fun getUserInfo(username: String): UserInfo? {
        val apiData = callApi(username)
        val userInfo = extractUserInfo(apiData)
        val ratedUserInfo = addStarRating(userInfo!!)
        return saveUserInfo(ratedUserInfo!!)
    }

    fun callApi(username: String): String {
        val client = HttpClient.newBuilder().build()
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create("https://api.github.com/users/$username"))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
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
