package sandbox.github.explorer

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import sandbox.github.explorer.Entities.UserInfo

object NullableApp {
    // 1. Call GitHub, pull info about the user
    fun callApi(username: String): String {
        val client = HttpClient.newBuilder().build()
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create("$gitHubUrl/$username"))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    // 2. Deserialize the JSON response into UserInfo?
    fun extractUserInfo(userInfoData: String): UserInfo? =
        UserInfo.deserializeFromJson(userInfoData)

    // 3. Run the transform logic
    fun addStarRating(userInfo: UserInfo): UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
    }

    // 4. Save the user in a data store
    fun saveUserInfo(userInfo: UserInfo): UserInfo? =
        saveRecord(userInfo)

    fun getUserInfo(username: String): UserInfo? {
        val apiData = callApi(username)
        val userInfo = extractUserInfo(apiData)
        val ratedUserInfo = addStarRating(userInfo!!)
        return saveUserInfo(ratedUserInfo!!)
    }

    fun run(args: Array<String>) {
        val username = args.firstOrNull()

        try {
            println(getUserInfo(username ?: "adomokos"))
        } catch (err: Exception) {
            println("Fatal error occurred: $err")
        }
    }
}
