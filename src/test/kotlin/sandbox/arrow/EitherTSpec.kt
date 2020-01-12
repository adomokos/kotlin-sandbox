package sandbox.arrow

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.fx.handleError
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class EitherTSpec : StringSpec() {
    object AppError

    private fun one(): IO<Either<AppError, Int>> =
        IO { Right(1) }
    private fun two(inputString: String): IO<Either<AppError, String>> =
        IO { Right(inputString) }
    private fun toInt(str: String): IO<Either<AppError, Int>> =
        IO { Right(str.toInt()) }.handleError { Left(AppError) }

    // https://stackoverflow.com/a/53747937
    fun result(inputString: String): EitherT<ForIO, AppError, Int> =
        EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
            val oneInt = ! EitherT(one())
            val twoString = ! EitherT(two(inputString))
            val twoInt = ! EitherT(toInt(twoString))
            oneInt + twoInt
        }.fix()

    init {
        "can combine operations with IO<Either>" {
            val rightResult = result("2").value().fix().unsafeRunSync()
            rightResult shouldBe Right(3)

            val leftResult = result("three").value().fix().unsafeRunSync()
            leftResult shouldBe Left(AppError)
        }
    }
}
