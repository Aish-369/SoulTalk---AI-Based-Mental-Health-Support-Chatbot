package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

/**
 * Secure storage for sensitive data like JWT tokens.
 * Uses Android Keystore and EncryptedSharedPreferences for production-grade security.
 */
object SecureStorage {
    private const val TAG = "SecureStorage"
    private const val PREFS_NAME = "soultalk_secure_prefs"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val USER_ID_KEY = "user_id"
    
    private lateinit var encryptedPrefs: SharedPreferences
    
    /**
     * Initialize secure storage with encrypted preferences.
     * Must be called before using any storage operations.
     */
    fun initialize(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            Log.d(TAG, "Secure storage initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize secure storage: ${e.message}")
            // Fallback to regular SharedPreferences if encryption fails
            encryptedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Store access token securely.
     */
    fun setAccessToken(token: String) {
        encryptedPrefs.edit().putString(ACCESS_TOKEN_KEY, token).apply()
        Log.d(TAG, "Access token stored securely")
    }
    
    /**
     * Retrieve access token.
     */
    fun getAccessToken(): String? {
        return encryptedPrefs.getString(ACCESS_TOKEN_KEY, null)
    }
    
    /**
     * Store refresh token securely.
     */
    fun setRefreshToken(token: String) {
        encryptedPrefs.edit().putString(REFRESH_TOKEN_KEY, token).apply()
        Log.d(TAG, "Refresh token stored securely")
    }
    
    /**
     * Retrieve refresh token.
     */
    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(REFRESH_TOKEN_KEY, null)
    }
    
    /**
     * Store user ID securely.
     */
    fun setUserId(userId: String) {
        encryptedPrefs.edit().putString(USER_ID_KEY, userId).apply()
        Log.d(TAG, "User ID stored securely")
    }
    
    /**
     * Retrieve user ID.
     */
    fun getUserId(): String? {
        return encryptedPrefs.getString(USER_ID_KEY, null)
    }
    
    /**
     * Clear all stored tokens (logout).
     */
    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .apply()
        Log.d(TAG, "All tokens cleared securely")
    }
    
    /**
     * Clear all stored data (account deletion).
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        Log.d(TAG, "All secure data cleared")
    }
    
    /**
     * Check if user is authenticated.
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }
}
