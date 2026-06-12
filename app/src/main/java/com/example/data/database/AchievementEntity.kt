package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
  @PrimaryKey val id: String,
  val user_id: Int = 1,
  val achievement_name: String,
  val description: String,
  val icon: String,
  val unlocked: Boolean = false,
  val unlocked_at: Long? = null,
  val progress: Int = 0,
  val max_progress: Int = 1
)
