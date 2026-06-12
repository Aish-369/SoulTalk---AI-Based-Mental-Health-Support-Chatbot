package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeline_events")
data class TimelineEventEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val user_id: Int = 1,
  val event_type: String,
  val emotion: String,
  val emotion_icon: String,
  val title: String,
  val description: String,
  val ai_reflection: String,
  val emotional_weather: String,
  val created_at: Long = System.currentTimeMillis()
)
