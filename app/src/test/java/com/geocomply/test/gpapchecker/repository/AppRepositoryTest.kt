package com.geocomply.test.gpapchecker.repository

import com.geocomply.test.gpapchecker.data.AppInfo
import com.geocomply.test.gpapchecker.utils.AppChecker
import com.geocomply.test.gpapchecker.utils.CsvExporter
import com.geocomply.test.gpapchecker.utils.PackageListStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class AppRepositoryTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var mockAppChecker: AppChecker

    @Mock
    private lateinit var mockCsvExporter: CsvExporter

    @Mock
    private lateinit var mockPackageListStorage: PackageListStorage

    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        // Mock the storage to return false for hasCustomList() so it uses hardcoded list
        `when`(mockPackageListStorage.hasCustomList()).thenReturn(false)
        repository = AppRepository(mockAppChecker, mockCsvExporter, mockPackageListStorage)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
    
    @Test
    fun `getPackageNamesToCheck should return correct package list`() {
        // When
        val packageList = repository.getPackageNamesToCheck()
        
        // Then
        assertNotNull(packageList)
        assertTrue(packageList.isNotEmpty())
        assertTrue(packageList.size >= 200) // Should have at least 200 packages
        
        // Verify some specific packages exist from the actual list
        assertTrue("Betano sportsbook package should be present", packageList.contains("com.betano.sportsbook"))
        assertTrue("FanDuel sportsbook package should be present", packageList.contains("com.fanduel.sportsbook"))
        assertTrue("DraftKings sportsbook package should be present", packageList.contains("com.draftkings.sportsbook"))
    }
    
    @Test
    fun `checkPackages should call AppChecker with correct package list`() = runBlockingTest {
        // Given
        val mockAppList = listOf(
            AppInfo("Betano Sportsbook", "com.betano.sportsbook", false, true),
            AppInfo("FanDuel Sportsbook", "com.fanduel.sportsbook", true, true)
        )
        val packageList = repository.getPackageNamesToCheck()
        `when`(mockAppChecker.checkPackages(packageList)).thenReturn(mockAppList)
        
        // When
        val result = repository.checkPackages()
        
        // Then
        verify(mockAppChecker).checkPackages(packageList)
        assertEquals(mockAppList, result)
    }
    
    @Test
    fun `checkPackages should return empty list when AppChecker returns empty`() = runBlockingTest {
        // Given
        val packageList = repository.getPackageNamesToCheck()
        `when`(mockAppChecker.checkPackages(packageList)).thenReturn(emptyList())
        
        // When
        val result = repository.checkPackages()
        
        // Then
        verify(mockAppChecker).checkPackages(packageList)
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `checkPackages should handle AppChecker exceptions gracefully`() = runBlockingTest {
        // Given
        val packageList = repository.getPackageNamesToCheck()
        `when`(mockAppChecker.checkPackages(packageList)).thenThrow(RuntimeException("Test error"))
        
        // When & Then
        try {
            repository.checkPackages()
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            // Expected exception
        }
        verify(mockAppChecker).checkPackages(packageList)
    }
    
    @Test
    fun `exportToCsv should call CsvExporter with correct data`() {
        // Given
        val mockAppList = listOf(
            AppInfo("Test App 1", "com.test.app1", false, true),
            AppInfo("Test App 2", "com.test.app2", true, true)
        )
        
        // When
        repository.exportToCsv(mockAppList)
        
        // Then
        verify(mockCsvExporter).exportAndEmail(mockAppList)
    }
    
    @Test
    fun `exportToCsv should handle empty list`() {
        // Given
        val emptyList = emptyList<AppInfo>()
        
        // When
        repository.exportToCsv(emptyList)
        
        // Then
        verify(mockCsvExporter).exportAndEmail(emptyList)
    }
    
    @Test
    fun `exportToCsv should handle large list`() {
        // Given
        val largeList = List(1000) { index ->
            AppInfo("App $index", "com.app$index", index % 2 == 0, index % 3 == 0)
        }
        
        // When
        repository.exportToCsv(largeList)
        
        // Then
        verify(mockCsvExporter).exportAndEmail(largeList)
    }
    
    @Test
    fun `package list should contain valid package names`() {
        // When
        val packageList = repository.getPackageNamesToCheck()
        
        // Then
        assertNotNull(packageList)
        assertTrue(packageList.isNotEmpty())
        
        // Check that all package names follow Android package naming convention (allowing uppercase)
        val packageNameRegex = Regex("^[a-z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$")
        for (packageName in packageList) {
            assertTrue("Package name '$packageName' is not valid", packageNameRegex.matches(packageName))
        }
        
        // Check specific expected packages from the actual list
        assertTrue("Betano sportsbook package should be present", packageList.contains("com.betano.sportsbook"))
        assertTrue("FanDuel sportsbook package should be present", packageList.contains("com.fanduel.sportsbook"))
        assertTrue("DraftKings sportsbook package should be present", packageList.contains("com.draftkings.sportsbook"))
    }
    
    @Test
    fun `repository should be properly initialized with dependencies`() {
        // Then
        assertNotNull(repository)
        // Verify that the repository can access its dependencies
        try {
            repository.getPackageNamesToCheck()
        } catch (e: Exception) {
            fail("Repository should not throw exception during initialization: ${e.message}")
        }
    }
}
