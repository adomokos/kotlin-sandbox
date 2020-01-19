package sandbox.explorer.logic

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import sandbox.explorer.AppError
import sandbox.explorer.GitHubMetric
import sandbox.explorer.GitHubUserInfo
import sandbox.explorer.Person

object GitHubMetricConverter {
    fun convertAndSaveData(gitHubUserInfo: GitHubUserInfo, personValue: Person): IO<Either<AppError, GitHubMetric>> =
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
                    accountCreatedAt = DateTime(2020, 1, 1, 12, 0, 0)
                    person = personValue
                }.right()
            }
        }.handleError { err -> AppError.GitHubMetricSaveError(err.message.toString()).left() }
}
