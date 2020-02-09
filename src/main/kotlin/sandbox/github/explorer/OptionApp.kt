package sandbox.github.explorer

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import sandbox.github.explorer.Entities.UserInfo

object OptionApp {
    // 1. Call GitHub, pull info about the user
    fun callApi(username: String): Option<String> {
        val client = HttpClient.newBuilder().build()
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create("${Util.gitHubUrl}/$username"))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return if (response.statusCode() == 404) {
            None
        } else {
            Some(response.body())
        }
    }

    // 2. Deserialize the JSON response into UserInfo?
    fun deserializeData(userInfoData: String): Option<UserInfo> =
        Option.fromNullable(UserInfo.deserializeFromJson(userInfoData))

    // 3. Run the transform logic
    fun addStarRating(userInfo: UserInfo): UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
    }

    // 4. Save the user in a data store
    fun saveUserInfo(userInfo: UserInfo): Option<UserInfo> =
        Util.optionSaveRecord(userInfo)

    fun getUserInfo(username: String): Option<UserInfo> =
        callApi(username)
            .flatMap(::deserializeData)
            .map(::addStarRating)
            .flatMap(::saveUserInfo)

    fun run(args: Array<String>) {
        val username = args.firstOrNull()

        try {
            println(getUserInfo(username ?: "adomokos"))
        } catch (err: Exception) {
            println("Fatal error occurred: $err")
        }
    }
}
