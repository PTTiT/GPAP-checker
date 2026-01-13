package com.geocomply.test.gpapchecker

import com.geocomply.test.gpapchecker.data.AppInfo
import com.geocomply.test.gpapchecker.data.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Test configuration and utilities for the GPAPChecker app
 */
object TestConfig {
    
    /**
     * Test coroutine dispatcher for unit testing
     */
    val testDispatcher = TestCoroutineDispatcher()
    
    /**
     * Setup test environment
     */
    fun setupTestEnvironment() {
        Dispatchers.setMain(testDispatcher)
    }
    
    /**
     * Cleanup test environment
     */
    fun cleanupTestEnvironment() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
    
    /**
     * Create sample AppInfo objects for testing
     */
    object SampleData {
        
        val sampleAppInfo1 = AppInfo(
            appName = "FanDuel Sportsbook",
            packageName = "com.fanduel.sportsbook",
            hasLicenseActivity = false,
            isInstalled = true
        )
        
        val sampleAppInfo2 = AppInfo(
            appName = "DraftKings Sportsbook",
            packageName = "com.draftkings.sportsbook",
            hasLicenseActivity = true,
            isInstalled = true
        )
        
        val sampleAppInfo3 = AppInfo(
            appName = "Not Found App",
            packageName = "com.notfound.app",
            hasLicenseActivity = false,
            isInstalled = false
        )
        
        val sampleAppList = listOf(sampleAppInfo1, sampleAppInfo2, sampleAppInfo3)
        
        val emptyAppList = emptyList<AppInfo>()
        
        val largeAppList = List(100) { index ->
            AppInfo(
                appName = "Test App $index",
                packageName = "com.test.app$index",
                hasLicenseActivity = index % 2 == 0,
                isInstalled = index % 3 != 0
            )
        }
    }
    
    /**
     * Create sample UiState objects for testing
     */
    object SampleUiStates {
        
        val initial = UiState.Initial
        val loading = UiState.Loading
        val success = UiState.Success(SampleData.sampleAppList)
        val successEmpty = UiState.Success(SampleData.emptyAppList)
        val error = UiState.Error("Test error message")
        val errorEmpty = UiState.Error("")
    }
    
    /**
     * Sample package names for testing
     */
    object SamplePackageNames {
        
        val validPackages = listOf(
            "com.fanduel.sportsbook",
            "com.draftkings.sportsbook",
            "com.betmgm.ca.casino",
            "com.pokerstars.casino",
            "com.wsop.mi"
        )
        
        val invalidPackages = listOf(
            "",
            "invalid.package",
            ".invalid.package",
            "invalid.package.",
            "INVALID.PACKAGE"
        )
    }
}
