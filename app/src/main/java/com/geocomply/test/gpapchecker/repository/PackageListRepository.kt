package com.geocomply.test.gpapchecker.repository

import com.geocomply.test.gpapchecker.data.ImportMode
import com.geocomply.test.gpapchecker.utils.PackageListStorage

class PackageListRepository(
    private val storage: PackageListStorage
) {
    suspend fun savePackages(packages: List<String>, mode: ImportMode) {
        val finalList = when (mode) {
            ImportMode.REPLACE -> packages
            ImportMode.APPEND -> {
                val existing = storage.getPackageList()
                (existing + packages).distinct()  // Merge & deduplicate
            }
        }
        storage.savePackageList(finalList)
    }

    fun getPackageList(): List<String> {
        return storage.getPackageList()
    }

    fun hasCustomList(): Boolean {
        return storage.hasCustomList()
    }
}
