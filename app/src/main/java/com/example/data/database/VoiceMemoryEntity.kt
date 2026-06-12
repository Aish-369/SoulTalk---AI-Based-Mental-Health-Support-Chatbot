package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_memories")
data class VoiceMemoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  val user_id: Int = 1,
  val audio_url: String = "",
  val transcript: String,
  val emotion: String,
  val confidence: Double,
  val summary: String,
  val created_at: Long = System.currentTimeMillis()
)
