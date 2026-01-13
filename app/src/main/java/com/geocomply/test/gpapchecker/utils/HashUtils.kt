package com.geocomply.test.gpapchecker.utils

import android.text.TextUtils
import android.util.Base64
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

object HashUtils {
    
    /**
     * Creates a SHA-256 hash of the input string and returns it in Base64 format
     * 
     * @param input The string to hash
     * @return Base64 encoded SHA-256 hash of the input string
     */
    fun createSha256Base64Hash(input: String): String {
        if (TextUtils.isEmpty(input)) {
            return input
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_PADDING)
    }
    
    /**
     * Creates a SHA-256 hash of the input string and returns it as hex string
     * 
     * @param input The string to hash
     * @return Hexadecimal representation of SHA-256 hash
     */
    fun createSha256HexHash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}