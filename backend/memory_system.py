import logging
from typing import Dict, List, Optional
from datetime import datetime
from sqlalchemy.orm import Session
from .models import (
    UserModel,
    CompanionMemoryModel,
    ChatMessageModel,
    MoodLogModel,
    UserPreferencesModel
)
from .emotion_engine import Emotion, PersonalityMode

# Configure logging
logger = logging.getLogger(__name__)

class MemorySystem:
    """Advanced memory system for emotional companion AI."""
    
    def __init__(self, db: Session):
        self.db = db
    
    def store_conversation_memory(self, user_id: int, message: str, emotion: str, 
                                   is_important: bool = False, context: str = None):
        """Store important conversation memory."""
        try:
            memory = CompanionMemoryModel(
                user_id=user_id,
                memory_type="conversation",
                content=message,
                emotion=emotion,
                importance_score=1.0 if is_important else 0.5,
                context=context,
                created_at=int(datetime.now().timestamp() * 1000)
            )
            self.db.add(memory)
            self.db.commit()
            logger.info(f"Conversation memory stored for user {user_id}")
        except Exception as e:
            logger.error(f"Failed to store conversation memory: {e}")
            self.db.rollback()
    
    def get_user_preferences(self, user_id: int) -> Dict:
        """Retrieve user preferences for personalization."""
        try:
            user = self.db.query(UserModel).filter(UserModel.id == user_id).first()
            if not user:
                return {}
            
            preferences = self.db.query(UserPreferencesModel).filter(
                UserPreferencesModel.user_id == user_id
            ).first()
            
            return {
                'companion_name': user.companion_name,
                'companion_type': user.companion_type,
                'personality_type': user.personality_type,
                'language': user.language,
                'emotion_sensitivity': preferences.emotion_sensitivity if preferences else 0.7,
                'response_style': preferences.response_style if preferences else 'supportive',
                'ai_memory_enabled': preferences.ai_memory_enabled if preferences else True
            }
        except Exception as e:
            logger.error(f"Failed to retrieve user preferences: {e}")
            return {}
    
    def get_recent_conversation_context(self, user_id: int, limit: int = 10) -> List[Dict]:
        """Get recent conversation context for context injection."""
        try:
            messages = self.db.query(ChatMessageModel).filter(
                ChatMessageModel.user_id == user_id
            ).order_by(ChatMessageModel.id.desc()).limit(limit * 2).all()
            
            # Convert to list of dicts, reversed for chronological order
            context = []
            for msg in reversed(messages):
                context.append({
                    'role': msg.role,
                    'message': msg.message,
                    'emotion': msg.emotion,
                    'timestamp': msg.timestamp
                })
            
            return context[-limit:]  # Return last 'limit' messages
        except Exception as e:
            logger.error(f"Failed to retrieve conversation context: {e}")
            return []
    
    def get_emotional_history(self, user_id: int, days: int = 7) -> Dict[str, int]:
        """Get emotional history for pattern recognition."""
        try:
            from datetime import timedelta
            cutoff_time = datetime.now() - timedelta(days=days)
            
            mood_logs = self.db.query(MoodLogModel).filter(
                MoodLogModel.user_id == user_id,
                MoodLogModel.timestamp >= int(cutoff_time.timestamp() * 1000)
            ).all()
            
            emotion_counts = {}
            for log in mood_logs:
                emotion = log.emotion or "neutral"
                emotion_counts[emotion] = emotion_counts.get(emotion, 0) + 1
            
            return emotion_counts
        except Exception as e:
            logger.error(f"Failed to retrieve emotional history: {e}")
            return {}
    
    def get_important_memories(self, user_id: int, limit: int = 5) -> List[Dict]:
        """Retrieve important memories for context."""
        try:
            memories = self.db.query(CompanionMemoryModel).filter(
                CompanionMemoryModel.user_id == user_id,
                CompanionMemoryModel.importance_score >= 0.7
            ).order_by(CompanionMemoryModel.created_at.desc()).limit(limit).all()
            
            return [
                {
                    'content': memory.content,
                    'emotion': memory.emotion,
                    'context': memory.context,
                    'created_at': memory.created_at
                }
                for memory in memories
            ]
        except Exception as e:
            logger.error(f"Failed to retrieve important memories: {e}")
            return []
    
    def update_interaction_pattern(self, user_id: int, emotion: str, 
                                    response_quality: float):
        """Learn from user interactions to improve future responses."""
        try:
            # Store interaction pattern for learning
            memory = CompanionMemoryModel(
                user_id=user_id,
                memory_type="interaction_pattern",
                content=f"emotion:{emotion},quality:{response_quality}",
                emotion=emotion,
                importance_score=0.3,
                created_at=int(datetime.now().timestamp() * 1000)
            )
            self.db.add(memory)
            self.db.commit()
            logger.info(f"Interaction pattern updated for user {user_id}")
        except Exception as e:
            logger.error(f"Failed to update interaction pattern: {e}")
            self.db.rollback()
    
    def get_personalization_context(self, user_id: int) -> Dict:
        """Get comprehensive personalization context for Gemini."""
        try:
            preferences = self.get_user_preferences(user_id)
            recent_context = self.get_recent_conversation_context(user_id, limit=5)
            emotional_history = self.get_emotional_history(user_id, days=7)
            important_memories = self.get_important_memories(user_id, limit=3)
            
            # Determine dominant emotion from history
            dominant_emotion = max(emotional_history.items(), 
                                 key=lambda x: x[1])[0] if emotional_history else "neutral"
            
            return {
                'user_preferences': preferences,
                'recent_conversation': recent_context,
                'emotional_history': emotional_history,
                'dominant_emotion': dominant_emotion,
                'important_memories': important_memories,
                'companion_personality': {
                    'name': preferences.get('companion_name', 'Friend'),
                    'type': preferences.get('companion_type', 'cat'),
                    'personality': preferences.get('personality_type', 'gentle')
                }
            }
        except Exception as e:
            logger.error(f"Failed to get personalization context: {e}")
            return {}
    
    def cleanup_old_memories(self, user_id: int, days: int = 30):
        """Clean up old memories to prevent database bloat."""
        try:
            from datetime import timedelta
            cutoff_time = datetime.now() - timedelta(days=days)
            
            # Delete old low-importance memories
            deleted = self.db.query(CompanionMemoryModel).filter(
                CompanionMemoryModel.user_id == user_id,
                CompanionMemoryModel.importance_score < 0.5,
                CompanionMemoryModel.created_at < int(cutoff_time.timestamp() * 1000)
            ).delete()
            
            self.db.commit()
            logger.info(f"Cleaned up {deleted} old memories for user {user_id}")
        except Exception as e:
            logger.error(f"Failed to cleanup old memories: {e}")
            self.db.rollback()

class ResponseEngine:
    """Response engine for generating emotionally intelligent responses."""
    
    def __init__(self, personality_mode: PersonalityMode = PersonalityMode.GENTLE_FRIEND):
        self.personality_mode = personality_mode
    
    def generate_response_structure(self, user_message: str, detected_emotion: Emotion,
                                   context: Dict) -> str:
        """
        Generate response following the 5-step structure:
        1. Acknowledge emotion
        2. Reflect feeling
        3. Ask gentle question
        4. Provide emotional support
        5. Give optional soft suggestion
        """
        # This will be used to construct the system instruction for Gemini
        # The actual response generation will be done by Gemini with this structure
        
        personality_traits = self._get_personality_traits()
        
        system_instruction = f"""
You are {personality_traits['name']}, a warm and empathetic emotional companion.
You are a {personality_traits['type']} with a {personality_traits['personality']} personality.

The user is currently feeling: {detected_emotion.value}

Recent emotional context: {context.get('dominant_emotion', 'neutral')}

RESPONSE STRUCTURE (follow this exactly):
1. Acknowledge the emotion the user is expressing
2. Reflect back what you hear them feeling
3. Ask a gentle, open-ended question to explore further
4. Provide warm emotional support
5. Give a soft, optional suggestion only if helpful

STYLE GUIDELINES:
- Keep responses brief and conversational (1-3 sentences typically)
- Use warm, gentle language
- Be non-judgmental and supportive
- Add appropriate emojis sparingly for warmth
- Never sound robotic or clinical
- Don't rush to solutions
- Vary response length naturally based on conversation depth

PERSONALITY MODE: {self.personality_mode.value}

Examples of good responses:
- User: "ok" → "Got it 💙"
- User: "thank you" → "You're always welcome. 🌿"
- User: "I feel stressed" → "That sounds exhausting. 💙 What's been weighing on your mind the most?"
"""
        return system_instruction
    
    def _get_personality_traits(self) -> Dict:
        """Get personality traits based on current mode."""
        traits = {
            PersonalityMode.GENTLE_FRIEND: {
                'name': 'a gentle friend',
                'type': 'supportive companion',
                'personality': 'warm and caring'
            },
            PersonalityMode.CALM_LISTENER: {
                'name': 'a calm listener',
                'type': 'peaceful presence',
                'personality': 'serene and patient'
            },
            PersonalityMode.MOTIVATIONAL_COACH: {
                'name': 'a motivational coach',
                'type': 'encouraging guide',
                'personality': 'energetic and supportive'
            }
        }
        return traits.get(self.personality_mode, traits[PersonalityMode.GENTLE_FRIEND])
    
    def determine_response_length(self, user_message: str, detected_emotion: Emotion) -> str:
        """Determine appropriate response length based on context."""
        message_length = len(user_message.strip())
        
        # Simple messages get short responses
        if message_length < 10 or user_message.lower() in ['ok', 'yes', 'no', 'thanks', 'thank you']:
            return 'short'
        
        # Emotional messages get medium responses
        if detected_emotion in [Emotion.SAD, Emotion.ANXIOUS, Emotion.LONELY, Emotion.STRESSED]:
            return 'medium'
        
        # Deep conversations get detailed responses
        if message_length > 50 or '?' in user_message:
            return 'detailed'
        
        return 'medium'

# Global instances
memory_system = None  # Will be initialized with database session
response_engine = ResponseEngine()

def get_memory_system(db: Session) -> MemorySystem:
    """Get memory system instance with database session."""
    return MemorySystem(db)
