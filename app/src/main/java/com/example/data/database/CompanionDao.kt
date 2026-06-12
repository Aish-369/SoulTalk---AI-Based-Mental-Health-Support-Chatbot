package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionDao {
  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  fun getUserFlow(userId: Int = 1): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  suspend fun getUserSync(userId: Int = 1): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Query("SELECT * FROM companion_progress WHERE user_id = :userId LIMIT 1")
  fun getProgressFlow(userId: Int = 1): Flow<CompanionProgressEntity?>

  @Query("SELECT * FROM companion_progress WHERE user_id = :userId LIMIT 1")
  suspend fun getProgressSync(userId: Int = 1): CompanionProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProgress(progress: CompanionProgressEntity)

  @Query("SELECT * FROM voice_memories WHERE user_id = :userId ORDER BY created_at DESC")
  fun getVoiceMemoriesFlow(userId: Int = 1): Flow<List<VoiceMemoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVoiceMemory(memory: VoiceMemoryEntity)

  @Query("DELETE FROM voice_memories WHERE id = :id")
  suspend fun deleteVoiceMemoryById(id: Int)

  @Query("SELECT * FROM breathing_sessions WHERE user_id = :userId ORDER BY created_at DESC")
  fun getBreathingSessionsFlow(userId: Int = 1): Flow<List<BreathingSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBreathingSession(session: BreathingSessionEntity)

  @Query("DELETE FROM users")
  suspend fun clearUsers()

  @Query("DELETE FROM companion_progress")
  suspend fun clearProgress()

  // Companion Memories
  @Query("SELECT * FROM companion_memories WHERE user_id = :userId ORDER BY created_at DESC")
  fun getMemoriesFlow(userId: Int = 1): Flow<List<CompanionMemoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: CompanionMemoryEntity)

  @Query("DELETE FROM companion_memories WHERE id = :id")
  suspend fun deleteMemoryById(id: Int)

  // Achievements
  @Query("SELECT * FROM achievements WHERE user_id = :userId")
  fun getAchievementsFlow(userId: Int = 1): Flow<List<AchievementEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAchievement(achievement: AchievementEntity)

  @Query("UPDATE achievements SET unlocked = :unlocked, unlocked_at = :unlockedAt, progress = :progress WHERE id = :id AND user_id = :userId")
  suspend fun updateAchievement(id: String, userId: Int, unlocked: Boolean, unlockedAt: Long?, progress: Int)

  // Customization
  @Query("SELECT * FROM companion_customization WHERE user_id = :userId")
  fun getCustomizationFlow(userId: Int = 1): Flow<List<CompanionCustomizationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomizationItem(item: CompanionCustomizationEntity)

  @Query("UPDATE companion_customization SET equipped = :equipped WHERE id = :id AND user_id = :userId")
  suspend fun updateCustomizationEquipped(id: String, userId: Int, equipped: Boolean)

  // Timeline Events
  @Query("SELECT * FROM timeline_events WHERE user_id = :userId ORDER BY created_at DESC")
  fun getTimelineEventsFlow(userId: Int = 1): Flow<List<TimelineEventEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTimelineEvent(event: TimelineEventEntity)

  @Query("DELETE FROM timeline_events WHERE id = :id")
  suspend fun deleteTimelineEventById(id: Int)

  @Query("DELETE FROM timeline_events WHERE user_id = :userId")
  suspend fun clearTimelineEvents(userId: Int = 1)

  // User Preferences
  @Query("SELECT * FROM user_preferences WHERE user_id = :userId LIMIT 1")
  fun getUserPreferencesFlow(userId: Int = 1): Flow<UserPreferencesEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUserPreferences(preferences: UserPreferencesEntity)

  @Query("UPDATE user_preferences SET ai_tone = :aiTone WHERE user_id = :userId")
  suspend fun updateAITone(userId: Int, aiTone: String)

  @Query("UPDATE user_preferences SET language = :language WHERE user_id = :userId")
  suspend fun updateLanguage(userId: Int, language: String)

  @Query("UPDATE user_preferences SET ai_memory_enabled = :enabled WHERE user_id = :userId")
  suspend fun updateAiMemoryEnabled(userId: Int, enabled: Boolean)

  @Query("UPDATE user_preferences SET notifications_enabled = :enabled WHERE user_id = :userId")
  suspend fun updateNotificationsEnabled(userId: Int, enabled: Boolean)
}
