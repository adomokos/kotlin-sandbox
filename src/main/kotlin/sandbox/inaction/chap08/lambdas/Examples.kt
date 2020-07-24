package sandbox.inaction.chap08.lambdas

data class SiteVisit(
    val path: String,
    val duration: Double,
    val os: OS
)

enum class OS { WINDOWS, LINUX, MAC, IOS, ANDROID }

val log = listOf(
    SiteVisit("/", 34.0, OS.WINDOWS),
    SiteVisit("/", 22.0, OS.MAC),
    SiteVisit("/login", 12.0, OS.WINDOWS),
    SiteVisit("/signup", 8.0, OS.IOS),
    SiteVisit("/", 16.3, OS.ANDROID)
)

fun List<SiteVisit>.averageDurationFor(os: OS) =
    filter { it.os == os }.map(SiteVisit::duration).average()

fun List<SiteVisit>.averageDurationFor2(
    predicate: (SiteVisit) -> Boolean
) =
    filter(predicate).map(SiteVisit::duration).average()

fun runExamples() {
    val averageWindowsDuration =
        log.averageDurationFor(OS.WINDOWS)
    println("Average windows duration: $averageWindowsDuration")

    val averageMacDuration =
        log.averageDurationFor2 { it.os == OS.MAC }
    println("Average mac duration: $averageMacDuration")

    val averageComplexDuration =
        log.averageDurationFor2 {
            it.os == OS.WINDOWS && it.path == "/login"
        }
    println("Average complex duration: $averageComplexDuration")
}
