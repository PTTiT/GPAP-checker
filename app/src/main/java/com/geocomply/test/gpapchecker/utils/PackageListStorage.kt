package com.geocomply.test.gpapchecker.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class PackageListStorage(context: Context) {
    companion object {
        private const val PREFS_NAME = "package_list_prefs"
        private const val KEY_CUSTOM_PACKAGES = "custom_packages"
        private const val KEY_IS_CUSTOM_LIST = "is_custom_list"
        private const val KEY_LAST_IMPORT_TIME = "last_import_time"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCustomList(): Boolean {
        return prefs.getBoolean(KEY_IS_CUSTOM_LIST, false)
    }

    fun getPackageList(): List<String> {
        if (!hasCustomList()) {
            return emptyList()  // Caller should use hardcoded list
        }
        val json = prefs.getString(KEY_CUSTOM_PACKAGES, "[]") ?: "[]"
        return try {
            Json.decodeFromString<List<String>>(json)
        } catch (e: Exception) {
            emptyList()  // Fallback on parse error
        }
    }

    fun savePackageList(packages: List<String>) {
        val json = Json.encodeToString(packages)
        prefs.edit().putString(KEY_CUSTOM_PACKAGES, json).apply()
        prefs.edit().putBoolean(KEY_IS_CUSTOM_LIST, true).apply()
        prefs.edit().putLong(KEY_LAST_IMPORT_TIME, System.currentTimeMillis()).apply()
    }

    fun clearCustomList() {
        prefs.edit().apply {
            remove(KEY_CUSTOM_PACKAGES)
            putBoolean(KEY_IS_CUSTOM_LIST, false)
            apply()
        }
    }
}
