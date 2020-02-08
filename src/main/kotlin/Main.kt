// import arrow.runExamples as runArrowExamples
// import sandbox.github.explorer.run as runExplorer
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import sandbox.github.explorer.NullableApp.run as runNullableApp
// import sandbox.github.explorer.EitherApp.run as runEitherApp
// import sandbox.github.explorer.EitherIOApp.run as runEitherIOApp

class Hello : CliktCommand() {
    val app: String by option(help = "App name").default("Nullable")
    val username: String by option(help = "GitHub username").default("adomokos")
    // val name: String by option(help = "The person to greet").prompt("Your name")

    override fun run() {
        when (app) {
            "Nullable" -> runNullableApp(arrayOf(username))
            else -> { // Note the block
                println("Sorry, I don't know what to do...")
            }
        }
    }
}

suspend fun main(args: Array<String>) {
    Hello().main(args)
}
