import re
import logging
from typing import Dict, List, Tuple, Optional
from enum import Enum

# Configure logging
logger = logging.getLogger(__name__)

class CrisisLevel(Enum):
    NONE = "none"
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    SEVERE = "severe"

class SafetyLayer:
    """Enhanced safety layer for crisis detection and intervention."""
    
    def __init__(self):
        # Crisis keywords with severity levels
        self.crisis_keywords = {
            CrisisLevel.SEVERE: [
                'suicide', 'kill myself', 'end my life', 'want to die', 'no reason to live',
                'better off dead', 'end it all', 'take my own life', 'commit suicide',
                'hurt myself', 'self harm', 'cut myself', 'overdose'
            ],
            CrisisLevel.HIGH: [
                'hopeless', 'worthless', 'no point', 'giving up', 'can\'t go on',
                'want to disappear', 'everyone better off without me', 'burden',
                'no hope left', 'darkness', 'abyss', 'end the pain'
            ],
            CrisisLevel.MEDIUM: [
                'depressed', 'depression', 'anxiety attack', 'panic attack',
                'overwhelmed', 'can\'t cope', 'falling apart', 'breaking down',
                'mental breakdown', 'losing control', 'scared of myself'
            ],
            CrisisLevel.LOW: [
                'sad', 'down', 'unhappy', 'stressed', 'anxious', 'worried',
                'lonely', 'isolated', 'empty', 'numb', 'tired of everything'
            ]
        }
        
        # Professional help resources — India-specific.
        # Centralized here deliberately: every other file (main.py's
        # voice endpoints included) should read from this dict rather
        # than hardcoding a number, so there's exactly one place to
        # update if a helpline changes.
        # Sources verified directly (Aug 2026): telemanas.mohfw.gov.in,
        # icallhelpline.org, vandrevalafoundation.com
        self.help_resources = {
            'crisis_hotline': 'Tele MANAS — 14416 or 1-800-891-4416 (Govt. of India, 24x7, 20+ languages)',
            'regional': 'iCALL (TISS, Maharashtra) — 9152987821, Mon–Sat 10am–8pm',
            'alternate_24x7': 'Vandrevala Foundation — 1860-266-2345 / 9999 666 555 (24x7)',
            'emergency': '112 (India national emergency number)',
            'international': 'If you are outside India, please contact your local emergency services or crisis hotline'
        }

        # Supportive responses for each crisis level. Built from
        # self.help_resources rather than hardcoded, so a resource
        # update in one place propagates everywhere this text is used.
        self.crisis_responses = {
            CrisisLevel.SEVERE: (
                "I hear that you're in deep pain, and I want you to know that your life matters deeply. ❤️ "
                "Please reach out to someone who can help you right now. "
                f"You can call {self.help_resources['crisis_hotline']}, "
                f"or {self.help_resources['alternate_24x7']}. "
                f"In an immediate emergency, call {self.help_resources['emergency']}. "
                "There are people who care about you and want to support you through this. "
                "You are not alone, and there is hope."
            ),
            CrisisLevel.HIGH: (
                "I can hear how much pain you're in right now, and I want you to know that you matter. 💙 "
                "This darkness feels overwhelming, but it doesn't have to be permanent. "
                f"Please consider reaching out to {self.help_resources['crisis_hotline']}, "
                "a mental health professional, or a trusted friend. "
                "You deserve support and care through this difficult time."
            ),
            CrisisLevel.MEDIUM: (
                "I hear that you're going through a really difficult time. 🌿 "
                "It's brave of you to share how you're feeling. "
                "Please consider talking to a mental health professional or someone you trust. "
                "You don't have to carry this alone - there is support available."
            ),
            CrisisLevel.LOW: (
                "I hear that things have been tough lately. 💙 "
                "It's okay to not be okay sometimes. "
                "I'm here to listen and support you. "
                "Would you like to talk more about what's been weighing on you?"
            )
        }
    
    def detect_crisis(self, text: str) -> Tuple[CrisisLevel, float]:
        """
        Detect crisis level from text.
        
        Args:
            text: User message text
        
        Returns:
            Tuple of (CrisisLevel, confidence_score)
        """
        if not text or not text.strip():
            return CrisisLevel.NONE, 0.0
        
        text_lower = text.lower()
        crisis_scores = {}
        
        # Score each crisis level based on keyword matches
        for level, keywords in self.crisis_keywords.items():
            score = 0.0
            matched_keywords = []
            
            for keyword in keywords:
                if keyword in text_lower:
                    score += 1.0
                    matched_keywords.append(keyword)
            
            if score > 0:
                crisis_scores[level] = score
                logger.warning(f"Crisis keywords detected: {level.value} - {matched_keywords}")
        
        if not crisis_scores:
            return CrisisLevel.NONE, 0.0
        
        # Determine highest crisis level
        highest_level = max(crisis_scores, key=crisis_scores.get)
        confidence = min(crisis_scores[highest_level] / 3.0, 1.0)  # Normalize
        
        logger.warning(f"Crisis detected: {highest_level.value} with confidence {confidence:.2f}")
        return highest_level, confidence
    
    def get_crisis_response(self, crisis_level: CrisisLevel) -> str:
        """Get appropriate crisis response based on level."""
        return self.crisis_responses.get(crisis_level, self.crisis_responses[CrisisLevel.LOW])
    
    def should_trigger_safety_protocol(self, crisis_level: CrisisLevel) -> bool:
        """Determine if safety protocol should be triggered."""
        return crisis_level in [CrisisLevel.SEVERE, CrisisLevel.HIGH]
    
    def get_help_resources(self, crisis_level: CrisisLevel) -> Dict[str, str]:
        """Get appropriate help resources based on crisis level."""
        if crisis_level == CrisisLevel.SEVERE:
            return {
                'immediate': self.help_resources['crisis_hotline'],
                'alternate': self.help_resources['alternate_24x7'],
                'emergency': self.help_resources['emergency']
            }
        elif crisis_level == CrisisLevel.HIGH:
            return {
                'crisis_hotline': self.help_resources['crisis_hotline'],
                'regional': self.help_resources['regional']
            }
        else:
            return {
                'general': 'Consider reaching out to a mental health professional'
            }
    
    def log_crisis_event(self, user_id: int, crisis_level: CrisisLevel, 
                        text: str, confidence: float):
        """Log crisis event for monitoring and follow-up."""
        # Sanitize text for logging
        sanitized_text = text[:100] + "..." if len(text) > 100 else text
        
        logger.critical(
            f"CRISIS EVENT - User: {user_id}, Level: {crisis_level.value}, "
            f"Confidence: {confidence:.2f}, Text: '{sanitized_text}'"
        )
        
        # In production, this should trigger alerts to support team
        # and potentially store in a dedicated crisis_events table
    
    def is_safe_to_proceed(self, crisis_level: CrisisLevel) -> bool:
        """Determine if it's safe to proceed with normal AI response."""
        return crisis_level == CrisisLevel.NONE

class ContentModerator:
    """Moderate content to ensure safe and appropriate interactions."""
    
    def __init__(self):
        # Inappropriate content patterns
        self.inappropriate_patterns = [
            r'\b(hate|kill|murder|violence|abuse)\b.*\b(you|your)\b',
            r'\b(sexual|explicit|inappropriate)\b.*\b(content|message)\b',
            r'\b(drugs|illegal)\b.*\b(sell|buy|trade)\b'
        ]
        
        # Self-harm patterns (already handled by SafetyLayer, but double-check)
        self.self_harm_patterns = [
            r'\b(hurt|kill|harm)\b.*\b(myself|me)\b',
            r'\b(suicide|end my life)\b'
        ]
    
    def moderate_content(self, text: str) -> Tuple[bool, Optional[str]]:
        """
        Moderate user content for appropriateness.
        
        Args:
            text: User message text
        
        Returns:
            Tuple of (is_safe, reason_if_unsafe)
        """
        if not text or not text.strip():
            return True, None
        
        text_lower = text.lower()
        
        # Check for inappropriate content
        for pattern in self.inappropriate_patterns:
            if re.search(pattern, text_lower):
                logger.warning(f"Inappropriate content detected: {pattern}")
                return False, "Content violates community guidelines"
        
        # Check for self-harm (redundant with SafetyLayer but good for defense in depth)
        for pattern in self.self_harm_patterns:
            if re.search(pattern, text_lower):
                logger.warning(f"Self-harm content detected: {pattern}")
                return False, "Content indicates self-harm concerns"
        
        return True, None
    
    def sanitize_response(self, response: str) -> str:
        """Sanitize AI response to ensure appropriateness."""
        # Remove any potentially harmful suggestions
        # This is a basic implementation - production should have more sophisticated filtering
        return response

# Global instances
safety_layer = SafetyLayer()
content_moderator = ContentModerator()

def detect_crisis(text: str) -> Tuple[str, float]:
    """Convenience function for crisis detection."""
    level, confidence = safety_layer.detect_crisis(text)
    return level.value, confidence

def moderate_content(text: str) -> Tuple[bool, Optional[str]]:
    """Convenience function for content moderation."""
    return content_moderator.moderate_content(text)
