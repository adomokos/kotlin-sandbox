// ktlint-disable filename
package sandbox.explorer

import arrow.fx.ForIO
import arrow.mtl.EitherT

typealias EitherIO<A> = EitherT<AppError, ForIO, A>

sealed class AppError {
    object CsvImportError : AppError()
    data class PersonInsertError(val errorInfo: String) : AppError()
    object JSONDeserializationError : AppError()
    data class GitHubApiError(val errorInfo: String) : AppError()
    data class GitHubMetricSaveError(val errorInfo: String) : AppError()
}
