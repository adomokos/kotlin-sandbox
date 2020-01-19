// ktlint-disable filename
package sandbox.explorer

import arrow.fx.ForIO
import arrow.mtl.EitherT

typealias EitherIO<A> = EitherT<ForIO, AppError, A>

sealed class AppError {
    object CsvImportError : AppError()
    data class PersonInsertError(val errorInfo: String) : AppError()
    object JSONDeserializaitonError : AppError()
    data class GitHubApiError(val errorInfo: String) : AppError()
    data class GitHubMetricSaveError(val errorInfo: String) : AppError()
}
