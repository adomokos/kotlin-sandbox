package sandbox.github.explorer

import arrow.core.Either
import arrow.core.Left
import arrow.core.flatMap
import arrow.core.left
import arrow.core.leftIfNull
import arrow.core.right
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import sandbox.github.explorer.Entities.UserInfo

// https://jorgecastillo.dev/please-try-to-use-io

object EitherIOApp {
    sealed class AppError {
        data class UserNotFound(val errorInfo: String) : AppError()
        data class GitHubConnectionFailed(val errorInfo: String) : AppError()
        data class UserDataJsonParseFailed(val errorInfo: String) : AppError()
        data class UserSaveFailed(val errorInfo: String) : AppError()
    }

    // 1. Call GitHub, pull info about the user
    fun callApi(username: String): IO<Either<AppError, String>> {
        val client = HttpClient.newBuilder().build()

        val result = IO {
            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create("${Util.gitHubUrl}/$username"))
                    .build()

            val response = client.send(request, BodyHandlers.ofString())

            if (response.statusCode() == 404) {
                AppError.UserNotFound("The user $username was not found on GitHub")
                    .left()
            } else {
                response.body().right()
            }
        }.handleError { exception ->
            AppError.GitHubConnectionFailed("Couldn't reach github.com: ${exception.cause}").left()
        }

        return result
    }

    fun <E, T> runCatchingEither(onError: (err: Throwable) -> E, block: () -> Either<E, T>): Either<E, T> {
        val result = runCatching(block)

        return result.fold(
            { it },
            { err -> onError(err).left() }
        )
    }

    // 2. Deserialize the JSON response into UserInfo?
    fun extractUserInfo(userInfoData: String): Either<AppError, UserInfo> =
        runCatchingEither({ err ->
            AppError.UserDataJsonParseFailed("Error deserializing JSON to UserInfo: ${err.message}")
        }) {
                UserInfo.deserializeFromJson(userInfoData)
                    .right()
                    .leftIfNull { AppError.UserDataJsonParseFailed("Parsed result is null") }
            }

        /*
       runCatching {
            UserInfo.deserializeFromJson(userInfoData)
                .right()
                .leftIfNull { AppError.UserDataJsonParseFailed("Parsed result is null") }
        }.fold(
            { it },
            { AppError.UserDataJsonParseFailed(it.message ?: "No message").left() }
        )
    */

    // 3. Run the transform logic
    fun addStarRating(userInfo: UserInfo): UserInfo {
        if (userInfo.publicReposCount > 20) {
            userInfo.username = userInfo.username + " ⭐"
        }
        return userInfo
    }

    // 4. Save the user in a data store
    fun saveUserInfo(userInfo: UserInfo): IO<Either<AppError, UserInfo>> =
        IO {
            Util.optionSaveRecord(userInfo).toEither {
                AppError.UserSaveFailed("Couldn't save the user with the DAO")
            }
        }.handleError { Left(AppError.UserSaveFailed("Something went wrong in saveUserInfo")) }

    fun getUserInfo(username: String): IO<Either<AppError, UserInfo>> =
        callApi(username)
            .map { eitherApiResponse ->
                eitherApiResponse
                    .flatMap(::extractUserInfo)
                    .map(::addStarRating)
                    .flatMap {
                        val result = saveUserInfo(it)
                        result.unsafeRunSync()
                    }
            }

    fun getUserInfoFx(username: String): IO<Either<AppError, UserInfo>> =
        IO.fx {
            val eitherApiResponse = !callApi(username)
            val eitherUserInfo = eitherApiResponse.flatMap(::extractUserInfo)
            val userInfoWithStarRating = eitherUserInfo.map(::addStarRating)
            val result = when (userInfoWithStarRating) {
                is Either.Left -> userInfoWithStarRating
                is Either.Right -> !saveUserInfo(userInfoWithStarRating.b)
            }

            userInfoWithStarRating
        }

    fun handleAppError(error: Throwable): Unit = println("app failed \uD83D\uDCA5: $error")
    fun handleFailure(error: AppError): Unit = println("The app error is: $error")
    fun handleSuccess(userInfo: UserInfo): Unit = println("The result is: $userInfo")

    fun run(args: Array<String>) {
        val username = args.firstOrNull()

        val program = getUserInfoFx(username ?: "adomokos")
            .map { result ->
                when (result) {
                    is Either.Left -> handleFailure(result.a)
                    is Either.Right -> handleSuccess(result.b)
                }
            }
            .handleError { error ->
                handleAppError(error)
            }

        // Run the program asynchronously, handle error
        program.unsafeRunSync()
    }
}
