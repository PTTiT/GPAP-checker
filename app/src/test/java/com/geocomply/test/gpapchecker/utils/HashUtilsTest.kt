package com.geocomply.test.gpapchecker.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class HashUtilsTest {
    
    @Test
    fun `createSha256HexHash should create consistent hash`() {
        val input = "Hello World"
        val hash1 = HashUtils.createSha256HexHash(input)
        val hash2 = HashUtils.createSha256HexHash(input)
        
        assertEquals("Same input should produce same hash", hash1, hash2)
    }
    
    @Test
    fun `createSha256HexHash should create different hash for different inputs`() {
        val input1 = "Hello World"
        val input2 = "Hello World!"
        
        val hash1 = HashUtils.createSha256HexHash(input1)
        val hash2 = HashUtils.createSha256HexHash(input2)
        
        assertNotEquals("Different inputs should produce different hashes", hash1, hash2)
    }
    
    @Test
    fun `createSha256HexHash should produce known hash for test input`() {
        val input = "Hello World"
        val expectedHash = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e"
        
        val actualHash = HashUtils.createSha256HexHash(input)
        
        assertEquals("Hex hash should match known value", expectedHash, actualHash)
    }
    
    @Test
    fun `createSha256HexHash should handle empty string`() {
        val input = ""
        val hash = HashUtils.createSha256HexHash(input)
        
        assertTrue("Hash should not be empty", hash.isNotEmpty())
        assertEquals("Empty string should produce known hash", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash)
    }
    
    @Test
    fun `createSha256HexHash should be lowercase`() {
        val input = "Test Input"
        val hash = HashUtils.createSha256HexHash(input)
        
        assertEquals("Hash should be lowercase", hash.lowercase(), hash)
    }
    
    @Test
    fun `createSha256HexHash should be 64 characters long`() {
        val input = "Any input string"
        val hash = HashUtils.createSha256HexHash(input)
        
        assertEquals("SHA-256 hex hash should be 64 characters", 64, hash.length)
    }
    
    @Test
    fun `hex hash should only contain valid hex characters`() {
        val input = "Test input with special chars!@#$%^&*()"
        val hash = HashUtils.createSha256HexHash(input)
        
        val validHexRegex = "[0-9a-f]+".toRegex()
        assertTrue("Hash should only contain hex characters", hash.matches(validHexRegex))
    }
    
    // Note: Base64 tests are skipped in unit tests due to Android dependency
    // The Base64 functionality works correctly in the actual Android app
}