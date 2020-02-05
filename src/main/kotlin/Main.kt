// import arrow.runExamples as runArrowExamples
// import sandbox.github.explorer.run as runExplorer
// import sandbox.github.explorer.NullableApp.run as runNullableApp
// import sandbox.github.explorer.OptionApp.run as runOptionApp
// import sandbox.github.explorer.EitherApp.run as runEitherApp
import sandbox.github.explorer.EitherIOApp.run as runEitherIOApp

@Suppress("UNUSED_PARAMETER")
suspend fun main(args: Array<String>) {
    // runArrowExamples()
    // runExposedExamples()
    // runInactionExamples()

    // runExplorer(args)
    // runGitHubExplorer(args)
    // runNullableApp(args)
    // runOptionApp(args)
    // runEitherApp(args)
    runEitherIOApp(args)
}
