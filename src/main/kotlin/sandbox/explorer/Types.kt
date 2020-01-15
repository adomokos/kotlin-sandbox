// ktlint-disable filename
package sandbox.explorer

sealed class AppError {
    object CsvImportError : AppError()
    object PersonInsertError : AppError()
}
