package sandbox.explorer.logic

import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.mtl.EitherT
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import sandbox.explorer.AppError
import sandbox.explorer.EitherIO
import sandbox.explorer.GitHubMetric
import sandbox.explorer.GitHubUserInfo
import sandbox.explorer.Person

object GitHubMetricConverter {
    fun convertAndSaveData(gitHubUserInfo: GitHubUserInfo, personValue: Person): EitherIO<GitHubMetric> =
        EitherT(
            IO.fx {
                transaction {
                    addLogger(StdOutSqlLogger)

                    GitHubMetric.new {
                        login = gitHubUserInfo.username
                        name = "${personValue.firstName} ${personValue.lastName}"
                        publicGistsCount = gitHubUserInfo.publicGistCount
                        publicReposCount = gitHubUserInfo.publicReposCount
                        followersCount = gitHubUserInfo.followersCount
                        followingCount = gitHubUserInfo.followingCount
                        accountCreatedAt = convertToDate(gitHubUserInfo.memberSince!!)
                        person = personValue
                    }.right()
                }
            }.handleError { err -> AppError.GitHubMetricSaveError(err.message.toString()).left() }
        )

    private fun convertToDate(dateTimeValue: LocalDateTime): DateTime {
        return DateTime(dateTimeValue.toInstant(ZoneOffset.UTC).toEpochMilli())
    }
}
