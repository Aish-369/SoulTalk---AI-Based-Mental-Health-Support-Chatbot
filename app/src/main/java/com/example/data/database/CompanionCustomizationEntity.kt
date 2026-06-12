package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companion_customization")
data class CompanionCustomizationEntity(
  @PrimaryKey val id: String,
  val user_id: Int = 1,
  val item_name: String,
  val icon: String,
  val category: String,
  val unlocked: Boolean = false,
  val unlock_level: Int,
  val equipped: Boolean = false
)
