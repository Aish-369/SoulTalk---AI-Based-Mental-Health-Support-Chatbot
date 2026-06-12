package com.example.data.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import com.example.core.ApiConfig

data class CompanionSelectionRequest(
  val companion_type: String,
  val companion_name: String
)

data class CompanionSelectionResponse(
  val success: Boolean
)

data class MoodLogRequest(
  val mood: String,
  val notes: String?
)

data class MoodLogResponse(
  val weather: String,
  val success: Boolean,
  val score: Int,
  val emotion: String
)

data class ChatSendRequest(
  val message: String,
  val user_id: String? = null,
  val emotion: String? = null
)

data class ChatSendResponse(
  val message_id: Int,
  val reply: String,
  val emotion: String,
  val confidence: Double,
  val voice_reply_base64: String?
)

data class ChatMessageDto(
  val id: Int,
  val role: String,
  val message: String,
  val emotion: String?,
  val created_at: Long
)

data class ChatContextResponse(
  val companion_name: String,
  val companion_type: String,
  val personality_type: String,
  val preferred_language: String,
  val recent_emotional_trends: List<String>,
  val recent_mood: String?
)

data class VoiceStartRequest(
  val voice_personality: String
)

data class VoiceStartResponse(
  val success: Boolean,
  val session_id: String,
  val greeting: String,
  val companion_name: String,
  val companion_type: String
)

data class VoiceProcessRequest(
  val transcript: String,
  val tone: String = "neutral",
  val speed: Double = 1.0,
  val energy: Double = 1.0,
  val duration: Int = 0
)

data class VoiceProcessResponse(
  val success: Boolean,
  val detected_emotion: String,
  val confidence: Double,
  val is_crisis: Boolean
)

data class VoiceResponseRequest(
  val transcript: String,
  val detected_emotion: String,
  val voice_personality: String
)

data class VoiceResponseResponse(
  val reply_text: String,
  val voice_pitch: Double,
  val voice_speed: Double,
  val character_expression: String
)

data class VoiceConversationDto(
  val id: Int,
  val user_id: Int,
  val transcript: String,
  val emotion: String?,
  val confidence: Double?,
  val duration: Int,
  val created_at: Long
)

data class MoodLogDto(
  val id: Int,
  val user_id: Int,
  val mood: String,
  val emotion: String,
  val score: Int,
  val notes: String?,
  val created_at: Long
)

data class EmotionalWeatherDto(
  val id: Int,
  val user_id: Int,
  val weather: String,
  val generated_at: Long
)

data class AIInsightsResponse(
  val weekly_summary: String,
  val achievements: List<String>,
  val growth_areas: List<String>,
  val personalized_encouragement: String,
  val insights: List<String>,
  val most_common_emotion: String,
  val best_day_of_week: String,
  val most_positive_time: String,
  val stress_triggers: String,
  val mood_improvement_factors: String
)

data class BreathingStartRequest(
  val session_type: String,
  val duration: Int
)

data class BreathingStartResponse(
  val success: Boolean,
  val session_id: String
)

data class BreathingCompleteRequest(
  val session_type: String,
  val duration: Int,
  val cycles_completed: Int,
  val xp_earned: Int
)

data class BreathingCompleteResponse(
  val success: Boolean,
  val xp_earned: Int,
  val current_level: Int,
  val total_xp: Int
)

data class BreathingSessionDto(
  val id: Int,
  val user_id: Int,
  val session_type: String,
  val duration: Int,
  val cycles_completed: Int,
  val xp_earned: Int,
  val created_at: Long
)

data class BreathingStatsResponse(
  val total_sessions: Int,
  val total_duration: Int,
  val total_xp: Int,
  val average_cycles: Double
)

data class CompanionStatusResponse(
  val companion_name: String,
  val companion_type: String,
  val level: Int,
  val xp: Int,
  val stage: String,
  val mood: String,
  val friendship_level: String,
  val today_activity: String
)

data class CompanionUpdateRequest(
  val level: Int,
  val xp: Int,
  val stage: String
)

data class CompanionUpdateResponse(
  val success: Boolean,
  val new_level: Int,
  val new_xp: Int,
  val new_stage: String
)

data class CompanionMemoryDto(
  val id: Int,
  val user_id: Int,
  val memory_title: String,
  val memory_description: String,
  val icon: String,
  val category: String,
  val created_at: Long
)

data class CompanionMemoryRequest(
  val memory_title: String,
  val memory_description: String,
  val icon: String,
  val category: String
)

data class AchievementDto(
  val id: String,
  val user_id: Int,
  val achievement_name: String,
  val description: String,
  val icon: String,
  val unlocked: Boolean,
  val unlocked_at: Long?,
  val progress: Int,
  val max_progress: Int
)

data class CompanionCustomizationRequest(
  val item_id: String,
  val equipped: Boolean
)

data class CompanionCustomizationResponse(
  val success: Boolean,
  val message: String
)

data class TimelineEventDto(
  val id: Int,
  val user_id: Int,
  val event_type: String,
  val emotion: String,
  val emotion_icon: String,
  val title: String,
  val description: String,
  val ai_reflection: String,
  val emotional_weather: String,
  val created_at: Long
)

data class TimelineEventRequest(
  val event_type: String,
  val emotion: String,
  val emotion_icon: String,
  val title: String,
  val description: String,
  val emotional_weather: String
)

data class TimelineGenerateRequest(
  val sources: List<String>
)

data class TimelineGenerateResponse(
  val success: Boolean,
  val events: List<TimelineEventDto>
)

data class GrowthSummaryResponse(
  val summary: String
)

// Profile Endpoints Data Classes
data class ProfileResponse(
  val user: UserDto,
  val preferences: ProfilePreferencesDto,
  val stats: ProfileStatsDto
)

data class ProfilePreferencesDto(
  val ai_tone: String,
  val language: String,
  val notifications_enabled: Boolean,
  val ai_memory_enabled: Boolean,
  val voice_enabled: Boolean
)

data class ProfileStatsDto(
  val mood_logs_count: Int,
  val journal_entries_count: Int,
  val voice_sessions_count: Int,
  val breathing_sessions_count: Int,
  val stability_score: Int,
  val top_emotion: String
)

data class ProfileUpdateRequest(
  val ai_tone: String? = null,
  val language: String? = null,
  val notifications_enabled: Boolean? = null,
  val ai_memory_enabled: Boolean? = null
)

data class ProfileInsightsResponse(
  val emotional_trends: List<String>,
  val stability_score: Int,
  val top_emotions: List<String>,
  val monthly_summary: String
)

data class ProfileResetResponse(
  val success: Boolean,
  val message: String
)

data class ProfileExportResponse(
  val success: Boolean,
  val data_url: String? = null,
  val message: String
)

// Settings Endpoints Data Classes
data class SettingsResponse(
  val notifications_enabled: Boolean,
  val ai_memory_enabled: Boolean,
  val voice_enabled: Boolean,
  val ai_tone: String,
  val language: String,
  val mood_reminders: Boolean,
  val journal_reminders: Boolean,
  val breathing_reminders: Boolean,
  val voice_reminders: Boolean,
  val emotion_sensitivity: String,
  val response_style: String,
  val voice_speed: Float,
  val voice_tone: String,
  val biometric_enabled: Boolean,
  val offline_data_enabled: Boolean,
  val privacy_level: String
)

data class SettingsUpdateRequest(
  val notifications_enabled: Boolean? = null,
  val ai_memory_enabled: Boolean? = null,
  val voice_enabled: Boolean? = null,
  val ai_tone: String? = null,
  val language: String? = null,
  val mood_reminders: Boolean? = null,
  val journal_reminders: Boolean? = null,
  val breathing_reminders: Boolean? = null,
  val voice_reminders: Boolean? = null,
  val emotion_sensitivity: String? = null,
  val response_style: String? = null,
  val voice_speed: Float? = null,
  val voice_tone: String? = null,
  val biometric_enabled: Boolean? = null,
  val offline_data_enabled: Boolean? = null
)

data class SettingsResetMemoryResponse(
  val success: Boolean,
  val message: String
)

interface CompanionApiService {
  @POST("companion/select")
  suspend fun selectCompanion(
    @Header("Authorization") authHeader: String,
    @Body request: CompanionSelectionRequest
  ): CompanionSelectionResponse

  @POST("breathing/start")
  suspend fun startBreathingSession(
    @Header("Authorization") authHeader: String,
    @Body request: BreathingStartRequest
  ): BreathingStartResponse

  @POST("breathing/complete")
  suspend fun completeBreathingSession(
    @Header("Authorization") authHeader: String,
    @Body request: BreathingCompleteRequest
  ): BreathingCompleteResponse

  @GET("breathing/history")
  suspend fun getBreathingHistory(
    @Header("Authorization") authHeader: String
  ): List<BreathingSessionDto>

  @GET("breathing/stats")
  suspend fun getBreathingStats(
    @Header("Authorization") authHeader: String
  ): BreathingStatsResponse

  @POST("mood/log")
  suspend fun logMood(
    @Header("Authorization") authHeader: String,
    @Body request: MoodLogRequest
  ): MoodLogResponse

  @POST("chat/send")
  suspend fun sendChatMessage(
    @Header("Authorization") authHeader: String,
    @Body request: ChatSendRequest
  ): ChatSendResponse

  @GET("chat/history")
  suspend fun getChatHistory(
    @Header("Authorization") authHeader: String
  ): List<ChatMessageDto>

  @GET("chat/context")
  suspend fun getChatContext(
    @Header("Authorization") authHeader: String
  ): ChatContextResponse

  @POST("voice/start")
  suspend fun startVoiceSession(
    @Header("Authorization") authHeader: String,
    @Body request: VoiceStartRequest
  ): VoiceStartResponse

  @POST("voice/process")
  suspend fun processVoiceInput(
    @Header("Authorization") authHeader: String,
    @Body request: VoiceProcessRequest
  ): VoiceProcessResponse

  @POST("voice/response")
  suspend fun generateVoiceResponse(
    @Header("Authorization") authHeader: String,
    @Body request: VoiceResponseRequest
  ): VoiceResponseResponse

  @GET("voice/history")
  suspend fun getVoiceHistory(
    @Header("Authorization") authHeader: String
  ): List<VoiceConversationDto>

  @GET("mood/history")
  suspend fun getMoodHistory(
    @Header("Authorization") authHeader: String
  ): List<MoodLogDto>

  @GET("mood/calendar")
  suspend fun getMoodCalendar(
    @Header("Authorization") authHeader: String
  ): List<MoodLogDto>

  @GET("weather/history")
  suspend fun getWeatherHistory(
    @Header("Authorization") authHeader: String
  ): List<EmotionalWeatherDto>

  @GET("insights")
  suspend fun getAIInsights(
    @Header("Authorization") authHeader: String
  ): AIInsightsResponse

  // Companion Home Screen Endpoints
  @GET("companion/status")
  suspend fun getCompanionStatus(
    @Header("Authorization") authHeader: String
  ): CompanionStatusResponse

  @POST("companion/update")
  suspend fun updateCompanionProgress(
    @Header("Authorization") authHeader: String,
    @Body request: CompanionUpdateRequest
  ): CompanionUpdateResponse

  @GET("companion/memories")
  suspend fun getCompanionMemories(
    @Header("Authorization") authHeader: String
  ): List<CompanionMemoryDto>

  @POST("companion/memories")
  suspend fun createCompanionMemory(
    @Header("Authorization") authHeader: String,
    @Body request: CompanionMemoryRequest
  ): CompanionMemoryDto

  @GET("companion/achievements")
  suspend fun getCompanionAchievements(
    @Header("Authorization") authHeader: String
  ): List<AchievementDto>

  @POST("companion/customize")
  suspend fun customizeCompanion(
    @Header("Authorization") authHeader: String,
    @Body request: CompanionCustomizationRequest
  ): CompanionCustomizationResponse

  // Timeline Endpoints
  @GET("timeline")
  suspend fun getTimeline(
    @Header("Authorization") authHeader: String
  ): List<TimelineEventDto>

  @POST("timeline/generate")
  suspend fun generateTimeline(
    @Header("Authorization") authHeader: String,
    @Body request: TimelineGenerateRequest
  ): TimelineGenerateResponse

  @GET("timeline/event/{id}")
  suspend fun getTimelineEvent(
    @Header("Authorization") authHeader: String,
    @Path("id") eventId: Int
  ): TimelineEventDto

  @GET("timeline/growth-summary")
  suspend fun getGrowthSummary(
    @Header("Authorization") authHeader: String
  ): GrowthSummaryResponse

  // Profile Endpoints
  @GET("profile")
  suspend fun getProfile(
    @Header("Authorization") authHeader: String
  ): ProfileResponse

  @PUT("profile/update")
  suspend fun updateProfile(
    @Header("Authorization") authHeader: String,
    @Body request: ProfileUpdateRequest
  ): ProfileResponse

  @GET("profile/insights")
  suspend fun getProfileInsights(
    @Header("Authorization") authHeader: String
  ): ProfileInsightsResponse

  @POST("profile/reset-data")
  suspend fun resetProfileData(
    @Header("Authorization") authHeader: String
  ): ProfileResetResponse

  @POST("profile/export")
  suspend fun exportProfileData(
    @Header("Authorization") authHeader: String
  ): ProfileExportResponse

  // Settings Endpoints
  @GET("settings")
  suspend fun getSettings(
    @Header("Authorization") authHeader: String
  ): SettingsResponse

  @PUT("settings/update")
  suspend fun updateSettings(
    @Header("Authorization") authHeader: String,
    @Body request: SettingsUpdateRequest
  ): SettingsResponse

  @POST("settings/reset-memory")
  suspend fun resetSettingsMemory(
    @Header("Authorization") authHeader: String
  ): SettingsResetMemoryResponse

  companion object {
    private const val BASE_URL = "http://10.0.2.2:8000/" // Default for localhost FastAPI in Android emulator

    fun create(baseUrl: String = BASE_URL): CompanionApiService {
      val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

      val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor { chain ->
          val originalRequest = chain.request()
          val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
          val request = requestBuilder.build()
          
          // Log request
          android.util.Log.d("CompanionApiService", "API Request: ${request.method} ${request.url}")
          
          val response = chain.proceed(request)
          
          // Log response
          android.util.Log.d("CompanionApiService", "API Response: ${response.code} ${request.url}")
          
          response
        }
        .build()

      return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(CompanionApiService::class.java)
    }
  }
}

