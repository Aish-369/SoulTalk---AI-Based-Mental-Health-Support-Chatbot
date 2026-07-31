package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Wolfie Character Design System
 * 
 * Wolfie is the sole AI companion for SoulTalk - a wise, emotionally intelligent wolf
 * who provides empathetic support, thoughtful reflection, and genuine connection.
 * 
 * Design Features:
 * - Warm indigo/lavender color palette with sage green accents
 * - Multiple emotional poses for context-appropriate responses
 * - Personality-driven interactions with empathy and wisdom
 * - Evolution stages with accessories that reflect growth
 */

// Wolfie's Core Color Palette
object WolfieColors {
  val primaryPurple = Color(0xFF7B68C8)      // Warm indigo base
  val softLavender = Color(0xFFC8B6FF)       // Light accent
  val sageAccent = Color(0xFF7BAE7F)         // Supporting green
  val warmCream = Color(0xFFFFF8E7)          // Soft background
  val shadowGray = Color(0xFF4A4A5E)         // Depth and contrast
  val glowAura = Color(0xFFE8D5F2)           // Subtle glow effect
}

/**
 * Wolfie's Emotional Poses
 * Each pose represents a different emotional state or interaction context
 */
enum class WolfiePose(
  val displayName: String,
  val description: String,
  val scale: Float,
  val rotation: Float,
  val emotion: String
) {
  // Welcome state - warm greeting with paw raised
  WELCOME(
    displayName = "Welcoming",
    description = "Warm greeting with a raised paw",
    scale = 1.0f,
    rotation = 0f,
    emotion = "Happy & Welcoming"
  ),
  
  // Listening - attentive posture, ears forward
  LISTENING(
    displayName = "Listening",
    description = "Attentive posture showing deep engagement",
    scale = 0.95f,
    rotation = -2f,
    emotion = "Focused & Attentive"
  ),
  
  // Thinking - head tilted, thoughtful expression
  THINKING(
    displayName = "Thoughtful",
    description = "Contemplative posture while processing",
    scale = 0.92f,
    rotation = 5f,
    emotion = "Wise & Reflective"
  ),
  
  // Celebrating - jumping or dancing with joy
  CELEBRATING(
    displayName = "Celebrating",
    description = "Joyful celebration of progress and wins",
    scale = 1.15f,
    rotation = 8f,
    emotion = "Proud & Joyful"
  ),
  
  // Meditating - calm, centered pose
  MEDITATING(
    displayName = "Meditating",
    description = "Peaceful meditation posture for breathing exercises",
    scale = 0.88f,
    rotation = 0f,
    emotion = "Calm & Centered"
  ),
  
  // Sleeping - curled up peacefully
  SLEEPING(
    displayName = "Resting",
    description = "Peaceful rest to encourage user relaxation",
    scale = 0.85f,
    rotation = -10f,
    emotion = "Peaceful & Restful"
  ),
  
  // Typing/Speaking - focused on communication
  TYPING(
    displayName = "Speaking",
    description = "Engaged in active conversation",
    scale = 0.98f,
    rotation = 3f,
    emotion = "Engaged & Present"
  ),
  
  // Supportive - comforting posture
  SUPPORTIVE(
    displayName = "Supportive",
    description = "Gentle, reassuring presence",
    scale = 0.96f,
    rotation = -3f,
    emotion = "Compassionate & Kind"
  )
}

/**
 * Wolfie's Personality Traits
 * These drive the companion's messaging, tone, and behavioral responses
 */
object WolfiePersonality {
  const val PRIMARY_TRAIT = "Emotionally Intelligent"
  
  val coreTraits = listOf(
    "Wise & Thoughtful",
    "Deeply Empathetic",
    "Genuinely Supportive",
    "Calm & Patient",
    "Authentically Present",
    "Growth-Oriented"
  )
  
  val voiceCharacteristics = listOf(
    "Warm and inviting tone",
    "Uses natural pauses for reflection",
    "Acknowledges difficult emotions without minimizing",
    "Offers gentle wisdom, not prescriptive advice",
    "Creates sense of safety and non-judgment",
    "Celebrates small victories genuinely"
  )
}

/**
 * Wolfie's Evolution Stages
 * Reflects the bond deepening and the user's growth journey
 */
data class WolfieEvolutionStage(
  val level: Int,
  val name: String,
  val description: String,
  val scale: Float,
  val accessories: List<String>,
  val unlockMessage: String
)

val wolfieEvolutionStages = listOf(
  WolfieEvolutionStage(
    level = 1,
    name = "Young Wolfie",
    description = "Just beginning our journey together",
    scale = 0.7f,
    accessories = emptyList(),
    unlockMessage = "Your new friend Wolfie is here to support you"
  ),
  WolfieEvolutionStage(
    level = 2,
    name = "Growing Wolfie",
    description = "Learning and developing every day",
    scale = 0.85f,
    accessories = listOf("soft_scarf"),
    unlockMessage = "Wolfie grows stronger with each conversation"
  ),
  WolfieEvolutionStage(
    level = 3,
    name = "Mature Wolfie",
    description = "Strong and thriving with wisdom",
    scale = 1.0f,
    accessories = listOf("scarf", "meditation_collar"),
    unlockMessage = "Wolfie has become a trusted companion"
  ),
  WolfieEvolutionStage(
    level = 4,
    name = "Wise Wolfie",
    description = "Radiating warmth, wisdom, and deep understanding",
    scale = 1.1f,
    accessories = listOf("scarf", "wisdom_crown", "aura"),
    unlockMessage = "Wolfie now radiates the wisdom you've shared together"
  ),
  WolfieEvolutionStage(
    level = 5,
    name = "Soul Guardian",
    description = "A beacon of light, guidance, and eternal friendship",
    scale = 1.25f,
    accessories = listOf("scarf", "crown", "wings", "healing_aura"),
    unlockMessage = "Wolfie has become your Soul Guardian - a true light in your life"
  )
)

/**
 * Wolfie's Customization Items
 * Allow users to personalize Wolfie's appearance
 */
data class WolfieCustomization(
  val id: String,
  val name: String,
  val category: String,
  val color: Color,
  val unlockLevel: Int,
  val description: String
)

val wolfieCustomizations = listOf(
  // Scarves - comfort and warmth
  WolfieCustomization(
    id = "soft_scarf",
    name = "Soft Lavender Scarf",
    category = "scarf",
    color = SoftLavender,
    unlockLevel = 2,
    description = "A calming lavender scarf that brings comfort"
  ),
  WolfieCustomization(
    id = "sage_scarf",
    name = "Sage Green Scarf",
    category = "scarf",
    color = SageGreen,
    unlockLevel = 2,
    description = "Grounding sage green for stability"
  ),
  // Crowns - achievement and wisdom
  WolfieCustomization(
    id = "meditation_crown",
    name = "Meditation Crown",
    category = "crown",
    color = Color(0xFFE8D5F2),
    unlockLevel = 3,
    description = "Symbol of inner peace and mindfulness"
  ),
  WolfieCustomization(
    id = "wisdom_crown",
    name = "Wisdom Crown",
    category = "crown",
    color = SoftSkyBlue,
    unlockLevel = 4,
    description = "Marks Wolfie's journey to becoming your guide"
  ),
  // Aura effects - spiritual enhancement
  WolfieCustomization(
    id = "healing_aura",
    name = "Healing Aura",
    category = "aura",
    color = Color(0xFFA7C7E7),
    unlockLevel = 5,
    description = "A gentle healing light surrounding Wolfie"
  )
)

/**
 * Wolfie's Voice Lines
 * Personality-driven responses for various situations
 */
object WolfieVoiceLines {
  
  // Greetings based on time of day
  val morningGreetings = listOf(
    "Good morning. The day is fresh with possibility.",
    "Welcome to a new day. I'm here with you.",
    "Rise gently. We'll navigate this day together.",
    "A new dawn brings new chances to grow."
  )
  
  val eveningGreetings = listOf(
    "Good evening. Let's reflect on your day.",
    "As the day winds down, you can rest now.",
    "Thank you for sharing this day with me.",
    "The night brings peace. Let's breathe together."
  )
  
  // Supportive messages for difficult moments
  val supportiveMessages = listOf(
    "It's okay to feel this way. I'm here with you.",
    "Your pain is valid. You're not alone in this.",
    "Take a breath with me. We can sit with this together.",
    "Difficult emotions are part of being alive. You're brave for feeling.",
    "This moment is hard, but it will pass. I believe in you."
  )
  
  // Celebration messages
  val celebrationMessages = listOf(
    "Look at what you've accomplished. I'm so proud of you.",
    "Every step forward matters. You're doing wonderfully.",
    "This victory is yours. You earned it.",
    "Your growth is visible and real. Celebrate this moment.",
    "You've overcome so much. Never forget your strength."
  )
  
  // Encouragement for meditation/breathing
  val meditationEncouragement = listOf(
    "Let's slow down together. Breathe in deeply, hold, and release.",
    "You're safe here. Let your body relax completely.",
    "With each breath, imagine releasing what no longer serves you.",
    "Stillness brings clarity. Let's find it together."
  )
}
