package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companion_memories")
data class CompanionMemoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val user_id: Int = 1,
  val memory_title: String,
  val memory_description: String,
  val icon: String,
  val category: String,
  val created_at: Long = System.currentTimeMillis()
)
