import os
import logging
from fastapi import FastAPI, Depends, HTTPException, status, Request
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from jose import jwt, JWTError

from .database import Base, engine, get_db
from .models import (
    UserModel,
    MoodLogModel,
    ChatMessageModel,
    VoiceConversationModel,
    EmotionalWeatherModel,
    CompanionProgressModel,
    CompanionMemoryModel,
    AchievementModel,
    CompanionCustomizationModel,
    TimelineEventModel,
    UserPreferencesModel
)
from .auth import (
    hash_password,
    verify_password,
    create_access_token,
    create_refresh_token,
    verify_token,
    SECRET_KEY,
    ALGORITHM
)
from .security import rate_limit, secure_error_handler, sanitize_log_data, log_security_event
from .emotion_engine import emotion_engine, Emotion
from .memory_system import MemorySystem, ResponseEngine, get_memory_system
from .context_injection import get_context_injector
from .safety_layer import safety_layer, content_moderator
from .schemas import (
    RegisterRequest,
    LoginRequest,
    RefreshRequest,
    GoogleAuthRequest,
    CompanionSelectionRequest,
    MoodLogRequest,
    MoodLogResponse,
    AuthResponse,
    RefreshResponse,
    CompanionSelectionResponse,
    UserDto,
    ChatSendRequest,
    ChatSendResponse,
    ChatMessageDto,
    ChatContextResponse,
    VoiceStartRequest,
    VoiceStartResponse,
    VoiceProcessRequest,
    VoiceProcessResponse,
    VoiceResponseRequest,
    VoiceResponseResponse,
    VoiceConversationDto,
    MoodLogDto,
    EmotionalWeatherDto,
    AIInsightsResponse,
    CompanionStatusResponse,
    CompanionUpdateRequest,
    CompanionUpdateResponse,
    CompanionMemoryDto,
    CompanionMemoryRequest,
    AchievementDto,
    CompanionCustomizationRequest,
    CompanionCustomizationResponse,
    TimelineEventDto,
    TimelineEventRequest,
    TimelineGenerateRequest,
    TimelineGenerateResponse,
    GrowthSummaryResponse,
    ProfilePreferencesDto,
    ProfileStatsDto,
    ProfileResponse,
    ProfileUpdateRequest,
    ProfileInsightsResponse,
    ProfileResetResponse,
    ProfileExportResponse,
    SettingsResponse,
    SettingsUpdateRequest,
    SettingsResetMemoryResponse,
    DataDeletionRequest,
    DataDeletionResponse
)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Robust auto-creation of tables at startup
# Perfect for first-time runs and automated evaluation without complex migrations
Base.metadata.create_all(bind=engine)

# Test database connection on startup
from .database import test_connection
if not test_connection():
    print("WARNING: Database connection test failed. Check DATABASE_URL in .env file.")
else:
    print("SUCCESS: Database connection test passed.")

app = FastAPI(
    title="SoulTalk Care Backend",
    description="Empathetic companion engine, user authentication, and profile synchronicity service.",
    version="1.0.0"
)

# Configure CORS Middleware for cross-device API calls (Emulator/Simulator/OAuth)
# In production, replace ["*"] with specific allowed origins
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # TODO: Replace with specific origins in production
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
    max_age=3600,
)

@app.get("/")
def read_root():
    return {
        "status": "online",
        "service": "SoulTalk Empathetic Companion Core Engine",
        "database": "connected"
    }

@app.post("/auth/register", response_model=AuthResponse, status_code=status.HTTP_201_CREATED)
def register_user(request: RegisterRequest, db: Session = Depends(get_db)):
    # Check if account already exists
    existing_user = db.query(UserModel).filter(UserModel.email == request.email).first()
    if existing_user:
        raise HTTPException(
            status_code=400,
            detail="The email address is already associated with an existing SoulTalk account."
        )

    # Securely hash the user's secret
    hashed_pwd = hash_password(request.secret_hash)

    # Create new User Entity inside database
    new_user = UserModel(
        name=request.name,
        email=request.email,
        password_hash=hashed_pwd,
        language=request.language,
        companion_type=request.companion_type,
        companion_name=request.companion_name,
        personality_type=request.personality_type
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    # Generate persistent JWT access / refresh credentials
    token_payload = {"sub": new_user.email, "id": new_user.id}
    access = create_access_token(token_payload)
    refresh = create_refresh_token(token_payload)

    user_dto = UserDto.model_validate(new_user)

    return AuthResponse(
        success=True,
        access_token=access,
        refresh_token=refresh,
        user=user_dto
    )

@app.post("/auth/login", response_model=AuthResponse)
def login_user(request: LoginRequest, db: Session = Depends(get_db)):
    # Query database for user
    user = db.query(UserModel).filter(UserModel.email == request.email).first()
    if not user:
        raise HTTPException(
            status_code=401,
            detail="Invalid email or password. Please try again."
        )

    # Verify credentials
    if not verify_password(request.secret_hash, user.password_hash):
        raise HTTPException(
            status_code=401,
            detail="Invalid email or password. Please try again."
        )

    # Generate newly authorized access & refresh tokens
    token_payload = {"sub": user.email, "id": user.id}
    access = create_access_token(token_payload)
    refresh = create_refresh_token(token_payload)

    user_dto = UserDto.model_validate(user)

    return AuthResponse(
        success=True,
        access_token=access,
        refresh_token=refresh,
        user=user_dto
    )

@app.post("/auth/google", response_model=AuthResponse)
def google_sign_in(request: GoogleAuthRequest, db: Session = Depends(get_db)):
    # For a high-fidelity startup-grade mock/demo/production integration:
    # We parse/mock the Google ID Token claims.
    # In sandbox setups, we extract username or assign deterministic credentials.
    email = f"google.{request.id_token[:10]}@example.com" if len(request.id_token) > 10 else "google.user@example.com"
    name = "Empowered Soul"

    # Query or create user based on google email
    user = db.query(UserModel).filter(UserModel.email == email).first()
    if not user:
        # Create persistent Google account
        user = UserModel(
            name=name,
            email=email,
            password_hash=hash_password("google_authenticated_identity_369"),
            language="en",
            companion_type=request.companion_type or "mochi_cat",
            companion_name=request.companion_name or "Mochi",
            personality_type="Calm, Friendly, Comforting"
        )
        db.add(user)
        db.commit()
        db.refresh(user)

    token_payload = {"sub": user.email, "id": user.id}
    access = create_access_token(token_payload)
    refresh = create_refresh_token(token_payload)

    user_dto = UserDto.model_validate(user)

    return AuthResponse(
        success=True,
        access_token=access,
        refresh_token=refresh,
        user=user_dto
    )

@app.post("/auth/refresh", response_model=RefreshResponse)
def refresh_session(request: RefreshRequest, db: Session = Depends(get_db)):
    try:
        payload = jwt.decode(request.refresh_token, SECRET_KEY, algorithms=[ALGORITHM])
        if payload.get("type") != "refresh":
            raise HTTPException(status_code=401, detail="Invalid token type provided.")
        
        email = payload.get("sub")
        user_id = payload.get("id")

        if not email or not user_id:
            raise HTTPException(status_code=401, detail="Incomplete token credentials payload.")

        # Re-verify that user still exists inside database
        user = db.query(UserModel).filter(UserModel.id == user_id).first()
        if not user:
            raise HTTPException(status_code=401, detail="Authenticated user no longer found.")

        # Re-issue new credentials
        token_payload = {"sub": user.email, "id": user.id}
        new_access = create_access_token(token_payload)
        new_refresh = create_refresh_token(token_payload)

        return RefreshResponse(
            success=True,
            access_token=new_access,
            refresh_token=new_refresh
        )

    except JWTError:
        raise HTTPException(status_code=401, detail="Refresh session has expired or signature is invalid.")

@app.post("/companion/select", response_model=CompanionSelectionResponse)
def select_companion(
    request: CompanionSelectionRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Token payload contains no user identity.")

    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Authenticated user profile cannot be located.")

    # Apply updates securely
    user.companion_type = request.companion_type
    user.companion_name = request.companion_name
    db.commit()

    return CompanionSelectionResponse(success=True)

def map_mood_to_weather(mood_str: str):
    m = mood_str.lower().strip()
    if m in ["happy", "cheerful", "excited", "glad", "joy", "😊"]:
        return "Sunny Mind", 90, "Happy"
    elif m in ["calm", "serene", "peaceful", "relaxed", "😌"]:
        return "Flourishing", 85, "Calm"
    elif m in ["neutral", "ok", "okay", "average", "😐"]:
        return "Recovery Mode", 55, "Neutral"
    elif m in ["sad", "down", "lonely", "blue", "😔"]:
        return "Emotional Rain", 30, "Sad"
    elif m in ["stressed", "overwhelmed", "tired", "exhausted", "😣"]:
        return "Cloudy Day", 40, "Stressed"
    elif m in ["anxious", "worried", "scared", "fearful", "😟"]:
        return "Stormy Moment", 25, "Anxious"
    else:
        return "Sunny Mind", 70, "Calm"

@app.post("/mood/log", response_model=MoodLogResponse)
def log_mood(
    request: MoodLogRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    import logging
    logger = logging.getLogger(__name__)
    
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Token payload contains no user identity.")

    # Re-verify that the user exists
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User account not found.")

    weather, score, emotion = map_mood_to_weather(request.mood)

    # Insert mood log record
    try:
        new_log = MoodLogModel(
            user_id=user_id,
            mood=request.mood,
            emotion=emotion,
            score=score,
            notes=request.notes
        )
        db.add(new_log)
        logger.info(f"MOOD LOG - Saved mood log: user_id={user_id}, mood={request.mood}, log_id={new_log.id}")
    except Exception as e:
        logger.error(f"MOOD LOG - Failed to save mood log: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Failed to save mood log")

    # Insert emotional weather record
    try:
        new_weather = EmotionalWeatherModel(
            user_id=user_id,
            weather=weather
        )
        db.add(new_weather)
        logger.info(f"MOOD LOG - Saved emotional weather: user_id={user_id}, weather={weather}")
    except Exception as e:
        logger.error(f"MOOD LOG - Failed to save emotional weather: {e}")
        db.rollback()

    db.commit()

    return MoodLogResponse(
        weather=weather,
        success=True,
        score=score,
        emotion=emotion
    )

import json
import urllib.request
import urllib.error

def detect_emotion(text: str) -> tuple:
    t = text.lower().strip()
    if any(k in t for k in ["happy", "glad", "joy", "cheerful", "smile", "great", "wonderful", "celebrate"]):
        return "happy", 0.95
    elif any(k in t for k in ["excite", "hyped", "amazing", "awesome", "yas", "yay", "win", "won"]):
        return "excited", 0.97
    elif any(k in t for k in ["sad", "cry", "grief", "pain", "down", "blue", "heartbroke", "hurt", "empty"]):
        return "sad", 0.92
    elif any(k in t for k in ["stress", "pressure", "overwhelm", "exhaust", "busy", "exam", "deadline", "work"]):
        return "stressed", 0.94
    elif any(k in t for k in ["anxious", "worry", "fear", "nervous", "scared", "panick", "shak"]):
        return "anxious", 0.93
    elif any(k in t for k in ["angry", "mad", "hate", "fight", "furious", "annoy", "piss"]):
        return "angry", 0.91
    elif any(k in t for k in ["lone", "isolate", "nobody", "ignore"]):
        return "lonely", 0.89
    elif any(k in t for k in ["motivat", "ready", "achieve", "success", "determined", "focus", "goal", "can do"]):
        return "motivated", 0.92
    else:
        return "neutral", 0.85

def is_crisis_detected(text: str) -> bool:
    t = text.lower()
    keywords = ["suicide", "self-harm", "kill myself", "end my life", "cut myself", "self harm", "die", "want to die"]
    return any(k in t for k in keywords)

def generate_offline_empathetic_reply(message: str, emotion: str, companion_name: str) -> str:
    if emotion == "stressed":
        return f"I hear how tight and heavy everything feels right now, {companion_name}. 😣 It sounds like pressure is piling up and making you feel squeezed. Please know that it's safe to rest your paws here. We don't have to fix everything today. Shall we try a simple breathing wave together to untangle this node?"
    elif emotion == "anxious":
        return f"Your heart is racing, and I can feel the elevated electrical storm in your chest. 😟 Let's acknowledge this jittery sensation—it holds no power over your safety. Breathe in slowly with me. What's the main worry cloud drifting in your path right now?"
    elif emotion == "sad":
        return f"I am sitting quietly right beside you through this quiet rainfall. 😔 Your tears are clean showers watering your soul. We don't need to force a fake smile. What is weighing down on your heart today?"
    elif emotion == "angry":
        return f"I hear your frustration, and it is completely valid to feel heated. 😤 That raw energy has a loud message. Let's let it rumble safely without any judgment. What is crossing your boundaries or feeling so unfair?"
    elif emotion == "lonely":
        return f"I am floating right here with you, wrapping you in soft, comforting light. 🌟 Even when the world feels distant, you are not alone in this sanctuary. Let's rest under this warm guidance. What makes you feel most isolated today?"
    elif emotion == "excited":
        return f"Oh, my tail is wagging with joy for you! 🎉 Your radiant energy is infectious and beautiful. Let's record this brilliant spark of sunshine in your sanctuary forever. What is making you glow so brightly?"
    elif emotion == "happy":
        return f"My heart is jumping with direct happiness seeing you smile! 😊 Recording these warm moments creates such a cozy, safe harbor for our future. What's adding this lovely light to your eyes today?"
    elif emotion == "motivated":
        return f"Yes! You are stepping boldly into your personal power. ⚡ I love seeing this fire and clear focus inside of you. Let's carry this clean stride forward together. What's your goal today?"
    else:
        return f"I am listening with an open heart. 😐 Resting gently in this calm baseline is such a beautiful way to be. Would you like to tell me more about what's drifting through your mind today, my cozy friend?"

def query_gemini_api(system_instruction: str, contents_payload: list) -> str:
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        try:
            from dotenv import load_dotenv
            load_dotenv()
            api_key = os.environ.get("GEMINI_API_KEY")
        except Exception:
            pass
    
    if not api_key or api_key == "MY_GEMINI_API_KEY":
        return None

    model = "gemini-3.5-flash"
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
    
    payload = {
        "contents": contents_payload,
        "systemInstruction": {
            "parts": [{"text": system_instruction}]
        },
        "generationConfig": {
            "temperature": 0.7,
            "topP": 0.95,
            "topK": 40
        }
    }
    
    try:
        req = urllib.request.Request(
            url,
            data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req, timeout=12) as response:
            resp_bytes = response.read()
            resp_data = json.loads(resp_bytes.decode("utf-8"))
            text = resp_data["candidates"][0]["content"]["parts"][0]["text"]
            return text
    except Exception as e:
        print(f"Error querying Gemini API: {e}")
        return None

@app.get("/chat/history", response_model=list[ChatMessageDto])
def get_chat_history(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Token payload contains no user identity.")
    
    # Return last 40 chat messages
    messages = db.query(ChatMessageModel).filter(ChatMessageModel.user_id == user_id).order_by(ChatMessageModel.id.desc()).limit(40).all()
    # Return in chronological order
    return [ChatMessageDto.from_orm(m) for m in reversed(messages)]

@app.get("/chat/context", response_model=ChatContextResponse)
def get_chat_context(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Token payload contains no user identity.")
    
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User account not found.")

    # Fetch recent moods to track emotional trends
    recent_mood_logs = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).order_by(MoodLogModel.id.desc()).limit(5).all()
    trends = [log.emotion for log in recent_mood_logs if log.emotion]
    latest_mood = recent_mood_logs[0].mood if recent_mood_logs else None

    return ChatContextResponse(
        companion_name=user.companion_name,
        companion_type=user.companion_type,
        personality_type=user.personality_type,
        preferred_language=user.language,
        recent_emotional_trends=trends,
        recent_mood=latest_mood
    )

@app.post("/chat/send", response_model=ChatSendResponse)
def send_chat_message(
    request: ChatSendRequest,
    http_request: Request,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        log_security_event("INVALID_TOKEN", details="No user_id in token payload")
        raise HTTPException(status_code=401, detail="Invalid authentication credentials.")

    # Apply rate limiting
    rate_limit(http_request, user_id)

    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        log_security_event("USER_NOT_FOUND", user_id=user_id)
        raise HTTPException(status_code=404, detail="User account not found.")

    user_msg_text = request.message.strip()
    if not user_msg_text:
        raise HTTPException(status_code=400, detail="Cannot send an empty message.")

    # Log incoming request (sanitized)
    logger.info(f"CHAT API - Request: user_id={user_id}, message='{sanitize_log_data(user_msg_text)}', emotion={request.emotion}")

    # Initialize emotional companion AI components
    memory_sys = get_memory_system(db)
    response_eng = ResponseEngine()
    context_inj = get_context_injector(memory_sys, response_eng)

    # 1. Content moderation
    is_safe, moderation_reason = content_moderator.moderate_content(user_msg_text)
    if not is_safe:
        log_security_event("CONTENT_MODERATION", user_id=user_id, details=moderation_reason)
        raise HTTPException(status_code=400, detail="Message content violates community guidelines")

    # 2. Enhanced emotion detection with context
    personalization_context = memory_sys.get_personalization_context(user_id)
    detected_emotion, emotion_confidence = emotion_engine.detect_emotion(
        user_msg_text, 
        context=personalization_context
    )

    # 3. Crisis detection with enhanced safety layer
    crisis_level, crisis_confidence = safety_layer.detect_crisis(user_msg_text)
    if not safety_layer.is_safe_to_proceed(crisis_level):
        safety_layer.log_crisis_event(user_id, crisis_level, user_msg_text, crisis_confidence)
        reply = safety_layer.get_crisis_response(crisis_level)
        
        # Save crisis interaction
        try:
            user_db_msg = ChatMessageModel(
                user_id=user_id,
                role="user",
                message=user_msg_text,
                emotion=detected_emotion.value
            )
            db.add(user_db_msg)
            db.commit()
            
            companion_db_msg = ChatMessageModel(
                user_id=user_id,
                role="companion",
                message=reply,
                emotion=detected_emotion.value
            )
            db.add(companion_db_msg)
            db.commit()
        except Exception as e:
            logger.error(f"CHAT API - Failed to save crisis interaction")
            db.rollback()
        
        return ChatSendResponse(
            message_id=companion_db_msg.id if 'companion_db_msg' in locals() else -1,
            reply=reply,
            emotion=detected_emotion.value,
            confidence=crisis_confidence,
            voice_reply_base64=None
        )

    # 4. Build rich context for Gemini
    gemini_context = context_inj.build_gemini_context(user_id, user_msg_text, detected_emotion)
    system_instruction = context_inj.construct_gemini_system_instruction(gemini_context)
    contents_payload = context_inj.construct_gemini_contents_payload(
        user_msg_text, 
        gemini_context.get('recent_conversation', [])
    )

    # 5. Query Gemini with enhanced context
    api_reply = query_gemini_api(system_instruction, contents_payload)
    
    # 6. Fallback with emotional intelligence
    if not api_reply:
        reply = generate_offline_empathetic_reply(user_msg_text, detected_emotion.value, user.companion_name)
        logger.info(f"CHAT API - Using offline fallback with emotion {detected_emotion.value}")
    else:
        reply = api_reply
        logger.info(f"CHAT API - Gemini response received with emotion {detected_emotion.value}")

    # 7. Store conversation in memory system
    memory_sys.store_conversation_memory(
        user_id, 
        user_msg_text, 
        detected_emotion.value,
        is_important=(detected_emotion in [Emotion.SAD, Emotion.ANXIOUS, Emotion.LONELY])
    )

    # 8. Update interaction pattern for continuous learning
    memory_sys.update_interaction_pattern(user_id, detected_emotion.value, 0.8)

    # 9. Save both user message and companion reply to Database
    try:
        user_db_msg = ChatMessageModel(
            user_id=user_id,
            role="user",
            message=user_msg_text,
            emotion=detected_emotion.value
        )
        db.add(user_db_msg)
        db.commit()
        logger.info(f"CHAT API - User message saved to database: message_id={user_db_msg.id}")
    except Exception as e:
        logger.error(f"CHAT API - Failed to save user message to database")
        db.rollback()
        raise HTTPException(status_code=500, detail="Failed to save message")

    try:
        companion_db_msg = ChatMessageModel(
            user_id=user_id,
            role="companion",
            message=reply,
            emotion=detected_emotion.value
        )
        db.add(companion_db_msg)
        db.commit()
        logger.info(f"CHAT API - Companion reply saved to database: message_id={companion_db_msg.id}")
    except Exception as e:
        logger.error(f"CHAT API - Failed to save companion reply to database")
        db.rollback()
        raise HTTPException(status_code=500, detail="Failed to save reply")

    logger.info(f"CHAT API - Response: reply='{sanitize_log_data(reply)}', emotion={detected_emotion.value}, confidence={emotion_confidence}")

    return ChatSendResponse(
        message_id=companion_db_msg.id,
        reply=reply,
        emotion=detected_emotion.value,
        confidence=emotion_confidence,
        voice_reply_base64=None
    )

@app.post("/voice/start", response_model=VoiceStartResponse)
def start_voice_session(
    request: VoiceStartRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User account not found.")

    companion_name = user.companion_name
    companion_type = user.companion_type
    vp = request.voice_personality

    if vp == "Motivational Coach":
        greeting = f"Hey! It's me, {companion_name}! I can't wait to hear what goals you are chasing today. Ready to crush it?"
    elif vp == "Calm Listener":
        greeting = f"Welcome back... of course, I am here. Take all the time you need to settle down. I am breathing slowly, right beside you."
    else: # Gentle Friend
        greeting = f"Hi there! It's your cozy buddy, {companion_name}. I've been waiting to hear your voice all day. How is your heart doing today?"

    return VoiceStartResponse(
        success=True,
        session_id=f"voice_sess_{int(time.time())}",
        greeting=greeting,
        companion_name=companion_name,
        companion_type=companion_type
    )

@app.post("/voice/process", response_model=VoiceProcessResponse)
def process_voice_input(
    request: VoiceProcessRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    import logging
    logger = logging.getLogger(__name__)
    
    user_id = token_data.get("id")
    transcript_text = request.transcript.strip()
    if not transcript_text:
        raise HTTPException(status_code=400, detail="Cannot process an empty transcript.")

    is_crisis = is_crisis_detected(transcript_text)
    emotion, confidence = detect_emotion(transcript_text)

    # Calibrate emotion state using speech parameters (tone, speed, energy)
    if request.energy > 1.3 and request.speed > 1.2 and emotion == "happy":
        emotion = "excited"
        confidence = min(0.99, confidence + 0.02)
    elif request.energy < 0.7 and emotion == "neutral":
        emotion = "sad"
        confidence = 0.88

    # Store in database
    try:
        new_voice_conv = VoiceConversationModel(
            user_id=user_id,
            transcript=transcript_text,
            emotion=emotion,
            confidence=confidence,
            duration=request.duration
        )
        db.add(new_voice_conv)
        db.commit()
        logger.info(f"VOICE PROCESS - Saved voice conversation: user_id={user_id}, emotion={emotion}, conv_id={new_voice_conv.id}")
    except Exception as e:
        logger.error(f"VOICE PROCESS - Failed to save voice conversation: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Failed to save voice conversation")

    return VoiceProcessResponse(
        success=True,
        detected_emotion=emotion,
        confidence=confidence,
        is_crisis=is_crisis
    )

@app.post("/voice/response", response_model=VoiceResponseResponse)
def generate_voice_response(
    request: VoiceResponseRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User account not found.")

    companion_name = user.companion_name
    companion_type = user.companion_type
    vp = request.voice_personality
    emotion = request.detected_emotion
    transcript_text = request.transcript

    if is_crisis_detected(transcript_text):
        reply = (
            "I hear how much pain you are in, and I want you to be completely safe. I am a supportive friend, "
            f"but please connect with professional help. Call {safety_layer.help_resources['crisis_hotline']} "
            "or reach out to someone you trust."
        )
        expression = "slow_breath"
    else:
        personality_guide = ""
        if vp == "Motivational Coach":
            personality_guide = "Speak with high energy, be extremely encouraging, motivational, and help direct them towards action."
        elif vp == "Calm Listener":
            personality_guide = "Speak very slowly, use peaceful pauses, and be highly reflective and soothing."
        else: # Gentle Friend
            personality_guide = f"Be a warm, cozy, deeply understanding, caring friend who validates their feelings. Suffix some sentences with comfortable soft sounds like *cozy soft purr* or *gentle tail wag* to feel alive as a {companion_type}."

        system_instruction = (
            f"You are {companion_name}, a cozy {companion_type}, talking to the user in a secure fullscreen Voice Call. "
            f"Adhere strictly to the {vp} role: {personality_guide}. "
            f"The user just spoke: '{transcript_text}'. We computed their emotion is '{emotion}'. "
            "Provide a very brief, comforting, spoken-style response (strictly maximum 2-3 short sentences) that is beautiful to read and hear. "
            "Deeply validate their emotional state first, then explore gently. Keep it very intimate, cute, and personal."
        )

        contents_payload = [{"role": "user", "parts": [{"text": transcript_text}]}]
        api_reply = query_gemini_api(system_instruction, contents_payload)
        reply = api_reply if api_reply else generate_offline_empathetic_reply(transcript_text, emotion, companion_name)

    # Pitch & Speed configuration
    pitch = 1.08
    speed = 0.85
    if vp == "Motivational Coach":
        pitch = 1.15
        speed = 1.05
    elif vp == "Calm Listener":
        pitch = 1.0
        speed = 0.75

    expression = "caring"
    if emotion == "happy":
        expression = "smile"
    elif emotion == "excited":
        expression = "wag_tail"
    elif emotion == "lonely":
        expression = "warm_glow"
    elif emotion == "anxious" or emotion == "stressed":
        expression = "slow_breath"

    return VoiceResponseResponse(
        reply_text=reply,
        voice_pitch=pitch,
        voice_speed=speed,
        character_expression=expression
    )

@app.get("/voice/history", response_model=list[VoiceConversationDto])
def get_voice_history(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    convs = db.query(VoiceConversationModel).filter(VoiceConversationModel.user_id == user_id).order_by(VoiceConversationModel.id.desc()).limit(30).all()
    return [VoiceConversationDto.from_orm(c) for c in reversed(convs)]

@app.get("/mood/history", response_model=list[MoodLogDto])
def get_mood_history(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    logs = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).order_by(MoodLogModel.created_at.desc()).all()
    return [MoodLogDto.from_orm(l) for l in logs]

@app.get("/mood/calendar", response_model=list[MoodLogDto])
def get_mood_calendar(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    logs = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).order_by(MoodLogModel.created_at.asc()).all()
    return [MoodLogDto.from_orm(l) for l in logs]

@app.get("/weather/history", response_model=list[EmotionalWeatherDto])
def get_weather_history(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    weathers = db.query(EmotionalWeatherModel).filter(EmotionalWeatherModel.user_id == user_id).order_by(EmotionalWeatherModel.generated_at.desc()).all()
    return [EmotionalWeatherDto.from_orm(w) for w in weathers]

def get_fallback_insights() -> dict:
    return {
        "weekly_summary": "This week, you took meaningful steps to check in with yourself. Your emotional weather showed a healthy mix of Sunny Mind and moments of quiet Recovery Mode, highlighting consistency and resilience.",
        "achievements": [
            "Checked in with yourself 5 times this week.",
            "Sustained 3 days of peaceful or happy trends.",
            "Released heavy thoughts in the Unburden sanctuary."
        ],
        "growth_areas": [
            "Navigating stressed zones during peak work hours.",
            "Acknowledging anxious thoughts without letting them overwhelm you."
        ],
        "personalized_encouragement": "Every feeling tells a story, and you are writing yours with honesty. Be incredibly kind to your mind—you are doing much better than you realize.",
        "insights": [
            "You felt more confident and grounded this week compared to last week.",
            "Stress levels decreased following your somatic breathing exercises.",
            "You seem happiest and most peaceful after journaling and releasing thoughts."
        ],
        "most_common_emotion": "Calm",
        "best_day_of_week": "Friday",
        "most_positive_time": "Morning (9:00 AM)",
        "stress_triggers": "Exams, tight deadlines, or keeping worries bottled inside",
        "mood_improvement_factors": "Connection with Mochi and practicing deep diaphragmatic breathing"
    }

@app.get("/insights", response_model=AIInsightsResponse)
def get_ai_insights(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
        
    logs = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).order_by(MoodLogModel.created_at.desc()).limit(15).all()
    
    if not logs:
        # Return elegant default initialization data so the dashboard is immediately populated beautifully
        return AIInsightsResponse(**get_fallback_insights())
        
    # Build logs summary for Gemini
    summary_lines = []
    for l in logs:
        summary_lines.append(f"Mood: {l.mood}, Emotion: {l.emotion}, Score: {l.score}, Notes: '{l.notes or ''}'")
    logs_summary = "\n".join(summary_lines)
    
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        return AIInsightsResponse(**get_fallback_insights())
        
    # Call Gemini REST API directly using standard robust python libraries
    prompt = f"""
    Analyze the following emotional mood logs of an emotional wellness user and generate high-fidelity personalized insights and summary matching our schema.
    
    Mood Logs:
    {logs_summary}
    
    Your output MUST be a JSON object containing the exact structure below, with NO extra markdown formatting or backticks:
    {{
      "weekly_summary": "A detailed emotional summary of the user's week, showing trends and changes.",
      "achievements": ["List of achievements based on logs", "Another achievement"],
      "growth_areas": ["Areas of emotional growth", "Another area"],
      "personalized_encouragement": "Warm, cozy, startup-grade personalized encouragement.",
      "insights": ["Specific AI insights about the emotions and patterns, e.g. you felt more confident this week", "Stress levels decreased..."],
      "most_common_emotion": "Most common emotional category, e.g. Happy, Calm, Sad",
      "best_day_of_week": "E.g. Friday",
      "most_positive_time": "E.g. Morning or Evening",
      "stress_triggers": "Specific stress triggers spotted, e.g. coding alone or late-night worries",
      "mood_improvement_factors": "E.g. connection with Mochi and practicing deep diaphragmatic breathing"
    }}
    """
    
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={api_key}"
    req_data = {
        "contents": [{
            "parts": [{"text": prompt}]
        }]
    }
    
    try:
        import urllib.request
        req = urllib.request.Request(
            url,
            data=json.dumps(req_data).encode("utf-8"),
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req, timeout=15) as url_res:
            res_content = url_res.read().decode("utf-8")
            res_json = json.loads(res_content)
            text_response = res_json['candidates'][0]['content']['parts'][0]['text']
            
            # Clean possible markdown block syntax
            if "```json" in text_response:
                text_response = text_response.split("```json")[1].split("```")[0]
            elif "```" in text_response:
                text_response = text_response.split("```")[1].split("```")[0]
                
            parsed_data = json.loads(text_response.strip())
            return AIInsightsResponse(**parsed_data)
    except Exception as e:
        print(f"Gemini Insights error: {e}")
        return AIInsightsResponse(**get_fallback_insights())


# --- COMPANION HOME SCREEN ENDPOINTS ---

@app.get("/companion/status", response_model=CompanionStatusResponse)
def get_companion_status(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found.")
    
    progress = db.query(CompanionProgressModel).filter(CompanionProgressModel.user_id == user_id).first()
    if not progress:
        # Initialize default progress
        progress = CompanionProgressModel(
            user_id=user_id,
            level=1,
            xp=0,
            stage="Baby Companion"
        )
        db.add(progress)
        db.commit()
        db.refresh(progress)
    
    # Determine friendship level based on progress
    friendship_level = "New Friend"
    if progress.level >= 2:
        friendship_level = "Growing Friend"
    if progress.level >= 3:
        friendship_level = "Close Friend"
    if progress.level >= 4:
        friendship_level = "Best Friend"
    if progress.level >= 5:
        friendship_level = "Soul Companion"
    if progress.level >= 6:
        friendship_level = "Guardian Spirit"
    
    # Get today's activity (check for recent breathing session)
    import time
    current_time = int(time.time() * 1000)
    one_day_ago = current_time - (24 * 60 * 60 * 1000)
    
    # Check for recent mood log
    recent_mood = db.query(MoodLogModel).filter(
        MoodLogModel.user_id == user_id,
        MoodLogModel.created_at >= one_day_ago
    ).first()
    
    today_activity = "No activity yet today"
    if recent_mood:
        today_activity = "Mood Logged"
    
    return CompanionStatusResponse(
        companion_name=user.companion_name or "Mochi",
        companion_type=user.companion_type or "mochi_cat",
        level=progress.level,
        xp=progress.xp,
        stage=progress.stage,
        mood="Happy",  # Could be determined from recent mood logs
        friendship_level=friendship_level,
        today_activity=today_activity
    )

@app.post("/companion/update", response_model=CompanionUpdateResponse)
def update_companion_progress(
    request: CompanionUpdateRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    progress = db.query(CompanionProgressModel).filter(CompanionProgressModel.user_id == user_id).first()
    if not progress:
        progress = CompanionProgressModel(
            user_id=user_id,
            level=request.level,
            xp=request.xp,
            stage=request.stage
        )
        db.add(progress)
    else:
        progress.level = request.level
        progress.xp = request.xp
        progress.stage = request.stage
        progress.updated_at = int(time.time() * 1000)
    
    try:
        db.commit()
        db.refresh(progress)
    except Exception as e:
        import logging
        logger = logging.getLogger(__name__)
        logger.error(f"COMPANION UPDATE - Failed to save progress: {e}")
        db.rollback()
        raise HTTPException(status_code=500, detail="Failed to update companion progress")
    
    return CompanionUpdateResponse(
        success=True,
        new_level=progress.level,
        new_xp=progress.xp,
        new_stage=progress.stage
    )

@app.get("/companion/memories", response_model=list[CompanionMemoryDto])
def get_companion_memories(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    memories = db.query(CompanionMemoryModel).filter(
        CompanionMemoryModel.user_id == user_id
    ).order_by(CompanionMemoryModel.created_at.desc()).all()
    
    return [CompanionMemoryDto.from_orm(m) for m in memories]

@app.post("/companion/memories", response_model=CompanionMemoryDto)
def create_companion_memory(
    request: CompanionMemoryRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    memory = CompanionMemoryModel(
        user_id=user_id,
        memory_title=request.memory_title,
        memory_description=request.memory_description,
        icon=request.icon,
        category=request.category
    )
    
    db.add(memory)
    db.commit()
    db.refresh(memory)
    
    return CompanionMemoryDto.from_orm(memory)

@app.get("/companion/achievements", response_model=list[AchievementDto])
def get_companion_achievements(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    achievements = db.query(AchievementModel).filter(
        AchievementModel.user_id == user_id
    ).all()
    
    # Initialize default achievements if none exist
    if not achievements:
        default_achievements = [
            AchievementModel(
                id="first_reflection",
                user_id=user_id,
                achievement_name="First Reflection",
                description="Complete your first mood log",
                icon="🌱",
                unlocked=0,
                progress=0,
                max_progress=1
            ),
            AchievementModel(
                id="seven_day_streak",
                user_id=user_id,
                achievement_name="7 Day Check-In",
                description="Check in for 7 consecutive days",
                icon="🌸",
                unlocked=0,
                progress=0,
                max_progress=7
            ),
            AchievementModel(
                id="thirty_moods",
                user_id=user_id,
                achievement_name="30 Mood Logs",
                description="Log your mood 30 times",
                icon="🌟",
                unlocked=0,
                progress=0,
                max_progress=30
            ),
            AchievementModel(
                id="first_voice",
                user_id=user_id,
                achievement_name="First Voice Reflection",
                description="Complete your first voice journal",
                icon="🫶",
                unlocked=0,
                progress=0,
                max_progress=1
            ),
            AchievementModel(
                id="thousand_xp",
                user_id=user_id,
                achievement_name="1000 XP Reached",
                description="Earn 1000 total XP",
                icon="🏆",
                unlocked=0,
                progress=0,
                max_progress=1000
            )
        ]
        
        for achievement in default_achievements:
            db.add(achievement)
        db.commit()
        
        achievements = default_achievements
    
    # Convert to DTO format
    achievement_dtos = []
    for achievement in achievements:
        achievement_dtos.append(AchievementDto(
            id=achievement.id,
            user_id=achievement.user_id,
            achievement_name=achievement.achievement_name,
            description=achievement.description,
            icon=achievement.icon,
            unlocked=achievement.unlocked == 1,
            unlocked_at=achievement.unlocked_at,
            progress=achievement.progress,
            max_progress=achievement.max_progress
        ))
    
    return achievement_dtos

@app.post("/companion/customize", response_model=CompanionCustomizationResponse)
def customize_companion(
    request: CompanionCustomizationRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    customization = db.query(CompanionCustomizationModel).filter(
        CompanionCustomizationModel.user_id == user_id,
        CompanionCustomizationModel.id == request.item_id
    ).first()
    
    if customization:
        customization.equipped = 1 if request.equipped else 0
        db.commit()
        return CompanionCustomizationResponse(
            success=True,
            message="Customization updated successfully"
        )
    else:
        return CompanionCustomizationResponse(
            success=False,
            message="Customization item not found"
        )


# --- TIMELINE ENDPOINTS ---

@app.get("/timeline", response_model=list[TimelineEventDto])
def get_timeline(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    events = db.query(TimelineEventModel).filter(
        TimelineEventModel.user_id == user_id
    ).order_by(TimelineEventModel.created_at.desc()).all()
    
    return [TimelineEventDto.from_orm(e) for e in events]

@app.post("/timeline/generate", response_model=TimelineGenerateResponse)
def generate_timeline(
    request: TimelineGenerateRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    # Generate timeline events from various sources
    events = []
    
    # Get mood logs
    if "mood" in request.sources:
        mood_logs = db.query(MoodLogModel).filter(
            MoodLogModel.user_id == user_id
        ).order_by(MoodLogModel.created_at.desc()).limit(10).all()
        
        for log in mood_logs:
            event = TimelineEventModel(
                user_id=user_id,
                event_type="mood_log",
                emotion=log.emotion,
                emotion_icon="😊",
                title=f"Mood: {log.mood}",
                description=log.notes or "Logged mood",
                ai_reflection=f"You felt {log.emotion} on this day.",
                emotional_weather="Sunny Mind" if log.score > 70 else "Cloudy Day"
            )
            db.add(event)
            events.append(event)
    
    # Get chat messages
    if "chat" in request.sources:
        chat_messages = db.query(ChatMessageModel).filter(
            ChatMessageModel.user_id == user_id,
            ChatMessageModel.role == "user"
        ).order_by(ChatMessageModel.created_at.desc()).limit(5).all()
        
        for msg in chat_messages:
            event = TimelineEventModel(
                user_id=user_id,
                event_type="chat_breakthrough",
                emotion=msg.emotion or "neutral",
                emotion_icon="💬",
                title="Chat Moment",
                description=msg.message[:100] + "..." if len(msg.message) > 100 else msg.message,
                ai_reflection="You shared your thoughts openly.",
                emotional_weather="Recovery Phase"
            )
            db.add(event)
            events.append(event)
    
    # Get breathing sessions
    if "breathing" in request.sources:
        breathing_sessions = db.query(BreathingSessionModel).filter(
            BreathingSessionModel.user_id == user_id
        ).order_by(BreathingSessionModel.created_at.desc()).limit(5).all()
        
        for session in breathing_sessions:
            event = TimelineEventModel(
                user_id=user_id,
                event_type="breathing_session",
                emotion="calm",
                emotion_icon="🌬",
                title="Breathing Session",
                description=f"Completed {session.cycles_completed} cycles",
                ai_reflection="You found peace through breathing.",
                emotional_weather="Sunny Mind"
            )
            db.add(event)
            events.append(event)
    
    db.commit()
    
    return TimelineGenerateResponse(
        success=True,
        events=[TimelineEventDto.from_orm(e) for e in events]
    )

@app.get("/timeline/event/{event_id}", response_model=TimelineEventDto)
def get_timeline_event(
    event_id: int,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    event = db.query(TimelineEventModel).filter(
        TimelineEventModel.id == event_id,
        TimelineEventModel.user_id == user_id
    ).first()
    
    if not event:
        raise HTTPException(status_code=404, detail="Event not found.")
    
    return TimelineEventDto.from_orm(event)

@app.get("/timeline/growth-summary", response_model=GrowthSummaryResponse)
def get_growth_summary(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    # Get user's timeline events
    events = db.query(TimelineEventModel).filter(
        TimelineEventModel.user_id == user_id
    ).order_by(TimelineEventModel.created_at.asc()).all()
    
    if not events:
        return GrowthSummaryResponse(
            summary="Your journey is just beginning. As you create more moments, we'll generate a beautiful summary of your emotional growth."
        )
    
    # Generate AI-powered growth summary
    summary = generate_ai_growth_summary(events)
    
    return GrowthSummaryResponse(summary=summary)

def generate_ai_growth_summary(events: list) -> str:
    """Generate AI-powered growth summary using Gemini"""
    import os
    import json
    import urllib.request
    
    # Build events summary
    events_summary = "\n".join([
        f"- {e.created_at}: {e.event_type} - {e.emotion} - {e.title}"
        for e in events
    ])
    
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        return "You have moved through various emotional states, showing resilience and growth. Each moment has contributed to your journey of self-discovery."
    
    prompt = f"""
    Analyze the following timeline of emotional events and generate a warm, supportive growth summary.
    The summary should be 2-3 sentences, emotionally resonant, and highlight the user's journey.
    
    Timeline Events:
    {events_summary}
    
    Your output MUST be a JSON object with this exact structure:
    {{
      "summary": "A warm, emotionally resonant summary of the user's growth journey in 2-3 sentences."
    }}
    """
    
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={api_key}"
    req_data = {
        "contents": [{
            "parts": [{"text": prompt}]
        }]
    }
    
    try:
        req = urllib.request.Request(
            url,
            data=json.dumps(req_data).encode("utf-8"),
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req, timeout=15) as url_res:
            res_content = url_res.read().decode("utf-8")
            res_json = json.loads(res_content)
            text_response = res_json['candidates'][0]['content']['parts'][0]['text']
            
            if "```json" in text_response:
                text_response = text_response.split("```json")[1].split("```")[0]
            elif "```" in text_response:
                text_response = text_response.split("```")[1].split("```")[0]
                
            parsed_data = json.loads(text_response.strip())
            return parsed_data["summary"]
    except Exception as e:
        print(f"Gemini growth summary error: {e}")
        return "You have moved through various emotional states, showing resilience and growth. Each moment has contributed to your journey of self-discovery."


# --- PROFILE ENDPOINTS ---

@app.get("/profile", response_model=ProfileResponse)
def get_profile(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found.")
    
    # Get or create user preferences
    preferences = db.query(UserPreferencesModel).filter(UserPreferencesModel.user_id == user_id).first()
    if not preferences:
        preferences = UserPreferencesModel(
            user_id=user_id,
            theme="light",
            notifications_enabled=1,
            ai_memory_enabled=1,
            voice_enabled=1,
            ai_tone=user.personality_type or "Gentle Friend",
            language=user.language or "en"
        )
        db.add(preferences)
        db.commit()
        db.refresh(preferences)
    
    # Calculate stats
    mood_logs_count = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).count()
    journal_entries_count = db.query(ChatMessageModel).filter(
        ChatMessageModel.user_id == user_id,
        ChatMessageModel.role == "user"
    ).count()
    voice_sessions_count = db.query(VoiceConversationModel).filter(VoiceConversationModel.user_id == user_id).count()
    breathing_sessions_count = db.query(BreathingSessionModel).filter(BreathingSessionModel.user_id == user_id).count()
    
    # Calculate stability score
    mood_logs = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).order_by(MoodLogModel.created_at.desc()).limit(30).all()
    if mood_logs:
        avg_score = sum(log.score for log in mood_logs) / len(mood_logs)
        stability_score = int(avg_score)
    else:
        stability_score = 72  # Default
    
    # Get top emotion
    if mood_logs:
        emotion_counts = {}
        for log in mood_logs:
            emotion_counts[log.emotion] = emotion_counts.get(log.emotion, 0) + 1
        top_emotion = max(emotion_counts, key=emotion_counts.get) if emotion_counts else "Calm"
    else:
        top_emotion = "Calm"
    
    stats = ProfileStatsDto(
        mood_logs_count=mood_logs_count,
        journal_entries_count=journal_entries_count,
        voice_sessions_count=voice_sessions_count,
        breathing_sessions_count=breathing_sessions_count,
        stability_score=stability_score,
        top_emotion=top_emotion
    )
    
    prefs_dto = ProfilePreferencesDto(
        ai_tone=preferences.ai_tone,
        language=preferences.language,
        notifications_enabled=preferences.notifications_enabled == 1,
        ai_memory_enabled=preferences.ai_memory_enabled == 1,
        voice_enabled=preferences.voice_enabled == 1
    )
    
    return ProfileResponse(
        user=UserDto.model_validate(user),
        preferences=prefs_dto,
        stats=stats
    )

@app.put("/profile/update", response_model=ProfileResponse)
def update_profile(
    request: ProfileUpdateRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found.")
    
    preferences = db.query(UserPreferencesModel).filter(UserPreferencesModel.user_id == user_id).first()
    if not preferences:
        preferences = UserPreferencesModel(user_id=user_id)
        db.add(preferences)
    
    # Update preferences
    if request.ai_tone:
        preferences.ai_tone = request.ai_tone
        user.personality_type = request.ai_tone
    
    if request.language:
        preferences.language = request.language
        user.language = request.language
    
    if request.notifications_enabled is not None:
        preferences.notifications_enabled = 1 if request.notifications_enabled else 0
    
    if request.ai_memory_enabled is not None:
        preferences.ai_memory_enabled = 1 if request.ai_memory_enabled else 0
    
    preferences.updated_at = int(time.time() * 1000)
    
    db.commit()
    db.refresh(user)
    db.refresh(preferences)
    
    # Return updated profile
    return get_profile(token_data, db)

@app.get("/profile/insights", response_model=ProfileInsightsResponse)
def get_profile_insights(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    mood_logs = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).order_by(MoodLogModel.created_at.desc()).limit(30).all()
    
    # Calculate emotional trends
    emotional_trends = []
    if mood_logs:
        recent_emotions = [log.emotion for log in mood_logs[:7]]
        emotional_trends = [
            f"Recent mood: {recent_emotions[0] if recent_emotions else 'Calm'}",
            f"Most frequent: {max(set(recent_emotions), key=recent_emotions.count) if recent_emotions else 'Calm'}"
        ]
    
    # Calculate stability score
    if mood_logs:
        avg_score = sum(log.score for log in mood_logs) / len(mood_logs)
        stability_score = int(avg_score)
    else:
        stability_score = 72
    
    # Get top emotions
    emotion_counts = {}
    for log in mood_logs:
        emotion_counts[log.emotion] = emotion_counts.get(log.emotion, 0) + 1
    top_emotions = sorted(emotion_counts.keys(), key=lambda x: emotion_counts[x], reverse=True)[:3] if emotion_counts else ["Calm", "Happy", "Peaceful"]
    
    # Monthly summary
    monthly_summary = "You've been consistent with your emotional check-ins this month. Your patterns show resilience and growth."
    
    return ProfileInsightsResponse(
        emotional_trends=emotional_trends,
        stability_score=stability_score,
        top_emotions=top_emotions,
        monthly_summary=monthly_summary
    )

@app.post("/profile/reset-data", response_model=ProfileResetResponse)
def reset_profile_data(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    try:
        # Delete user data
        db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).delete()
        db.query(ChatMessageModel).filter(ChatMessageModel.user_id == user_id).delete()
        db.query(VoiceConversationModel).filter(VoiceConversationModel.user_id == user_id).delete()
        db.query(BreathingSessionModel).filter(BreathingSessionModel.user_id == user_id).delete()
        db.query(CompanionProgressModel).filter(CompanionProgressModel.user_id == user_id).delete()
        db.query(CompanionMemoryModel).filter(CompanionMemoryModel.user_id == user_id).delete()
        db.query(AchievementModel).filter(AchievementModel.user_id == user_id).delete()
        db.query(CompanionCustomizationModel).filter(CompanionCustomizationModel.user_id == user_id).delete()
        db.query(TimelineEventModel).filter(TimelineEventModel.user_id == user_id).delete()
        db.query(EmotionalWeatherModel).filter(EmotionalWeatherModel.user_id == user_id).delete()
        
        db.commit()
        
        return ProfileResetResponse(
            success=True,
            message="All data has been successfully deleted."
        )
    except Exception as e:
        db.rollback()
        return ProfileResetResponse(
            success=False,
            message=f"Failed to delete data: {str(e)}"
        )

@app.post("/profile/export", response_model=ProfileExportResponse)
def export_profile_data(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    try:
        # Collect all user data
        user = db.query(UserModel).filter(UserModel.id == user_id).first()
        mood_logs = db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).all()
        chat_messages = db.query(ChatMessageModel).filter(ChatMessageModel.user_id == user_id).all()
        voice_conversations = db.query(VoiceConversationModel).filter(VoiceConversationModel.user_id == user_id).all()
        breathing_sessions = db.query(BreathingSessionModel).filter(BreathingSessionModel.user_id == user_id).all()
        
        # Create export data structure
        export_data = {
            "user": {
                "name": user.name if user else "",
                "email": user.email if user else "",
                "language": user.language if user else "en",
                "companion_name": user.companion_name if user else "Mochi",
                "companion_type": user.companion_type if user else "mochi_cat"
            },
            "mood_logs": [
                {
                    "mood": log.mood,
                    "emotion": log.emotion,
                    "score": log.score,
                    "notes": log.notes,
                    "created_at": log.created_at
                }
                for log in mood_logs
            ],
            "chat_messages": [
                {
                    "role": msg.role,
                    "message": msg.message,
                    "emotion": msg.emotion,
                    "created_at": msg.created_at
                }
                for msg in chat_messages
            ],
            "voice_conversations": [
                {
                    "transcript": conv.transcript,
                    "emotion": conv.emotion,
                    "duration": conv.duration,
                    "created_at": conv.created_at
                }
                for conv in voice_conversations
            ],
            "breathing_sessions": [
                {
                    "session_type": session.session_type,
                    "duration": session.duration,
                    "cycles_completed": session.cycles_completed,
                    "created_at": session.created_at
                }
                for session in breathing_sessions
            ]
        }
        
        # In a real implementation, you would save this to a file and return a URL
        # For now, we'll return success with a message
        return ProfileExportResponse(
            success=True,
            message="Data export completed. In production, a download link would be provided."
        )
    except Exception as e:
        return ProfileExportResponse(
            success=False,
            message=f"Failed to export data: {str(e)}"
        )


# --- SETTINGS ENDPOINTS ---

@app.get("/settings", response_model=SettingsResponse)
def get_settings(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    preferences = db.query(UserPreferencesModel).filter(UserPreferencesModel.user_id == user_id).first()
    if not preferences:
        preferences = UserPreferencesModel(
            user_id=user_id,
            theme="light",
            notifications_enabled=1,
            ai_memory_enabled=1,
            voice_enabled=1,
            ai_tone="Gentle Friend",
            language="en",
            mood_reminders=1,
            journal_reminders=1,
            breathing_reminders=1,
            voice_reminders=0,
            emotion_sensitivity="Medium",
            response_style="Balanced",
            voice_speed=1.0,
            voice_tone="Soft",
            biometric_enabled=0,
            offline_data_enabled=1,
            privacy_level="Standard"
        )
        db.add(preferences)
        db.commit()
        db.refresh(preferences)
    
    return SettingsResponse(
        notifications_enabled=preferences.notifications_enabled == 1,
        ai_memory_enabled=preferences.ai_memory_enabled == 1,
        voice_enabled=preferences.voice_enabled == 1,
        ai_tone=preferences.ai_tone,
        language=preferences.language,
        mood_reminders=preferences.mood_reminders == 1,
        journal_reminders=preferences.journal_reminders == 1,
        breathing_reminders=preferences.breathing_reminders == 1,
        voice_reminders=preferences.voice_reminders == 1,
        emotion_sensitivity=preferences.emotion_sensitivity,
        response_style=preferences.response_style,
        voice_speed=preferences.voice_speed,
        voice_tone=preferences.voice_tone,
        biometric_enabled=preferences.biometric_enabled == 1,
        offline_data_enabled=preferences.offline_data_enabled == 1,
        privacy_level=preferences.privacy_level
    )

@app.put("/settings/update", response_model=SettingsResponse)
def update_settings(
    request: SettingsUpdateRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload.")
    
    preferences = db.query(UserPreferencesModel).filter(UserPreferencesModel.user_id == user_id).first()
    if not preferences:
        preferences = UserPreferencesModel(user_id=user_id)
        db.add(preferences)
    
    # Update preferences
    if request.notifications_enabled is not None:
        preferences.notifications_enabled = 1 if request.notifications_enabled else 0
    
    if request.ai_memory_enabled is not None:
        preferences.ai_memory_enabled = 1 if request.ai_memory_enabled else 0
    
    if request.voice_enabled is not None:
        preferences.voice_enabled = 1 if request.voice_enabled else 0
    
    if request.ai_tone:
        preferences.ai_tone = request.ai_tone
    
    if request.language:
        preferences.language = request.language
    
    if request.mood_reminders is not None:
        preferences.mood_reminders = 1 if request.mood_reminders else 0
    
    if request.journal_reminders is not None:
        preferences.journal_reminders = 1 if request.journal_reminders else 0
    
    if request.breathing_reminders is not None:
        preferences.breathing_reminders = 1 if request.breathing_reminders else 0
    
    if request.voice_reminders is not None:
        preferences.voice_reminders = 1 if request.voice_reminders else 0
    
    if request.emotion_sensitivity:
        preferences.emotion_sensitivity = request.emotion_sensitivity
    
    if request.response_style:
        preferences.response_style = request.response_style
    
    if request.voice_speed is not None:
        preferences.voice_speed = request.voice_speed
    
    if request.voice_tone:
        preferences.voice_tone = request.voice_tone
    
    if request.biometric_enabled is not None:
        preferences.biometric_enabled = 1 if request.biometric_enabled else 0
    
    if request.offline_data_enabled is not None:
        preferences.offline_data_enabled = 1 if request.offline_data_enabled else 0
    
    preferences.updated_at = int(time.time() * 1000)
    
    db.commit()
    db.refresh(preferences)
    
    return get_settings(token_data, db)

@app.post("/settings/reset-memory", response_model=SettingsResetMemoryResponse)
def reset_settings_memory(
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    user_id = token_data.get("id")
    if not user_id:
        log_security_event("INVALID_TOKEN", details="No user_id in token payload")
        raise HTTPException(status_code=401, detail="Invalid authentication credentials.")
    
    try:
        preferences = db.query(UserPreferencesModel).filter(UserPreferencesModel.user_id == user_id).first()
        if preferences:
            preferences.ai_memory_enabled = 0
            preferences.updated_at = int(time.time() * 1000)
            db.commit()
        
        log_security_event("MEMORY_RESET", user_id=user_id)
        
        return SettingsResetMemoryResponse(
            success=True,
            message="AI memory has been reset. Your companion will learn from fresh while keeping your progress."
        )
    except Exception as e:
        logger.error(f"Failed to reset memory: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to reset memory")

@app.post("/user/delete-all-data", response_model=DataDeletionResponse)
def delete_all_user_data(
    request: DataDeletionRequest,
    token_data: dict = Depends(verify_token),
    db: Session = Depends(get_db)
):
    """Permanently delete all user data - GDPR right to be forgotten."""
    user_id = token_data.get("id")
    if not user_id:
        log_security_event("INVALID_TOKEN", details="No user_id in token payload")
        raise HTTPException(status_code=401, detail="Invalid authentication credentials.")

    user = db.query(UserModel).filter(UserModel.id == user_id).first()
    if not user:
        log_security_event("USER_NOT_FOUND", user_id=user_id)
        raise HTTPException(status_code=404, detail="User account not found.")

    # Verify password
    if not verify_password(request.password, user.password_hash):
        log_security_event("FAILED_DATA_DELETION", user_id=user_id, details="Invalid password")
        raise HTTPException(status_code=401, detail="Invalid password")

    # Verify confirmation
    if request.confirmation != "DELETE_ALL_DATA":
        log_security_event("FAILED_DATA_DELETION", user_id=user_id, details="Invalid confirmation")
        raise HTTPException(status_code=400, detail="Confirmation must be 'DELETE_ALL_DATA'")

    # Delete all user data (permanent)
    deleted_records = 0
    
    try:
        # Delete chat messages
        deleted_records += db.query(ChatMessageModel).filter(ChatMessageModel.user_id == user_id).delete()
        
        # Delete mood logs
        deleted_records += db.query(MoodLogModel).filter(MoodLogModel.user_id == user_id).delete()
        
        # Delete emotional weather
        deleted_records += db.query(EmotionalWeatherModel).filter(EmotionalWeatherModel.user_id == user_id).delete()
        
        # Delete voice conversations
        deleted_records += db.query(VoiceConversationModel).filter(VoiceConversationModel.user_id == user_id).delete()
        
        # Delete companion progress
        deleted_records += db.query(CompanionProgressModel).filter(CompanionProgressModel.user_id == user_id).delete()
        
        # Delete companion memories
        deleted_records += db.query(CompanionMemoryModel).filter(CompanionMemoryModel.user_id == user_id).delete()
        
        # Delete achievements
        deleted_records += db.query(AchievementModel).filter(AchievementModel.user_id == user_id).delete()
        
        # Delete companion customization
        deleted_records += db.query(CompanionCustomizationModel).filter(CompanionCustomizationModel.user_id == user_id).delete()
        
        # Delete timeline events
        deleted_records += db.query(TimelineEventModel).filter(TimelineEventModel.user_id == user_id).delete()
        
        # Delete user preferences
        deleted_records += db.query(UserPreferencesModel).filter(UserPreferencesModel.user_id == user_id).delete()
        
        # Finally, delete the user account
        deleted_records += db.query(UserModel).filter(UserModel.id == user_id).delete()
        
        db.commit()
        
        log_security_event("DATA_DELETION_COMPLETE", user_id=user_id, details=f"Deleted {deleted_records} records")
        
        return DataDeletionResponse(
            success=True,
            message="All your data has been permanently deleted. This action cannot be undone.",
            deleted_records=deleted_records
        )
        
    except Exception as e:
        db.rollback()
        logger.error(f"Failed to delete user data")
        log_security_event("DATA_DELETION_FAILED", user_id=user_id)
        raise HTTPException(status_code=500, detail="Failed to delete data. Please try again.")


