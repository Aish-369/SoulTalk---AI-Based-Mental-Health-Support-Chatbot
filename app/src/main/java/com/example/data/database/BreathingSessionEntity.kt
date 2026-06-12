package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breathing_sessions")
data class BreathingSessionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  val user_id: Int = 1,
  val session_type: String,
  val duration: Int, // in seconds
  val cycles_completed: Int,
  val xp_earned: Int,
  val created_at: Long = System.currentTimeMillis()
)
