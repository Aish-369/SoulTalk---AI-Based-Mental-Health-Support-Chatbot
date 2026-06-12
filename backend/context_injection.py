import logging
from typing import Dict, List
from datetime import datetime
from .emotion_engine import Emotion, PersonalityMode
from .memory_system import MemorySystem, ResponseEngine

# Configure logging
logger = logging.getLogger(__name__)

class ContextInjector:
    """Injects rich context into Gemini API requests for personalized responses."""
    
    def __init__(self, memory_system: MemorySystem, response_engine: ResponseEngine):
        self.memory_system = memory_system
        self.response_engine = response_engine
    
    def build_gemini_context(self, user_id: int, user_message: str, 
                            detected_emotion: Emotion) -> Dict:
        """
        Build comprehensive context for Gemini API request.
        
        Args:
            user_id: User identifier
            user_message: Current user message
            detected_emotion: Emotion detected from user message
        
        Returns:
            Dictionary containing all context for Gemini
        """
        # Get personalization context from memory system
        personalization_context = self.memory_system.get_personalization_context(user_id)
        
        # Get recent conversation context
        recent_conversation = self.memory_system.get_recent_conversation_context(user_id, limit=5)
        
        # Get emotional history
        emotional_history = self.memory_system.get_emotional_history(user_id, days=7)
        
        # Get important memories
        important_memories = self.memory_system.get_important_memories(user_id, limit=3)
        
        # Build comprehensive context
        context = {
            'user_message': user_message,
            'detected_emotion': detected_emotion.value,
            'user_preferences': personalization_context.get('user_preferences', {}),
            'companion_personality': personalization_context.get('companion_personality', {}),
            'recent_conversation': self._format_conversation_for_gemini(recent_conversation),
            'emotional_history': self._format_emotional_history(emotional_history),
            'important_memories': self._format_memories_for_gemini(important_memories),
            'dominant_emotion': personalization_context.get('dominant_emotion', 'neutral'),
            'response_length_hint': self._determine_response_length(user_message, detected_emotion),
            'timestamp': datetime.now().isoformat()
        }
        
        logger.info(f"Context built for user {user_id} with emotion {detected_emotion.value}")
        return context
    
    def _format_conversation_for_gemini(self, conversation: List[Dict]) -> str:
        """Format conversation history for Gemini API."""
        if not conversation:
            return "No recent conversation history."
        
        formatted = []
        for msg in conversation[-5:]:  # Last 5 messages
            role = msg['role']
            # Truncate long messages for context
            message = msg['message'][:100] + "..." if len(msg['message']) > 100 else msg['message']
            formatted.append(f"{role}: {message}")
        
        return "\n".join(formatted)
    
    def _format_emotional_history(self, emotional_history: Dict[str, int]) -> str:
        """Format emotional history for Gemini API."""
        if not emotional_history:
            return "No emotional history available."
        
        sorted_emotions = sorted(emotional_history.items(), key=lambda x: x[1], reverse=True)
        formatted = []
        for emotion, count in sorted_emotions:
            formatted.append(f"{emotion}: {count} times")
        
        return ", ".join(formatted)
    
    def _format_memories_for_gemini(self, memories: List[Dict]) -> str:
        """Format important memories for Gemini API."""
        if not memories:
            return "No important memories."
        
        formatted = []
        for memory in memories[:3]:  # Top 3 memories
            content = memory['content'][:80] + "..." if len(memory['content']) > 80 else memory['content']
            formatted.append(f"- {content} (emotion: {memory['emotion']})")
        
        return "\n".join(formatted)
    
    def _determine_response_length(self, user_message: str, detected_emotion: Emotion) -> str:
        """Determine appropriate response length hint for Gemini."""
        message_length = len(user_message.strip())
        
        # Simple messages get short responses
        if message_length < 10 or user_message.lower() in ['ok', 'yes', 'no', 'thanks', 'thank you']:
            return 'very_short'
        
        # Emotional messages get medium responses
        if detected_emotion in [Emotion.SAD, Emotion.ANXIOUS, Emotion.LONELY, Emotion.STRESSED]:
            return 'medium'
        
        # Deep conversations get detailed responses
        if message_length > 50 or '?' in user_message:
            return 'detailed'
        
        return 'medium'
    
    def construct_gemini_system_instruction(self, context: Dict) -> str:
        """
        Construct the system instruction for Gemini API with all context.
        
        Args:
            context: Comprehensive context dictionary
        
        Returns:
            System instruction string for Gemini
        """
        user_prefs = context.get('user_preferences', {})
        companion = context.get('companion_personality', {})
        detected_emotion = context.get('detected_emotion', 'neutral')
        recent_conv = context.get('recent_conversation', '')
        emotional_hist = context.get('emotional_history', '')
        important_mems = context.get('important_memories', '')
        response_length = context.get('response_length_hint', 'medium')
        
        system_instruction = f"""
You are {companion.get('name', 'a warm emotional companion')}, a {companion.get('type', 'supportive companion')} with a {companion.get('personality', 'gentle')} personality.

CURRENT EMOTIONAL STATE:
The user is currently expressing: {detected_emotion}
Recent emotional patterns: {emotional_hist}

CONVERSATION CONTEXT:
Recent conversation:
{recent_conv}

IMPORTANT MEMORIES:
{important_mems}

RESPONSE GUIDELINES:
Follow this 5-step structure for every response:
1. Acknowledge the emotion the user is expressing
2. Reflect back what you hear them feeling
3. Ask a gentle, open-ended question to explore further
4. Provide warm emotional support
5. Give a soft, optional suggestion only if helpful

STYLE REQUIREMENTS:
- Response length: {response_length}
- Use warm, gentle language
- Be non-judgmental and supportive
- Add appropriate emojis sparingly for warmth
- Never sound robotic or clinical
- Don't rush to solutions
- Vary response length naturally based on conversation depth
- Make the user feel heard, understood, and not alone

USER PREFERENCES:
- Language: {user_prefs.get('language', 'English')}
- Response style: {user_prefs.get('response_style', 'supportive')}
- Emotion sensitivity: {user_prefs.get('emotion_sensitivity', 0.7)}

EXAMPLES OF GOOD RESPONSES:
- User: "ok" → "Got it 💙"
- User: "thank you" → "You're always welcome. 🌿"
- User: "I feel stressed" → "That sounds exhausting. 💙 What's been weighing on your mind the most?"
- User: "I'm sad today" → "I hear that heaviness in your words. 💙 It's okay to feel sad sometimes. What's making your heart feel heavy today?"

Remember: You are a personal emotional companion, not a generic chatbot. Make the user feel truly understood and supported.
"""
        return system_instruction
    
    def construct_gemini_contents_payload(self, user_message: str, 
                                         recent_conversation: List[Dict]) -> List[Dict]:
        """
        Construct the contents payload for Gemini API.
        
        Args:
            user_message: Current user message
            recent_conversation: Recent conversation history
        
        Returns:
            List of content dictionaries for Gemini API
        """
        contents = []
        
        # Add recent conversation context
        for msg in recent_conversation[-10:]:  # Last 10 messages for context
            role = "user" if msg['role'] == "user" else "model"
            contents.append({
                "role": role,
                "parts": [{"text": msg['message']}]
            })
        
        # Add current user message
        contents.append({
            "role": "user",
            "parts": [{"text": user_message}]
        })
        
        return contents

# Global context injector instance
context_injector = None

def get_context_injector(memory_system: MemorySystem, 
                        response_engine: ResponseEngine) -> ContextInjector:
    """Get context injector instance."""
    return ContextInjector(memory_system, response_engine)
