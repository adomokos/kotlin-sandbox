package sandbox.explorer.logic

import arrow.core.k
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.parTraverse
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import sandbox.explorer.AppError
import sandbox.explorer.EitherIO
import sandbox.explorer.GitHubMetric
import sandbox.explorer.GitHubUserInfo
import sandbox.explorer.Person

object PeopleProcessor {
    fun processPeopleParallel(people: List<Person>) =
        people.k().parTraverse { aPerson ->
            val result = processPerson(aPerson).value().fix()
            result
        }

    fun processPeople(people: List<Person>) =
        EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
            people.map { aPerson ->
                val result = ! processPerson(aPerson) // .value().fix()
                result
            }
        }

    fun processPerson(aPerson: Person): EitherIO<GitHubMetric> =
        GitHubApiCaller.callApi(aPerson.gitHubUsername).flatMap(IO.monad()) { gitHubInfo ->
            var result = GitHubUserInfo.deserializeFromJson2(gitHubInfo).flatMap(IO.monad()) { gitHubUserInfo ->
                GitHubMetricConverter.convertAndSaveData(gitHubUserInfo, aPerson)
            }
            result
        }
}
