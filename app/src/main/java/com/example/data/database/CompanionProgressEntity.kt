package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companion_progress")
data class CompanionProgressEntity(
  @PrimaryKey val user_id: Int = 1,
  val level: Int = 1,
  val xp: Int = 0,
  val stage: String = "Baby Companion",
  val updated_at: Long = System.currentTimeMillis()
)
