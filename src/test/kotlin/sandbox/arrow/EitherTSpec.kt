package sandbox.arrow

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
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

typealias EitherTIO<A> = EitherT<ForIO, AppError, A>

class EitherTSpec : StringSpec() {
    private fun one(): EitherT<ForIO, AppError, Int> =
        EitherT(IO { Right(1) })
    private fun two(inputString: String): EitherT<ForIO, AppError, Int> =
        toInt(inputString)
    private fun toInt(str: String): EitherT<ForIO, AppError, Int> =
        EitherT(IO { Right(str.toInt()) }.handleError { Left(AppError) })

    // https://stackoverflow.com/a/53747937
    private fun result(inputString: String): EitherT<ForIO, AppError, Int> =
        EitherT.monad<ForIO, AppError>(IO.monad()).fx.monad {
            val oneInt = ! one()
            val twoInt = ! two(inputString)
            oneInt + twoInt
        }.fix()

    // Using typealias
    private fun oneT(): EitherTIO<Int> =
        EitherTIO(IO { Right(1) })
    private fun twoT(inputString: String): EitherTIO<Int> =
        toIntT(inputString)
    private fun toIntT(str: String): EitherTIO<Int> =
        EitherTIO(IO { Right(str.toInt()) }.handleError { Left(AppError) })

    private fun resultT(inputString: String): EitherTIO<Int> =
        EitherTIO.monad<ForIO, AppError>(IO.monad()).fx.monad {
            val oneInt = ! oneT()
            val twoInt = ! twoT(inputString)
            oneInt + twoInt
        }.fix()

    init {
        "can combine operations with EitherIO" {
            val rightResult = result("2").value().fix().unsafeRunSync()
            rightResult shouldBe Right(3)

            val leftResult = result("three").value().fix().unsafeRunSync()
            leftResult shouldBe Left(AppError)
        }

        "can combine operations with EitherTIO - a type alias" {
            val rightResult = resultT("2").value().fix().unsafeRunSync()
            rightResult shouldBe Right(3)

            val leftResult = resultT("three").value().fix().unsafeRunSync()
            leftResult shouldBe Left(AppError)
        }
    }
}
