package sandbox.arrow

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.extensions.fx
import arrow.core.fix
import arrow.core.value
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.fx.handleError
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.Reader
import arrow.mtl.ReaderApi
import arrow.mtl.ReaderT
import arrow.mtl.extensions.eithert.applicative.applicative
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.extensions.monadError
import arrow.mtl.fix
import arrow.mtl.flatMap
import arrow.mtl.map
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

object AppError

data class GetAppContext(
    val numberString: String
)

typealias EitherIO<A, B> = EitherT<ForIO, A, B>
typealias EitherIOP<E> = EitherTPartialOf<ForIO, E>
typealias RIO<E, B> = ReaderT<EitherIOP<E>, GetAppContext, B>
typealias ReatherIO<A> = Reader<GetAppContext, EitherIO<AppError, A>>

class ReaderSpec : StringSpec() {

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
    private fun oneT2(): ReatherIO<Int> =
        ReaderApi.lift {
            EitherIO(IO { Right(1) })
        }

    private fun twoT2(): ReatherIO<Int> =
        ReaderApi.ask<GetAppContext>().flatMap { ctx ->
            toInt2(ctx.numberString)
        }

    private fun toInt2(str: String): ReatherIO<Int> =
        ReaderApi.lift {
            EitherIO(IO { Right(str.toInt()) }.handleError { Left(AppError) })
        }

    private fun oneT(): EitherIO<AppError, Int> =
        EitherIO(IO { Right(1) })
    private fun twoT(inputString: String): EitherIO<AppError, Int> =
        toIntT(inputString)
    private fun toIntT(str: String): EitherIO<AppError, Int> =
        EitherT(IO { Right(str.toInt()) }.handleError { Left(AppError) })

    private fun myAppT(): ReatherIO<Int> =
        ReaderApi.ask<GetAppContext>().map { ctx ->
            EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
                val x = ! oneT()
                val y = ! twoT(ctx.numberString)

                x + y
            }.fix()
        }

    private val myAppT2 =
        ReaderApi.ask<GetAppContext>().map { ctx ->
            EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
                val x = ! oneT2().run(ctx).fix().value()
                val y = ! twoT2().run(ctx).fix().value()

                x + y
            }
        }

    object RIOApi {
        fun monadError() = EitherT.monadError<ForIO, AppError>(IO.monad())

        fun <E> raiseError(e: E): RIO<E, Nothing> =
            RIO.raiseError(EitherT.monadError<ForIO, E>(IO.monad()), e)

        fun <A> just(a: A): RIO<Nothing, A> =
            RIO.just(EitherT.applicative<ForIO, Nothing>(IO.applicative()), a)

        fun ask() = ReaderT.ask<EitherTPartialOf<ForIO, AppError>, GetAppContext>(monadError())
    }

    val myAppReaderT =
        RIOApi.ask().map(RIOApi.monadError()) { ctx ->
            3
        }

        /*
            val result = ctx.numberString

//            val x = ! oneT()
//            val y = ! twoT(ctx.numberString)
//            RIOApi.just(x + y

            RIOApi.just(result)
        }
        */

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

        "can use ReaderT for simplicity?" {
            val appContext = GetAppContext(numberString = "4")
            val app = myAppReaderT.run(appContext).fix()

            val result = app.value().fix().unsafeRunSync()

            result shouldBe Right(3)
        }

        "can carry the ReaderT in the calling context" {
            val appContext = GetAppContext(numberString = "4")
            val app = myAppT2.run(appContext).fix().value().fix()

            val result = app.value().fix().unsafeRunSync()
            result shouldBe Right(5)
        }
    }
}
