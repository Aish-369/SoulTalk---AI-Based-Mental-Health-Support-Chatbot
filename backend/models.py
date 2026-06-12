import time
from sqlalchemy import Column, Integer, String, BigInteger, ForeignKey, Float
from .database import Base

class UserModel(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    password_hash = Column(String, nullable=True) # Hashed password matching security requirements
    language = Column(String, default="en")
    companion_type = Column(String, default="mochi_cat")
    companion_name = Column(String, default="Mochi")
    personality_type = Column(String, default="Calm, Friendly, Comforting")
    created_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class MoodLogModel(Base):
    __tablename__ = "mood_logs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    mood = Column(String, nullable=False)
    emotion = Column(String, nullable=True)
    score = Column(Integer, default=50) # Emotional scale score (e.g. 1 to 100)
    notes = Column(String, nullable=True)
    created_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class ChatMessageModel(Base):
    __tablename__ = "chat_messages"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    role = Column(String, nullable=False) # "user" or "companion"
    message = Column(String, nullable=False)
    emotion = Column(String, nullable=True) # e.g. "happy", "stressed"
    created_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class VoiceConversationModel(Base):
    __tablename__ = "voice_conversations"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    transcript = Column(String, nullable=False)
    emotion = Column(String, nullable=True)
    confidence = Column(Float, nullable=True)
    duration = Column(Integer, default=0)
    created_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class EmotionalWeatherModel(Base):
    __tablename__ = "emotional_weather"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    weather = Column(String, nullable=False)
    generated_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class CompanionProgressModel(Base):
    __tablename__ = "companion_progress"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, unique=True, nullable=False)
    level = Column(Integer, default=1)
    xp = Column(Integer, default=0)
    stage = Column(String, default="Baby Companion")
    updated_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class CompanionMemoryModel(Base):
    __tablename__ = "companion_memories"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    memory_title = Column(String, nullable=False)
    memory_description = Column(String, nullable=False)
    icon = Column(String, nullable=False)
    category = Column(String, nullable=False)
    created_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class AchievementModel(Base):
    __tablename__ = "achievements"

    id = Column(String, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    achievement_name = Column(String, nullable=False)
    description = Column(String, nullable=False)
    icon = Column(String, nullable=False)
    unlocked = Column(Integer, default=0)  # 0 = false, 1 = true
    unlocked_at = Column(BigInteger, nullable=True)
    progress = Column(Integer, default=0)
    max_progress = Column(Integer, default=1)

class CompanionCustomizationModel(Base):
    __tablename__ = "companion_customization"

    id = Column(String, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    item_name = Column(String, nullable=False)
    icon = Column(String, nullable=False)
    category = Column(String, nullable=False)
    unlocked = Column(Integer, default=0)  # 0 = false, 1 = true
    unlock_level = Column(Integer, default=1)
    equipped = Column(Integer, default=0)  # 0 = false, 1 = true

class TimelineEventModel(Base):
    __tablename__ = "timeline_events"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    event_type = Column(String, nullable=False)
    emotion = Column(String, nullable=False)
    emotion_icon = Column(String, nullable=False)
    title = Column(String, nullable=False)
    description = Column(String, nullable=False)
    ai_reflection = Column(String, nullable=False)
    emotional_weather = Column(String, nullable=False)
    created_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

class UserPreferencesModel(Base):
    __tablename__ = "user_preferences"

    user_id = Column(Integer, primary_key=True, index=True)
    theme = Column(String, default="light")
    notifications_enabled = Column(Integer, default=1)  # 0 = false, 1 = true
    ai_memory_enabled = Column(Integer, default=1)  # 0 = false, 1 = true
    voice_enabled = Column(Integer, default=1)  # 0 = false, 1 = true
    ai_tone = Column(String, default="Gentle Friend")
    language = Column(String, default="en")
    mood_reminders = Column(Integer, default=1)  # 0 = false, 1 = true
    journal_reminders = Column(Integer, default=1)  # 0 = false, 1 = true
    breathing_reminders = Column(Integer, default=1)  # 0 = false, 1 = true
    voice_reminders = Column(Integer, default=0)  # 0 = false, 1 = true
    emotion_sensitivity = Column(String, default="Medium")
    response_style = Column(String, default="Balanced")
    voice_speed = Column(Float, default=1.0)
    voice_tone = Column(String, default="Soft")
    biometric_enabled = Column(Integer, default=0)  # 0 = false, 1 = true
    offline_data_enabled = Column(Integer, default=1)  # 0 = false, 1 = true
    privacy_level = Column(String, default="Standard")
    updated_at = Column(BigInteger, default=lambda: int(time.time() * 1000))
