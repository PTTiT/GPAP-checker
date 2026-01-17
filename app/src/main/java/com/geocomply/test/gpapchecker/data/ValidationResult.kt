package com.geocomply.test.gpapchecker.data

data class ValidationResult(
    val validCount: Int,
    val invalidCount: Int,
    val validPackages: List<String>,
    val invalidPackages: List<String>
) {
    val totalCount: Int get() = validCount + invalidCount
    val hasValidPackages: Boolean get() = validCount > 0
    val hasInvalidPackages: Boolean get() = invalidCount > 0
}
