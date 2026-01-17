package com.geocomply.test.gpapchecker.utils

import com.geocomply.test.gpapchecker.data.ValidationResult

class PackageListParser {
    companion object {
        private val PACKAGE_NAME_REGEX = Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+\$")
    }

    fun parse(input: String): ValidationResult {
        // Split by newlines, then by commas, trim whitespace, filter empty, and deduplicate
        val candidates = input
            .split('\n')
            .flatMap { line -> line.split(',') }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        // Partition into valid and invalid based on regex validation
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
