package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
  @PrimaryKey val user_id: Int,
  val theme: String = "light",
  val notifications_enabled: Boolean = true,
  val ai_memory_enabled: Boolean = true,
  val voice_enabled: Boolean = true,
  val ai_tone: String = "Gentle Friend",
  val language: String = "en",
  val mood_reminders: Boolean = true,
  val journal_reminders: Boolean = true,
  val breathing_reminders: Boolean = true,
  val voice_reminders: Boolean = false,
  val emotion_sensitivity: String = "Medium",
  val response_style: String = "Balanced",
  val voice_speed: Float = 1.0f,
  val voice_tone: String = "Soft",
  val biometric_enabled: Boolean = false,
  val offline_data_enabled: Boolean = true,
  val privacy_level: String = "Standard",
  val updated_at: Long = System.currentTimeMillis()
)
