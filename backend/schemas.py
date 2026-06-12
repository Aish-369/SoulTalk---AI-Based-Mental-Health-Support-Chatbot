from pydantic import BaseModel, EmailStr
from typing import Optional

class GoogleAuthRequest(BaseModel):
    id_token: str
    companion_type: Optional[str] = None
    companion_name: Optional[str] = None

class RegisterRequest(BaseModel):
    name: str
    email: EmailStr
    secret_hash: str  # The client's hashed/encrypted secret or plain password
    language: str = "en"
    companion_type: str = "mochi_cat"
    companion_name: str = "Mochi"
    personality_type: str = "Calm, Friendly, Comforting"

class LoginRequest(BaseModel):
    email: EmailStr
    secret_hash: str

class RefreshRequest(BaseModel):
    refresh_token: str

class CompanionSelectionRequest(BaseModel):
    companion_type: str
    companion_name: str

class UserDto(BaseModel):
    id: int
    name: str
    email: str
    language: str
    companion_type: str
    companion_name: str
    personality_type: str
    created_at: int

    class Config:
        from_attributes = True

class AuthResponse(BaseModel):
    success: bool
    access_token: str
    refresh_token: str
    user: Optional[UserDto] = None

class RefreshResponse(BaseModel):
    success: bool
    access_token: str
    refresh_token: str

class CompanionSelectionResponse(BaseModel):
    success: bool

class MoodLogRequest(BaseModel):
    mood: str
    notes: Optional[str] = None

class MoodLogResponse(BaseModel):
    weather: str
    success: bool
    score: int
    emotion: str

class ChatSendRequest(BaseModel):
    message: str
    user_id: Optional[str] = None
    emotion: Optional[str] = None

class ChatSendResponse(BaseModel):
    message_id: int
    reply: str
    emotion: str
    confidence: float
    voice_reply_base64: Optional[str] = None # Simulating the friendly slowly paced TTS voice content if needed

class ChatMessageDto(BaseModel):
    id: int
    role: str
    message: str
    emotion: Optional[str] = None
    created_at: int

    class Config:
        from_attributes = True

class ChatContextResponse(BaseModel):
    companion_name: str
    companion_type: str
    personality_type: str
    preferred_language: str
    recent_emotional_trends: list[str]
    recent_mood: Optional[str] = None

class VoiceStartRequest(BaseModel):
    voice_personality: str = "Gentle Friend"

class VoiceStartResponse(BaseModel):
    success: bool
    session_id: str
    greeting: str
    companion_name: str
    companion_type: str

class VoiceProcessRequest(BaseModel):
    transcript: str
    tone: Optional[str] = "neutral"
    speed: Optional[float] = 1.0
    energy: Optional[float] = 1.0
    duration: Optional[int] = 0

class VoiceProcessResponse(BaseModel):
    success: bool
    detected_emotion: str
    confidence: float
    is_crisis: bool

class VoiceResponseRequest(BaseModel):
    transcript: str
    detected_emotion: str
    voice_personality: str = "Gentle Friend"

class VoiceResponseResponse(BaseModel):
    reply_text: str
    voice_pitch: float
    voice_speed: float
    character_expression: str

class VoiceConversationDto(BaseModel):
    id: int
    user_id: int
    transcript: str
    emotion: Optional[str] = None
    confidence: Optional[float] = 0.0
    duration: int
    created_at: int

    class Config:
        from_attributes = True

class MoodLogDto(BaseModel):
    id: int
    user_id: int
    mood: str
    emotion: str
    score: int
    notes: Optional[str] = None
    created_at: int

    class Config:
        from_attributes = True

class EmotionalWeatherDto(BaseModel):
    id: int
    user_id: int
    weather: str
    generated_at: int

    class Config:
        from_attributes = True

class AIInsightsResponse(BaseModel):
    weekly_summary: str
    achievements: list[str]
    growth_areas: list[str]
    personalized_encouragement: str
    insights: list[str]
    most_common_emotion: str
    best_day_of_week: str
    most_positive_time: str
    stress_triggers: str
    mood_improvement_factors: str

# Companion Home Screen Schemas
class CompanionStatusResponse(BaseModel):
    companion_name: str
    companion_type: str
    level: int
    xp: int
    stage: str
    mood: str
    friendship_level: str
    today_activity: str

class CompanionUpdateRequest(BaseModel):
    level: int
    xp: int
    stage: str

class CompanionUpdateResponse(BaseModel):
    success: bool
    new_level: int
    new_xp: int
    new_stage: str

class CompanionMemoryDto(BaseModel):
    id: int
    user_id: int
    memory_title: str
    memory_description: str
    icon: str
    category: str
    created_at: int

    class Config:
        from_attributes = True

class CompanionMemoryRequest(BaseModel):
    memory_title: str
    memory_description: str
    icon: str
    category: str

class AchievementDto(BaseModel):
    id: str
    user_id: int
    achievement_name: str
    description: str
    icon: str
    unlocked: bool
    unlocked_at: Optional[int] = None
    progress: int
    max_progress: int

    class Config:
        from_attributes = True

class CompanionCustomizationRequest(BaseModel):
    item_id: str
    equipped: bool

class CompanionCustomizationResponse(BaseModel):
    success: bool
    message: str

# Timeline Schemas
class TimelineEventDto(BaseModel):
    id: int
    user_id: int
    event_type: str
    emotion: str
    emotion_icon: str
    title: str
    description: str
    ai_reflection: str
    emotional_weather: str
    created_at: int

    class Config:
        from_attributes = True

class TimelineEventRequest(BaseModel):
    event_type: str
    emotion: str
    emotion_icon: str
    title: str
    description: str
    emotional_weather: str

class TimelineGenerateRequest(BaseModel):
    sources: list[str]

class TimelineGenerateResponse(BaseModel):
    success: bool
    events: list[TimelineEventDto]

class GrowthSummaryResponse(BaseModel):
    summary: str

# Profile Schemas
class ProfilePreferencesDto(BaseModel):
    ai_tone: str
    language: str
    notifications_enabled: bool
    ai_memory_enabled: bool
    voice_enabled: bool

class ProfileStatsDto(BaseModel):
    mood_logs_count: int
    journal_entries_count: int
    voice_sessions_count: int
    breathing_sessions_count: int
    stability_score: int
    top_emotion: str

class ProfileResponse(BaseModel):
    user: UserDto
    preferences: ProfilePreferencesDto
    stats: ProfileStatsDto

class ProfileUpdateRequest(BaseModel):
    ai_tone: Optional[str] = None
    language: Optional[str] = None
    notifications_enabled: Optional[bool] = None
    ai_memory_enabled: Optional[bool] = None

class ProfileInsightsResponse(BaseModel):
    emotional_trends: list[str]
    stability_score: int
    top_emotions: list[str]
    monthly_summary: str

class ProfileResetResponse(BaseModel):
    success: bool
    message: str

class ProfileExportResponse(BaseModel):
    success: bool
    data_url: Optional[str] = None
    message: str

# Settings Schemas
class SettingsResponse(BaseModel):
    notifications_enabled: bool
    ai_memory_enabled: bool
    voice_enabled: bool
    ai_tone: str
    language: str
    mood_reminders: bool
    journal_reminders: bool
    breathing_reminders: bool
    voice_reminders: bool
    emotion_sensitivity: str
    response_style: str
    voice_speed: float
    voice_tone: str
    biometric_enabled: bool
    offline_data_enabled: bool
    privacy_level: str

class SettingsUpdateRequest(BaseModel):
    notifications_enabled: Optional[bool] = None
    ai_memory_enabled: Optional[bool] = None
    voice_enabled: Optional[bool] = None
    ai_tone: Optional[str] = None
    language: Optional[str] = None
    mood_reminders: Optional[bool] = None
    journal_reminders: Optional[bool] = None
    breathing_reminders: Optional[bool] = None
    voice_reminders: Optional[bool] = None
    emotion_sensitivity: Optional[str] = None
    response_style: Optional[str] = None
    voice_speed: Optional[float] = None
    voice_tone: Optional[str] = None
    biometric_enabled: Optional[bool] = None
    offline_data_enabled: Optional[bool] = None

class SettingsResetMemoryResponse(BaseModel):
    success: bool
    message: str

class DataDeletionRequest(BaseModel):
    password: str
    confirmation: str  # Must be "DELETE_ALL_DATA" to confirm

class DataDeletionResponse(BaseModel):
    success: bool
    message: str
    deleted_records: int


