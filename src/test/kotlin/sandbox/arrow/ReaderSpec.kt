package sandbox.arrow

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.extensions.fx
import arrow.core.value
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.fx.handleError
import arrow.mtl.EitherT
import arrow.mtl.Reader
import arrow.mtl.ReaderApi
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import arrow.mtl.map
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

typealias EitherIO<A, B> = EitherT<ForIO, A, B>

class ReaderSpec : StringSpec() {
    object AppError

    data class GetAppContext(
        val numberString: String
    )

    private fun one(): IO<Either<AppError, Int>> =
        IO { Right(1) }
    private fun two(inputString: String): IO<Either<AppError, Int>> = toInt(inputString)
    private fun toInt(str: String): IO<Either<AppError, Int>> =
        IO { Right(str.toInt()) }.handleError { Left(AppError) }

    private fun myApp(): Reader<GetAppContext, IO<Either<AppError, Int>>> =
        ReaderApi.ask<GetAppContext>().map { ctx ->
            IO.fx {
                val oneResult = !one()
                val parsedTwo = !two(ctx.numberString)

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

    // Same example, but Reader holds an EitherT<ForIO> - EitherIO
    private fun oneT(): EitherIO<AppError, Int> =
        EitherIO(IO { Right(1) })
    private fun twoT(inputString: String): EitherIO<AppError, Int> =
        toIntT(inputString)
    private fun toIntT(str: String): EitherIO<AppError, Int> =
        EitherT(IO { Right(str.toInt()) }.handleError { Left(AppError) })

    private fun myAppT(): Reader<GetAppContext, EitherIO<AppError, Int>> =
        ReaderApi.ask<GetAppContext>().map { ctx ->
            EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
                val x = ! oneT()
                val y = ! twoT(ctx.numberString)

                x + y
            }.fix()
        }

    init {
        "can pull data from the Reader Context" {
            val appContext = GetAppContext(numberString = "3")
            val appEffect = myApp().run(appContext).value()

            val result = appEffect.unsafeRunSync()
            result shouldBe Right(4)
        }

        "can use EitherT for simplicity" {
            val appContext = GetAppContext(numberString = "4")
            val appEffect = myAppT().run(appContext).value()

            val result = appEffect.value().fix().unsafeRunSync()
            result shouldBe Right(5)
        }
    }
}
