package com.geocomply.test.gpapchecker.utils

import com.geocomply.test.gpapchecker.data.ValidationResult

class PackageListParser {
    companion object {
        private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+\$")
    }

    fun parse(input: String): ValidationResult {
        val candidates = input
            .split('\n')
            .flatMap { line -> line.split(',') }
            .filter { it.isNotEmpty() }

        val (valid, invalid) = candidates.partition { isValidPackageName(it) }

        return ValidationResult(
            validCount = valid.size,
            invalidCount = invalid.size,
            validPackages = valid,
            invalidPackages = invalid
        )
    }

    private fun isValidPackageName(name: String): Boolean {
        return PACKAGE_NAME_REGEX.matches(name)
    }
}
