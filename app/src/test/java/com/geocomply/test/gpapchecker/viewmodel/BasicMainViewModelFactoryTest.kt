package com.geocomply.test.gpapchecker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BasicMainViewModelFactoryTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var factory: MainViewModelFactory
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        factory = MainViewModelFactory(mockContext)
    }
    
    @Test
    fun `factory should implement ViewModelProvider Factory interface`() {
        assertTrue(factory is ViewModelProvider.Factory)
    }
    
    @Test
    fun `create should throw exception when wrong class is requested`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            factory.create(TestViewModel::class.java)
        }
        assertEquals("Unknown ViewModel class", exception.message)
    }
    
    @Test
    fun `factory constructor should accept context parameter`() {
        val newFactory = MainViewModelFactory(mockContext)
        assertNotNull(newFactory)
        assertTrue(newFactory is MainViewModelFactory)
    }
    
    @Test
    fun `isAssignableFrom should work correctly with MainViewModel class`() {
        assertTrue(MainViewModel::class.java.isAssignableFrom(MainViewModel::class.java))
    }
    
    // Test ViewModel class for testing purposes
    private class TestViewModel : ViewModel()
}