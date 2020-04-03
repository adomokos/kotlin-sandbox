package sandbox.explorer.logic

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.core.k
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.parTraverse
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import sandbox.explorer.AppError
import sandbox.explorer.EitherIO
import sandbox.explorer.GitHubMetric
import sandbox.explorer.GitHubUserInfo
import sandbox.explorer.Person

object PeopleProcessor {
    fun processPeopleParallel(people: List<Person>): EitherIO<List<GitHubMetric>> =
        EitherT(people.k().parTraverse { aPerson ->
            val result = processPerson(aPerson).value().fix()
            result
        } // Do some type conversion gymnastics to return EitherIO<List<GitHubMetric>>
            .map { item ->
            item
                .traverse(Either.applicative()) { it }
                .fix()
                .map { it.fix().toList() }
        })

    fun processPeople(people: List<Person>): EitherIO<List<GitHubMetric>> =
        EitherT.monad<AppError, ForIO>(IO.monad()).fx.monad {
            people.map { aPerson ->
                val result = ! processPerson(aPerson)
                result
            }
        }.fix()

    fun processPerson(aPerson: Person): EitherIO<GitHubMetric> =
        GitHubApiCaller.callApi(aPerson.gitHubUsername).flatMap(IO.monad()) { gitHubInfo ->
            var result = GitHubUserInfo.deserializeFromJson2(gitHubInfo).flatMap(IO.monad()) { gitHubUserInfo ->
                GitHubMetricConverter.convertAndSaveData(gitHubUserInfo, aPerson)
            }
            result
        }
}
