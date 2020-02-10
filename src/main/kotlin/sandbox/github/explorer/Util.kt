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

    object TerminalColors {
        const val ANSI_RESET = "\u001B[0m"
        const val ANSI_BLACK = "\u001B[30m"
        const val ANSI_RED = "\u001B[31m"
        const val ANSI_GREEN = "\u001B[32m"
        const val ANSI_YELLOW = "\u001B[33m"
        const val ANSI_BLUE = "\u001B[34m"
        const val ANSI_PURPLE = "\u001B[35m"
        const val ANSI_CYAN = "\u001B[36m"
        const val ANSI_WHITE = "\u001B[37m"
    }

    fun printlnGreen(input: Any?): Unit =
        println("${TerminalColors.ANSI_GREEN}$input${TerminalColors.ANSI_RESET}")

    fun printlnRed(input: Any?): Unit =
        println("${TerminalColors.ANSI_RED}$input${TerminalColors.ANSI_RESET}")

    fun printlnYellow(input: Any?): Unit =
        println("${TerminalColors.ANSI_YELLOW}$input${TerminalColors.ANSI_RESET}")
}
