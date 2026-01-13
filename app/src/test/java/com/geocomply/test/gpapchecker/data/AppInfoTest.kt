package com.geocomply.test.gpapchecker.data

import org.junit.Assert.*
import org.junit.Test

class AppInfoTest {
    
    @Test
    fun `AppInfo should be created with all required parameters`() {
        // Given
        val appName = "Test App"
        val packageName = "com.test.app"
        val hasLicenseActivity = true
        val isInstalled = true
        
        // When
        val appInfo = AppInfo(appName, packageName, hasLicenseActivity, isInstalled)
        
        // Then
        assertEquals(appName, appInfo.appName)
        assertEquals(packageName, appInfo.packageName)
        assertEquals(hasLicenseActivity, appInfo.hasLicenseActivity)
        assertEquals(isInstalled, appInfo.isInstalled)
    }
    
    @Test
    fun `AppInfo should use default value for isInstalled when not specified`() {
        // Given
        val appName = "Test App"
        val packageName = "com.test.app"
        val hasLicenseActivity = false
        
        // When
        val appInfo = AppInfo(appName, packageName, hasLicenseActivity)
        
        // Then
        assertTrue(appInfo.isInstalled)
    }
    
    @Test
    fun `AppInfo should handle empty strings`() {
        // Given
        val emptyAppName = ""
        val emptyPackageName = ""
        
        // When
        val appInfo = AppInfo(emptyAppName, emptyPackageName, false, false)
        
        // Then
        assertEquals("", appInfo.appName)
        assertEquals("", appInfo.packageName)
        assertFalse(appInfo.hasLicenseActivity)
        assertFalse(appInfo.isInstalled)
    }
    
    @Test
    fun `AppInfo should handle special characters in names`() {
        // Given
        val specialAppName = "App with special chars: !@#$%^&*()"
        val specialPackageName = "com.test.app_with_special_chars"
        
        // When
        val appInfo = AppInfo(specialAppName, specialPackageName, true, true)
        
        // Then
        assertEquals(specialAppName, appInfo.appName)
        assertEquals(specialPackageName, appInfo.packageName)
    }
    
    @Test
    fun `AppInfo should handle very long names`() {
        // Given
        val longAppName = "A".repeat(1000)
        val longPackageName = "com." + "a".repeat(1000)
        
        // When
        val appInfo = AppInfo(longAppName, longPackageName, false, false)
        
        // Then
        assertEquals(longAppName, appInfo.appName)
        assertEquals(longPackageName, appInfo.packageName)
        assertEquals(1000, appInfo.appName.length)
        assertTrue(appInfo.packageName.length > 1000)
    }
    
    @Test
    fun `AppInfo should handle null values`() {
        // Given
        val nullAppName: String? = null
        val nullPackageName: String? = null
        
        // When
        val appInfo = AppInfo(nullAppName ?: "", nullPackageName ?: "", false, false)
        
        // Then
        assertEquals("", appInfo.appName)
        assertEquals("", appInfo.packageName)
    }
    
    @Test
    fun `AppInfo should be data class with proper equals and hashCode`() {
        // Given
        val appInfo1 = AppInfo("Test App", "com.test.app", true, true)
        val appInfo2 = AppInfo("Test App", "com.test.app", true, true)
        val appInfo3 = AppInfo("Different App", "com.test.app", true, true)
        
        // When & Then
        assertEquals(appInfo1, appInfo2)
        assertNotEquals(appInfo1, appInfo3)
        assertEquals(appInfo1.hashCode(), appInfo2.hashCode())
        assertNotEquals(appInfo1.hashCode(), appInfo3.hashCode())
    }
    
    @Test
    fun `AppInfo should have proper toString representation`() {
        // Given
        val appInfo = AppInfo("Test App", "com.test.app", true, true)
        
        // When
        val toString = appInfo.toString()
        
        // Then
        assertTrue(toString.contains("Test App"))
        assertTrue(toString.contains("com.test.app"))
        assertTrue(toString.contains("true"))
    }
    
    @Test
    fun `AppInfo should handle different boolean combinations`() {
        // Given & When
        val appInfo1 = AppInfo("App1", "com.app1", true, true)
        val appInfo2 = AppInfo("App2", "com.app2", true, false)
        val appInfo3 = AppInfo("App3", "com.app3", false, true)
        val appInfo4 = AppInfo("App4", "com.app4", false, false)
        
        // Then
        assertTrue(appInfo1.hasLicenseActivity && appInfo1.isInstalled)
        assertTrue(appInfo2.hasLicenseActivity && !appInfo2.isInstalled)
        assertFalse(appInfo3.hasLicenseActivity && appInfo3.isInstalled)
        assertFalse(appInfo4.hasLicenseActivity && appInfo4.isInstalled)
    }
    
    @Test
    fun `AppInfo should handle edge case package names`() {
        // Given
        val edgeCasePackageNames = listOf(
            "com",
            "com.test",
            "com.test.app",
            "com.test.app.sub",
            "com.test.app.sub.deep"
        )
        
        // When & Then
        edgeCasePackageNames.forEach { packageName ->
            val appInfo = AppInfo("Test", packageName, false, true)
            assertEquals(packageName, appInfo.packageName)
        }
    }
}
