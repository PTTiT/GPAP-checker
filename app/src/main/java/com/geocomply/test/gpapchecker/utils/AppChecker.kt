package com.geocomply.test.gpapchecker.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.geocomply.test.gpapchecker.data.AppInfo

class AppChecker(private val context: Context) {
    private val packageManager = context.packageManager
    private val licenseActivityName = "com.pairip.licensecheck.LicenseActivity"

    fun checkPackages(packageNames: List<String>): List<AppInfo> {
        val results = mutableListOf<AppInfo>()
        
        for (packageName in packageNames) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val hasLicenseActivity = checkForLicenseActivity(packageName)
                
                results.add(AppInfo(appName, packageName, hasLicenseActivity, true))
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not installed, add with "Not Found" indicator
                results.add(AppInfo("Not Found", packageName, false, false))
            } catch (e: Exception) {
                // Any other error, treat as not found
                results.add(AppInfo("Not Found", packageName, false, false))
            }
        }
        
        return results
    }
    
    private fun checkForLicenseActivity(packageName: String): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(
                packageName, 
                PackageManager.GET_ACTIVITIES
            )
            
            packageInfo.activities?.any { activityInfo ->
                activityInfo.name == licenseActivityName
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}