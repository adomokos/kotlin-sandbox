package github.explorer

import arrow.fx.IO
import arrow.fx.handleError
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

// https://jorgecastillo.dev/please-try-to-use-io

sealed class AppError {
    data class RequestFailed(val username: String) : AppError()
}

class ApiClient {
    fun getUserInfo(username: String): IO<String> =
        callApi(username)

    fun callApi(username: String): IO<String> =
        IO {
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/users/$username"))
                .build()
            val response = client.send(request, BodyHandlers.ofString())
            response.body()
        }
    }

fun handleFailure(error: Throwable): Unit = println("The error is: $error")
fun handleSuccess(apiResult: String): Unit = println("The result is: $apiResult")

@Suppress("UNUSED_PARAMETER")
fun run(args: Array<String>) {
    val apiClient = ApiClient()

    val program = apiClient.getUserInfo("adomokos")
        .map { it.toUpperCase() }.map {
            result -> handleSuccess(result)
        }
        .handleError { error ->
            handleFailure(error)
        }

    // Run the program asynchronously
    /*
    program.unsafeRunAsync{ result ->
        result.fold({ error -> println("Error: $error") }, { println(it) })
    }
    */

    program.unsafeRunSync()
}
