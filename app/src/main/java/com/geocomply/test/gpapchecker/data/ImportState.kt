package com.geocomply.test.gpapchecker.data

sealed class ImportState {
    object Initial : ImportState()
    object ParsingInput : ImportState()
    object ReadingFile : ImportState()
    data class ValidationComplete(val result: ValidationResult) : ImportState()
    data class ImportSuccess(val packageCount: Int, val mode: ImportMode) : ImportState()
    data class Error(val message: String) : ImportState()
}

enum class ImportMode {
    REPLACE,
    APPEND
}
