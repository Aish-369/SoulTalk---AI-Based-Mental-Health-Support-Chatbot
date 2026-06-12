package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: Int = 1,
  val name: String = "",
  val email: String = "",
  val language: String = "en",
  val companion_type: String,
  val companion_name: String,
  val personality_type: String,
  val created_at: Long = System.currentTimeMillis()
)
