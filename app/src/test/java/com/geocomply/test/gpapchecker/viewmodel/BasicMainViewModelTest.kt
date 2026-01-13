package com.geocomply.test.gpapchecker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.geocomply.test.gpapchecker.data.AppInfo
import com.geocomply.test.gpapchecker.data.UiState
import com.geocomply.test.gpapchecker.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class BasicMainViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var mockRepository: AppRepository
    
    private lateinit var viewModel: MainViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be Initial`() {
        assertTrue(viewModel.uiState.value is UiState.Initial)
    }
    
    @Test
    fun `initial export state should be false`() {
        assertFalse(viewModel.canExport.value!!)
    }
    
    @Test
    fun `getCurrentAppList should return null when state is Initial`() {
        assertNull(viewModel.getCurrentAppList())
    }
    
    @Test
    fun `getPackageCount should delegate to repository`() {
        // Given
        `when`(mockRepository.getPackageNamesToCheck()).thenReturn(listOf("com.test1", "com.test2"))
        
        // When
        val count = viewModel.getPackageCount()
        
        // Then
        assertEquals(2, count)
        verify(mockRepository).getPackageNamesToCheck()
    }
    
    @Test
    fun `exportResults should not call repository when state is Initial`() {
        // When
        viewModel.exportResults()
        
        // Then
        verifyNoInteractions(mockRepository)
    }
    
    @Test
    fun `checkPackages should update state to Success with data`() = runTest {
        // Given
        val testData = listOf(AppInfo("Test App", "com.test.app", false, true))
        `when`(mockRepository.checkPackages()).thenReturn(testData)
        
        // When
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successState = viewModel.uiState.value as UiState.Success
        assertEquals(testData, successState.data)
        assertTrue(viewModel.canExport.value!!)
    }
    
    @Test
    fun `checkPackages should update state to Success with empty data`() = runTest {
        // Given
        `when`(mockRepository.checkPackages()).thenReturn(emptyList())
        
        // When
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successState = viewModel.uiState.value as UiState.Success
        assertTrue(successState.data.isEmpty())
        assertFalse(viewModel.canExport.value!!)
    }
    
    @Test
    fun `checkPackages should update state to Error on exception`() = runTest {
        // Given
        val errorMessage = "Test error"
        `when`(mockRepository.checkPackages()).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.uiState.value is UiState.Error)
        val errorState = viewModel.uiState.value as UiState.Error
        assertEquals("Error checking packages: $errorMessage", errorState.message)
        assertFalse(viewModel.canExport.value!!)
    }
    
    @Test
    fun `exportResults should call repository when state is Success with data`() = runTest {
        // Given
        val testData = listOf(AppInfo("Test App", "com.test.app", false, true))
        `when`(mockRepository.checkPackages()).thenReturn(testData)
        
        // Setup state
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.exportResults()
        
        // Then
        verify(mockRepository).exportToCsv(testData)
    }
    
    @Test
    fun `exportResults should not call repository when state is Success with empty data`() = runTest {
        // Given
        `when`(mockRepository.checkPackages()).thenReturn(emptyList())
        
        // Setup state
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        reset(mockRepository) // Clear previous interactions
        
        // When
        viewModel.exportResults()
        
        // Then
        verifyNoInteractions(mockRepository)
    }
    
    @Test
    fun `getCurrentAppList should return data when state is Success`() = runTest {
        // Given
        val testData = listOf(AppInfo("Test App", "com.test.app", false, true))
        `when`(mockRepository.checkPackages()).thenReturn(testData)
        
        // Setup state
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val result = viewModel.getCurrentAppList()
        
        // Then
        assertEquals(testData, result)
    }
    
    @Test
    fun `getCurrentAppList should return empty list when state is Success with empty data`() = runTest {
        // Given
        `when`(mockRepository.checkPackages()).thenReturn(emptyList())
        
        // Setup state
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val result = viewModel.getCurrentAppList()
        
        // Then
        assertEquals(emptyList<AppInfo>(), result)
    }
    
    @Test
    fun `getCurrentAppList should return null when state is Error`() = runTest {
        // Given
        `when`(mockRepository.checkPackages()).thenThrow(RuntimeException("Test error"))
        
        // Setup state
        viewModel.checkPackages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val result = viewModel.getCurrentAppList()
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `uiState getter should return correct LiveData`() {
        assertNotNull(viewModel.uiState)
        assertTrue(viewModel.uiState.value is UiState.Initial)
    }
    
    @Test
    fun `canExport getter should return correct LiveData`() {
        assertNotNull(viewModel.canExport)
        assertFalse(viewModel.canExport.value!!)
    }
}