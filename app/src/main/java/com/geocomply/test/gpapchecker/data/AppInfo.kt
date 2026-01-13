package com.geocomply.test.gpapchecker.data

data class AppInfo(
    val appName: String,
    val packageName: String,
    val hasLicenseActivity: Boolean,
    val isInstalled: Boolean = true
)