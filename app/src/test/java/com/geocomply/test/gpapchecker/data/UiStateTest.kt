package com.geocomply.test.gpapchecker.data

import org.junit.Assert.*
import org.junit.Test

class UiStateTest {
    
    @Test
    fun `Initial state should be singleton`() {
        // When
        val state1 = UiState.Initial
        val state2 = UiState.Initial
        
        // Then
        assertSame(state1, state2)
    }
    
    @Test
    fun `Loading state should be singleton`() {
        // When
        val state1 = UiState.Loading
        val state2 = UiState.Loading
        
        // Then
        assertSame(state1, state2)
    }
    
    @Test
    fun `Success state should contain data`() {
        // Given
        val testData = listOf("item1", "item2", "item3")
        
        // When
        val state = UiState.Success(testData)
        
        // Then
        assertTrue(state is UiState.Success)
        assertEquals(testData, state.data)
    }
    
    @Test
    fun `Success state should handle empty data`() {
        // Given
        val emptyData = emptyList<String>()
        
        // When
        val state = UiState.Success(emptyData)
        
        // Then
        assertTrue(state is UiState.Success)
        assertTrue(state.data.isEmpty())
    }
    
    @Test
    fun `Success state should handle null data`() {
        // Given
        val nullData: String? = null
        
        // When
        val state = UiState.Success(nullData)
        
        // Then
        assertTrue(state is UiState.Success)
        assertNull(state.data)
    }
    
    @Test
    fun `Error state should contain message`() {
        // Given
        val errorMessage = "Test error message"
        
        // When
        val state = UiState.Error(errorMessage)
        
        // Then
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, state.message)
    }
    
    @Test
    fun `Error state should handle empty message`() {
        // Given
        val emptyMessage = ""
        
        // When
        val state = UiState.Error(emptyMessage)
        
        // Then
        assertTrue(state is UiState.Error)
        assertEquals(emptyMessage, state.message)
    }
    
    @Test
    fun `Error state should handle null message`() {
        // Given
        val nullMessage: String? = null
        
        // When
        val state = UiState.Error(nullMessage ?: "")
        
        // Then
        assertTrue(state is UiState.Error)
        assertEquals("", state.message)
    }
    
    @Test
    fun `Success state should handle complex data types`() {
        // Given
        val complexData = mapOf(
            "key1" to 123,
            "key2" to "value2",
            "key3" to true
        )
        
        // When
        val state = UiState.Success(complexData)
        
        // Then
        assertTrue(state is UiState.Success)
        assertEquals(complexData, state.data)
        assertEquals(123, state.data["key1"])
        assertEquals("value2", state.data["key2"])
        assertEquals(true, state.data["key3"])
    }
    
    @Test
    fun `Success state should handle custom objects`() {
        // Given
        data class TestObject(val id: Int, val name: String)
        val testObject = TestObject(1, "Test")
        
        // When
        val state = UiState.Success(testObject)
        
        // Then
        assertTrue(state is UiState.Success)
        assertEquals(testObject, state.data)
        assertEquals(1, state.data.id)
        assertEquals("Test", state.data.name)
    }
    
    @Test
    fun `Error state should handle special characters in message`() {
        // Given
        val specialMessage = "Error with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
        
        // When
        val state = UiState.Error(specialMessage)
        
        // Then
        assertTrue(state is UiState.Error)
        assertEquals(specialMessage, state.message)
    }
    
    @Test
    fun `Error state should handle very long message`() {
        // Given
        val longMessage = "A".repeat(1000)
        
        // When
        val state = UiState.Error(longMessage)
        
        // Then
        assertTrue(state is UiState.Error)
        assertEquals(longMessage, state.message)
        assertEquals(1000, state.message.length)
    }
    
    @Test
    fun `Success state should handle large collections`() {
        // Given
        val largeList = List(10000) { it }
        
        // When
        val state = UiState.Success(largeList)
        
        // Then
        assertTrue(state is UiState.Success)
        assertEquals(largeList, state.data)
        assertEquals(10000, state.data.size)
        assertEquals(0, state.data.first())
        assertEquals(9999, state.data.last())
    }
    
    @Test
    fun `state equality should work correctly`() {
        // Given
        val data1 = listOf("a", "b", "c")
        val data2 = listOf("a", "b", "c")
        val data3 = listOf("x", "y", "z")
        
        // When
        val success1 = UiState.Success(data1)
        val success2 = UiState.Success(data2)
        val success3 = UiState.Success(data3)
        val error1 = UiState.Error("error1")
        val error2 = UiState.Error("error2")
        
        // Then
        assertEquals(success1, success2)
        assertNotEquals(success1, success3)
        assertNotEquals(success1, error1)
        assertNotEquals(error1, error2)
        assertEquals(UiState.Initial, UiState.Initial)
        assertEquals(UiState.Loading, UiState.Loading)
    }
    
    @Test
    fun `state hashCode should work correctly`() {
        // Given
        val data = listOf("test")
        val success = UiState.Success(data)
        val error = UiState.Error("test")
        
        // When
        val successHash = success.hashCode()
        val errorHash = error.hashCode()
        val initialHash = UiState.Initial.hashCode()
        val loadingHash = UiState.Loading.hashCode()
        
        // Then
        assertNotNull(successHash)
        assertNotNull(errorHash)
        assertNotNull(initialHash)
        assertNotNull(loadingHash)
        
        // Hash codes should be consistent
        assertEquals(successHash, UiState.Success(data).hashCode())
        assertEquals(errorHash, UiState.Error("test").hashCode())
    }
}
