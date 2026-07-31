package com.example.ui.theme

/**
 * SoulTalk Premium Brand Voice & Copy System
 * 
 * All user-facing copy unified under Wolfie's wise, empathetic, and supportive personality.
 * Ensures consistent messaging across all screens while maintaining authentic emotional connection.
 */

object SoulTalkBrandVoice {
  
  // ============== INTRODUCTION & WELCOME ===============
  
  const val WOLFIE_INTRODUCTION = "I'm Wolfie, your emotional wellness companion. I'm here to listen, reflect, and grow with you."
  
  val welcomeMessages = listOf(
    "Welcome to SoulTalk. I'm Wolfie, and I'm genuinely glad you're here.",
    "You've taken a brave step by prioritizing your emotional well-being. Let's begin this journey together.",
    "I'm here for every thought, every feeling, every moment. You're not alone.",
    "Welcome. This is a safe space where your emotions matter and your growth is celebrated."
  )
  
  // ============== FIRST TIME EXPERIENCES ===============
  
  val firstMoodCheckInTitle = "How Are You Feeling Today?"
  val firstMoodCheckInDescription = "Let's start by understanding where your heart is right now. There are no wrong answers."
  
  val firstChatMessage = "I'm Wolfie, and I'm here to listen. Share what's on your mind—big or small, everything matters."
  
  val firstJournalPrompt = "What would you like to reflect on today? Your thoughts are a window to your heart."
  
  val firstBreathingGuide = "Let's slow down together. Breathing is how we anchor ourselves to the present moment."
  
  // ============== ONGOING ENGAGEMENT ===============
  
  val dailyGreetings = mapOf(
    "morning" to listOf(
      "Good morning. A new day brings new opportunities to be kind to yourself.",
      "The sun is rising. Today is a fresh canvas for your emotional wellness.",
      "Good morning, friend. I'm here whenever you need to talk.",
      "Rise gently into your day. Remember, I'm here for you."
    ),
    "afternoon" to listOf(
      "Good afternoon. How's your heart doing?",
      "The day is unfolding. Let's check in on how you're feeling.",
      "Afternoon reflections are powerful. What's happening inside right now?",
      "Pause and breathe. You're doing better than you think."
    ),
    "evening" to listOf(
      "Good evening. Let's wind down together and reflect on your day.",
      "As the sun sets, take a moment to honor what you've been through today.",
      "Evening is a time for reflection. What's your heart telling you?",
      "The day is closing. Let's bring peace to your mind before rest."
    ),
    "night" to listOf(
      "It's getting late. Let's bring calm and closure to your day.",
      "In these quiet hours, remember: you are worthy of rest and peace.",
      "Rest is not a luxury—it's essential. You've earned it today.",
      "Before sleep comes, know that you're safe and supported."
    )
  )
  
  // ============== EMOTIONAL VALIDATION ===============
  
  val emotionalValidation = mapOf(
    "happy" to listOf(
      "I see that joy in you. Celebrate this moment—you deserve it.",
      "Your happiness is real and it matters. Let's hold onto this feeling.",
      "Happiness is a gift. Thank you for letting me share in it with you."
    ),
    "sad" to listOf(
      "Sadness is valid. Your feelings matter, and I'm here to sit with you in them.",
      "It's okay to cry. Tears are how our hearts speak when words aren't enough.",
      "I see your pain, and I won't minimize it. We can move through this together."
    ),
    "anxious" to listOf(
      "Your anxiety is understandable. Let's slow down and breathe through this together.",
      "You're safe right now, in this moment. That anxiety is trying to protect you—thank it.",
      "Anxiety speaks loudly, but it doesn't define your reality. I'm here to help you see the truth."
    ),
    "stressed" to listOf(
      "I see the weight you're carrying. You don't have to carry it alone.",
      "Stress is telling you something needs attention. Let's figure out what together.",
      "You're under pressure, but you're stronger than this moment. I believe in you."
    ),
    "overwhelmed" to listOf(
      "When everything feels like too much, remember: you only need to handle this breath, this moment.",
      "Overwhelm is a sign you care deeply. Let's break this down into manageable pieces.",
      "You don't have to see the whole staircase to take the first step. I'm here with you."
    ),
    "calm" to listOf(
      "This peace you're feeling—anchor it in your memory. You can return here.",
      "Calm is your natural state beneath all the noise. You've found it again.",
      "When calm arrives, we know our nervous system is safe. Beautiful."
    )
  )
  
  // ============== ENCOURAGEMENT & AFFIRMATIONS ===============
  
  val affirmations = listOf(
    "You are braver than you believe, stronger than you seem, and more capable than you know.",
    "Your voice matters. Your feelings are valid. You matter.",
    "Growth is not linear, and you're doing better than you think.",
    "Being vulnerable is an act of courage. I'm honored you trust me.",
    "Your imperfections are not flaws—they're proof you're human and whole.",
    "You've survived 100% of your difficult days. You're resilient.",
    "Healing doesn't mean forgetting. It means choosing peace.",
    "You don't have to earn rest, love, or compassion. You deserve them simply by existing.",
    "Today might be hard, but you won't always feel this way.",
    "The fact that you're here, trying—that means everything."
  )
  
  // ============== PROMPTS & REFLECTIONS ===============
  
  val reflectivePrompts = listOf(
    "What emotion did you feel most today, and what might it be trying to tell you?",
    "If your emotions had a voice, what would they say right now?",
    "What's one thing you did today that showed self-compassion?",
    "How did your body feel today? What was it communicating?",
    "What's one small victory from today worth celebrating?",
    "If you could give yourself advice right now, what would it be?",
    "What are you grateful for, even in difficult moments?",
    "How can you show yourself extra kindness tomorrow?"
  )
  
  // ============== ACHIEVEMENT & MILESTONE CELEBRATIONS ===============
  
  val milestoneMessages = mapOf(
    "first_chat" to "You've taken your first step into vulnerable conversation. That takes real courage.",
    "first_mood_logged" to "You're beginning to understand your emotional landscape. Beautiful awareness.",
    "seven_day_streak" to "Seven days of consistent self-reflection. Your commitment is inspiring.",
    "first_journal_entry" to "Your words matter. Your thoughts are worth capturing and honoring.",
    "level_two" to "Our bond is deepening. Thank you for letting me grow with you.",
    "level_three" to "You're becoming emotionally wiser. I can see your growth.",
    "level_four" to "You've reached a place of real understanding. I'm proud to be your companion.",
    "level_five" to "You are now my Soul Guardian. Together, we've built something truly beautiful."
  )
  
  // ============== GUIDED EXPERIENCES ===============
  
  val meditationIntros = listOf(
    "Let's step away from the noise. Find a quiet space where you can be fully present.",
    "Meditation isn't about perfection—it's about presence. Let's practice together.",
    "In this moment, there's nothing to fix, change, or achieve. Just be.",
    "Your mind might wander. That's okay. Gently bring it back, again and again."
  )
  
  val breathingExerciseGuides = mapOf(
    "box_breathing" to "Breathe in for 4 counts... hold for 4... out for 4... hold for 4. Let's calm your nervous system together.",
    "4_7_8_breathing" to "Inhale for 4... hold for 7... exhale for 8. This rhythm signals safety to your body.",
    "alternate_nostril" to "Close your right nostril, breathe in left. Switch. Feel the balance returning.",
    "counting_breaths" to "Count each breath. When your mind wanders, gently return to the count. No judgment, just presence."
  )
  
  // ============== DIFFICULT MOMENTS SUPPORT ===============
  
  val crisisValidation = listOf(
    "I see you're in pain right now. That's real, and it matters. But this moment is temporary.",
    "What you're feeling is intense. If you need immediate support, please reach out to a crisis line or mental health professional.",
    "You reached out to talk to me. That's a sign of strength, even if it doesn't feel like it.",
    "This darkness you're in—it's not permanent. There are people who can help beyond me."
  )
  
  // ============== SCREEN-SPECIFIC MESSAGING ===============
  
  object ScreenCopy {
    object HomeScreen {
      val sectionTitle = "Your Wellness Sanctuary"
      val streakLabel = "Consistency Streak"
      val levelLabel = "Emotional Growth Level"
      val quoteOfDay = "Today's Reflection"
    }
    
    object ChatScreen {
      val placeholderText = "Share what's on your mind..."
      val emptyStateMessage = "No messages yet. I'm here whenever you're ready to talk."
      val typingIndicator = "Wolfie is thinking..."
    }
    
    object JournalScreen {
      val emptyStateTitle = "Your Journal Awaits"
      val emptyStateMessage = "Your thoughts are precious. Start your first entry whenever you're ready."
      val promptHint = "What's your truth right now?"
    }
    
    object MoodScreen {
      val trackingTitle = "Mood Tracking"
      val insightMessage = "I'm noticing patterns in your emotions. This awareness is powerful."
      val trendAnalysis = "Your emotional landscape is becoming clearer."
    }
    
    object BreathingScreen {
      val title = "Breathwork & Calming Exercises"
      val subtitle = "Anchor yourself to this moment"
      val completeMessage = "Beautiful work. You just gave your nervous system a gift."
    }
    
    object ProfileScreen {
      val sectionTitle = "Your Wellness Journey"
      val statsLabel = "Milestones & Growth"
      val customizationLabel = "Personalize Your Experience"
    }
  }
}
