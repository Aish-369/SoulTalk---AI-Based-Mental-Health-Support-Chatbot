package com.example.core

object ApiConfig {
    // Environment-based API configuration
    private const val ENV_DEV = "dev"
    private const val ENV_PROD = "prod"
    
    // Current environment (can be set via BuildConfig or runtime)
    var currentEnvironment: String = ENV_DEV
        private set
    
    fun setEnvironment(env: String) {
        currentEnvironment = when(env.lowercase()) {
            "production", "prod" -> ENV_PROD
            else -> ENV_DEV
        }
    }
    
    // Base URLs for different environments
    private val devBaseUrl = "http://10.0.2.2:8000" // Android emulator
    private val localNetworkBaseUrl = "http://192.168.1.100:8000" // Local network
    private val prodBaseUrl = "https://api.soultalk.app" // Production
    
    // Get current base URL
    val baseUrl: String
        get() = when(currentEnvironment) {
            ENV_PROD -> prodBaseUrl
            else -> devBaseUrl
        }
    
    // API endpoints
    object Endpoints {
        const val CHAT_SEND = "/chat/send"
        const val MOOD_LOG = "/mood/log"
        const val JOURNAL_CREATE = "/journal/create"
        const val VOICE_SAVE = "/voice/process"
        const val COMPANION_UPDATE = "/companion/update"
        const val COMPANION_STATUS = "/companion/status"
        const val SETTINGS = "/settings"
    }
    
    // Timeout configurations
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L
    
    // Retry configuration
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
}
