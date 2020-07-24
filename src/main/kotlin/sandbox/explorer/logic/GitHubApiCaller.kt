package sandbox.explorer.logic

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.mtl.EitherT
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import sandbox.explorer.AppError
import sandbox.explorer.EitherIO

object GitHubApiCaller {
    fun callApi(username: String): EitherIO<String> =
        EitherT(
            IO.fx {
                val client = HttpClient.newBuilder().build()
                val request =
                    HttpRequest
                        .newBuilder()
                        .uri(URI.create("https://api.github.com/users/$username"))
                        .build()

                val userInfoJsonData = IO.fx {
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                    processResponse(response, username)
                }.handleError { AppError.GitHubApiError("Couldn't reach github.com").left() }.bind()

                userInfoJsonData
            }
        )

    fun processResponse(response: HttpResponse<String>, username: String): Either<AppError, String> {
        if (response.statusCode() == 404) {
            return AppError.GitHubApiError("The user $username was not found on GitHub")
                .left()
        }

        if (response.statusCode() != 200) {
            return AppError.GitHubApiError("Received a status code that is not 200: ${response.statusCode()}")
                .left()
        }

        return when (val body: Option<String> = response.body().toOption()) {
            is None -> AppError.GitHubApiError("Response body was null").left()
            is Some -> body.t.right()
        }
    }
}
