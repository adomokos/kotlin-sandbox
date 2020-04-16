package sandbox.explorer.logic

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.concurrent.parTraverse
import sandbox.explorer.AppError
import sandbox.explorer.GitHubMetric
import sandbox.explorer.GitHubUserInfo
import sandbox.explorer.Person

object PeopleProcessor {
    fun processPeopleParallel(people: List<Person>): IO<Either<AppError, List<GitHubMetric>>> =
        people.parTraverse { aPerson ->
            val result = processPerson(aPerson)
            result
        }.map { item ->
            item.traverse(Either.applicative()) { it }.fix().map { it.fix().toList() }
        }

    fun processPeople(people: List<Person>): IO<Either<AppError, List<GitHubMetric>>> =
        IO.fx {
            people.map { aPerson ->
                val result = ! processPerson(aPerson)
                result
            }.traverse(Either.applicative()) { it }.fix().map { it.fix().toList() }
        }

    fun processPerson(aPerson: Person): IO<Either<AppError, GitHubMetric>> =
        IO.fx {
            val eitherGHInfo = ! GitHubApiCaller.callApi(aPerson.gitHubUsername)

            val eitherUserInfo = when (eitherGHInfo) {
                is Either.Left -> eitherGHInfo
                is Either.Right -> ! GitHubUserInfo.deserializeFromJson2(eitherGHInfo.b)
            }

            val result = when (eitherUserInfo) {
                is Either.Left -> eitherUserInfo
                is Either.Right -> ! GitHubMetricConverter.convertAndSaveData(eitherUserInfo.b, aPerson)
            }

            result
        }
}
