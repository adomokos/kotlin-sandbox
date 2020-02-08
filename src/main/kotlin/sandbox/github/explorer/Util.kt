package sandbox.github.explorer

import arrow.core.Option
import arrow.core.Some
import sandbox.github.explorer.Entities.UserInfo

object Util {

    fun saveRecord(userInfo: UserInfo): UserInfo? {
        println(":: Saved user info ::")
        return userInfo
    }

    fun optionSaveRecord(userInfo: UserInfo): Option<UserInfo> {
        println(":: Saved user info ::")
        return Some(userInfo)
    }

    val gitHubUrl: String =
        System.getenv("GITHUB_URL") ?: "https://api.github.com/users"
}
