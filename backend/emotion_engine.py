import re
import logging
from typing import Dict, Tuple, List
from enum import Enum

# Configure logging
logger = logging.getLogger(__name__)

class Emotion(Enum):
    HAPPY = "happy"
    SAD = "sad"
    ANGRY = "angry"
    ANXIOUS = "anxious"
    LONELY = "lonely"
    STRESSED = "stressed"
    NEUTRAL = "neutral"
    EXCITED = "excited"

class PersonalityMode(Enum):
    GENTLE_FRIEND = "gentle_friend"
    CALM_LISTENER = "calm_listener"
    MOTIVATIONAL_COACH = "motivational_coach"

class EmotionDetectionEngine:
    """Advanced emotion detection with context awareness and confidence scoring."""
    
    def __init__(self):
        # Emotion keywords with weights
        self.emotion_keywords = {
            Emotion.HAPPY: {
                'high': ['happy', 'joy', 'glad', 'cheerful', 'delighted', 'thrilled', 'excited', 'wonderful', 'amazing', 'great', 'fantastic', 'love', 'blessed', 'grateful'],
                'medium': ['smile', 'laugh', 'good', 'nice', 'positive', 'bright', 'warm', 'peaceful', 'content'],
                'low': ['okay', 'fine', 'alright']
            },
            Emotion.SAD: {
                'high': ['sad', 'depressed', 'heartbroken', 'devastated', 'grief', 'mourning', 'hopeless', 'despair', 'crying', 'tears'],
                'medium': ['down', 'blue', 'unhappy', 'upset', 'hurt', 'pain', 'sorrow', 'melancholy', 'lonely'],
                'low': ['not great', 'not good', 'off']
            },
            Emotion.ANGRY: {
                'high': ['furious', 'rage', 'hate', 'angry', 'mad', 'outraged', 'livid', 'irate', 'violence', 'destroy'],
                'medium': ['annoyed', 'frustrated', 'irritated', 'upset', 'bothered', 'aggravated', 'tense'],
                'low': ['not happy', 'disappointed']
            },
            Emotion.ANXIOUS: {
                'high': ['panic', 'terrified', 'scared', 'fear', 'anxiety', 'worried', 'nervous', 'dread', 'phobia', 'overwhelmed'],
                'medium': ['anxious', 'concerned', 'uneasy', 'restless', 'apprehensive', 'stressed', 'tense'],
                'low': ['unsure', 'uncertain']
            },
            Emotion.LONELY: {
                'high': ['isolated', 'alone', 'lonely', 'abandoned', 'rejected', 'nobody', 'no one', 'by myself'],
                'medium': ['miss', 'wish someone was here', 'need company', 'feel disconnected'],
                'low': ['quiet', 'solitude']
            },
            Emotion.STRESSED: {
                'high': ['overwhelmed', 'exhausted', 'burnout', 'pressure', 'deadline', 'workload', 'stress', 'panic'],
                'medium': ['busy', 'tired', 'stressed', 'pressure', 'hectic', 'rushed', 'overloaded'],
                'low': ['lot going on', 'much to do']
            },
            Emotion.EXCITED: {
                'high': ['excited', 'thrilled', 'pumped', 'hyped', 'amazing', 'awesome', 'incredible', 'cant wait'],
                'medium': ['looking forward', 'anticipating', 'eager', 'enthusiastic'],
                'low': ['interested', 'curious']
            }
        }
        
        # Context patterns for better detection
        self.context_patterns = {
            'intensifiers': ['very', 'really', 'extremely', 'absolutely', 'completely', 'totally', 'so', 'such'],
            'negators': ['not', 'never', "don't", "doesn't", "didn't", "won't", "can't"],
            'questions': ['?', 'what', 'how', 'why', 'when', 'where', 'who']
        }
    
    def detect_emotion(self, text: str, context: Dict = None) -> Tuple[Emotion, float]:
        """
        Detect emotion from text with confidence score.
        
        Args:
            text: User message text
            context: Additional context (mood history, recent conversations)
        
        Returns:
            Tuple of (Emotion, confidence_score)
        """
        if not text or not text.strip():
            return Emotion.NEUTRAL, 0.0
        
        text_lower = text.lower()
        emotion_scores = {}
        
        # Score each emotion based on keyword matches
        for emotion, keywords in self.emotion_keywords.items():
            score = 0.0
            total_weight = 0
            
            for weight_level, words in keywords.items():
                weight = {'high': 3.0, 'medium': 2.0, 'low': 1.0}[weight_level]
                for word in words:
                    if word in text_lower:
                        score += weight
                        total_weight += weight
            
            if total_weight > 0:
                emotion_scores[emotion] = score / total_weight
        
        # Apply context-based adjustments
        if context:
            emotion_scores = self._apply_context_adjustments(emotion_scores, context)
        
        # Determine dominant emotion
        if not emotion_scores:
            return Emotion.NEUTRAL, 0.0
        
        dominant_emotion = max(emotion_scores, key=emotion_scores.get)
        confidence = emotion_scores[dominant_emotion]
        
        # Normalize confidence
        confidence = min(confidence, 1.0)
        
        logger.info(f"Emotion detected: {dominant_emotion.value} with confidence {confidence:.2f}")
        return dominant_emotion, confidence
    
    def _apply_context_adjustments(self, emotion_scores: Dict[Emotion, float], context: Dict) -> Dict[Emotion, float]:
        """Apply context-based adjustments to emotion scores."""
        # Adjust based on recent mood history
        recent_moods = context.get('recent_moods', [])
        if recent_moods:
            # Give slight boost to emotions consistent with recent patterns
            for mood in recent_moods[-3:]:  # Last 3 moods
                try:
                    mood_emotion = Emotion(mood.lower())
                    if mood_emotion in emotion_scores:
                        emotion_scores[mood_emotion] *= 1.1
                except ValueError:
                    pass
        
        # Adjust based on time of day (if available)
        time_of_day = context.get('time_of_day')
        if time_of_day:
            # Morning might have more excited/energetic emotions
            if time_of_day == 'morning':
                if Emotion.EXCITED in emotion_scores:
                    emotion_scores[Emotion.EXCITED] *= 1.1
            # Evening might have more reflective/sad emotions
            elif time_of_day == 'evening':
                if Emotion.SAD in emotion_scores:
                    emotion_scores[Emotion.SAD] *= 1.1
        
        return emotion_scores
    
    def detect_emotions_multi(self, text: str) -> List[Tuple[Emotion, float]]:
        """
        Detect multiple emotions in text (for complex emotional states).
        
        Returns:
            List of (Emotion, confidence) tuples, sorted by confidence
        """
        if not text or not text.strip():
            return [(Emotion.NEUTRAL, 0.0)]
        
        text_lower = text.lower()
        emotion_scores = {}
        
        for emotion, keywords in self.emotion_keywords.items():
            score = 0.0
            total_weight = 0
            
            for weight_level, words in keywords.items():
                weight = {'high': 3.0, 'medium': 2.0, 'low': 1.0}[weight_level]
                for word in words:
                    if word in text_lower:
                        score += weight
                        total_weight += weight
            
            if total_weight > 0:
                emotion_scores[emotion] = score / total_weight
        
        # Sort by confidence and return top emotions
        sorted_emotions = sorted(emotion_scores.items(), key=lambda x: x[1], reverse=True)
        
        # Normalize confidence scores
        max_score = max([score for _, score in sorted_emotions]) if sorted_emotions else 1.0
        normalized_emotions = [(emotion, score/max_score) for emotion, score in sorted_emotions]
        
        return normalized_emotions[:3]  # Return top 3 emotions

# Global emotion detection engine instance
emotion_engine = EmotionDetectionEngine()

def detect_emotion(text: str, context: Dict = None) -> Tuple[str, float]:
    """
    Convenience function for emotion detection.
    
    Returns:
        Tuple of (emotion_string, confidence_score)
    """
    emotion, confidence = emotion_engine.detect_emotion(text, context)
    return emotion.value, confidence
