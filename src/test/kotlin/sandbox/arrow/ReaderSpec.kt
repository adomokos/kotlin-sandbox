package sandbox.arrow

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.extensions.fx
import arrow.core.value
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.mtl.Reader
import arrow.mtl.ReaderApi
import arrow.mtl.fix
import arrow.mtl.map
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ReaderSpec : StringSpec() {
    object AppError

    data class GetAppContext(
        val number: String
    )

    private fun one(): IO<Either<AppError, Int>> =
        IO { Right(1) }
    private fun two(inputString: String): IO<Either<AppError, String>> =
        IO { Right(inputString) }
    private fun toInt(str: String): IO<Either<AppError, Int>> =
        IO { Right(str.toInt()) }.handleError { Left(AppError) }

    private fun myApp(): Reader<GetAppContext, IO<Either<AppError, Int>>> =
        ReaderApi.ask<GetAppContext>().map { ctx ->
            IO.fx {
                val oneResult = !one()
                val parsedTwo = !toInt(ctx.number)

                /*
                listOf(oneResult, parsedTwo)
                    .traverse(Either.applicative(), ::identity)
                    .fix()
                    .map { it.fix().sum() }
                */

                Either.fx<AppError, Int> {
                    val x = ! oneResult
                    val y = ! parsedTwo
                    x + y
                }

                /*
                // Another way to do applicative
                oneResult.flatMap { x ->
                    parsedTwo.map { y ->
                        x + y
                    }
                }
                */
            }
        }.fix()

    init {
        "can pull data from the Reader Context" {
            val appContext = GetAppContext(number = "3")
            val appEffect = myApp().run(appContext).value()

            val result = appEffect.unsafeRunSync()
            result shouldBe Right(4)
        }
    }
}
