package com.geocomply.test.gpapchecker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.geocomply.test.gpapchecker.data.UiState
import com.geocomply.test.gpapchecker.repository.AppRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SimpleMainViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var mockRepository: AppRepository
    
    private lateinit var viewModel: MainViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = MainViewModel(mockRepository)
    }
    
    @Test
    fun `initial state should be Initial`() {
        // When
        val initialState = viewModel.uiState.value
        
        // Then
        assertTrue(initialState is UiState.Initial)
    }
    
    @Test
    fun `initial export state should be false`() {
        // When
        val initialExportState = viewModel.canExport.value
        
        // Then
        assertFalse(initialExportState!!)
    }
    
    @Test
    fun `getCurrentAppList should return null when state is Initial`() {
        // When
        val result = viewModel.getCurrentAppList()
        
        // Then
        assertNull(result)
    }
}

