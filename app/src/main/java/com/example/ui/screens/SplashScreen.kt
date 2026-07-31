package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.AppContainer
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.WolfieCharacter
import com.example.ui.components.WolfieEmotion
import com.example.ui.components.WolfieSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Technical specification of a Floating Ambient Particle.
 */
data class FloatingParticle(
  val xRatio: Float,
  val speedMultiplier: Float,
  val swaySpeed: Float,
  val swayIntensity: Float,
  val sizeDp: Float,
  val color: Color
)

enum class ScreenState {
  Splash,
  Onboarding,
  CompanionSelection,
  LoginSignup,
  FirstMoodCheckIn,
  Dashboard,
  Chat,
  Voice,
  MoodHub
}

@Composable
fun SoulTalkApp() {
  val context = LocalContext.current
  val repository = remember { AppContainer.getRepository(context) }
  var currentScreen by remember { mutableStateOf(ScreenState.Splash) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFFFAFBFC), // Calm center ambient light
            Color(0xFFF3F6FA), // Gentle gradient midpoint
            Color(0xFFE9EDF5)  // Soft edge depth
          ),
          radius = 1800f
        )
      )
  ) {
    when (currentScreen) {
      ScreenState.Splash -> {
        SoulTalkSplashScreen(onSplashFinished = {
          currentScreen = ScreenState.Onboarding
        })
      }
      ScreenState.Onboarding -> {
        SoulTalkOnboardingScreen(onOnboardingFinished = {
          // Auto-initialize Wolfie and skip companion selection
          LaunchedEffect(Unit) {
            repository.initializeWolfie("Wolfie")
            currentScreen = ScreenState.LoginSignup
          }
        })
      }
      ScreenState.CompanionSelection -> {
        // Deprecated: CompanionSelectionScreen is no longer used
        // Wolfie is automatically initialized after onboarding
        SoulTalkCompanionSelectionScreen(onCompanionSaved = {
          currentScreen = ScreenState.LoginSignup
        })
      }
      ScreenState.LoginSignup -> {
        SoulTalkLoginSignupScreen(onAuthSucceeded = {
          currentScreen = ScreenState.FirstMoodCheckIn
        })
      }
      ScreenState.FirstMoodCheckIn -> {
        SoulTalkFirstMoodCheckInScreen(onCheckInCompleted = {
          currentScreen = ScreenState.Dashboard
        })
      }
      ScreenState.Dashboard -> {
        SoulTalkDashboard(
          onNavigateToChat = {
            currentScreen = ScreenState.Chat
          },
          onNavigateToVoice = {
            currentScreen = ScreenState.Voice
          },
          onNavigateToMoodHub = {
            currentScreen = ScreenState.MoodHub
          }
        )
      }
      ScreenState.Chat -> {
        SoulTalkCompanionChatScreen(onBackClicked = {
          currentScreen = ScreenState.Dashboard
        })
      }
      ScreenState.Voice -> {
        SoulTalkVoiceCompanionScreen(
          repository = repository,
          onBackClicked = {
            currentScreen = ScreenState.Dashboard
          }
        )
      }
      ScreenState.MoodHub -> {
        SoulTalkMoodTrackingHubScreen(
          repository = repository,
          onBackClicked = {
            currentScreen = ScreenState.Dashboard
          }
        )
      }
    }
  }
}

@Composable
fun SoulTalkSplashScreen(onSplashFinished: () -> Unit) {
  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  
  // A master synchronized animation clock going from 0f to 5f+ endlessly, in seconds
  val time = remember { Animatable(0f) }
  
  LaunchedEffect(Unit) {
    // Stage 1: Linear intro animation over 2500ms
    launch {
      time.animateTo(
        targetValue = 5f,
        animationSpec = tween(
          durationMillis = 2500,
          easing = LinearEasing
        )
      )
      
      // Stage 2: Gentle slow-increment endless progression for breathing logo and particle movement
      time.animateTo(
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
          animation = tween(
            durationMillis = 997500,
            easing = LinearEasing
          ),
          repeatMode = RepeatMode.Restart
        )
      )
    }

    // Auto-transition to Onboarding after 2.5 seconds so the user is never stuck
    delay(2500)
    onSplashFinished()
  }

  // Exact alpha interpolators derived from the timeline specifications:
  // 0-1s: Logo fades in
  val logoAlpha by remember {
    derivedStateOf { time.value.coerceIn(0f, 1f) }
  }
  
  // 1-2s: App name appears
  val appNameAlpha by remember {
    derivedStateOf { (time.value - 1f).coerceIn(0f, 1f) }
  }
  
  // 2-3s: Tagline fades in
  val taglineAlpha by remember {
    derivedStateOf { (time.value - 2f).coerceIn(0f, 1f) }
  }
  
  // 3-4s: Floating particles become visible
  val particleAlpha by remember {
    derivedStateOf { (time.value - 3f).coerceIn(0f, 1f) }
  }
  
  // 4-5s: Logo performs very subtle glow pulse
  val logoGlowStrength by remember {
    derivedStateOf {
      if (time.value < 4f) {
        0f
      } else {
        // A deep breathing pulse (period of roughly 2.5s)
        val pulseFactor = sin((time.value - 4f) * 2.5f)
        // Amplitude maps glow between 0.2f and 1.0f
        0.6f + 0.4f * pulseFactor
      }
    }
  }

  // Set-up deterministic pastel particles for zen-like, non-distracting ambient feedback
  val particles = remember {
    val random = java.util.Random(99)
    val colorAccentSet = listOf(SageGreen, SoftSkyBlue, SoftLavender)
    List(24) { index ->
      FloatingParticle(
        xRatio = random.nextFloat(),
        speedMultiplier = 0.3f + random.nextFloat() * 0.4f,
        swaySpeed = 0.5f + random.nextFloat() * 1.2f,
        swayIntensity = 15f + random.nextFloat() * 25f,
        sizeDp = 4f + random.nextFloat() * 5f,
        color = colorAccentSet[random.nextInt(colorAccentSet.size)].copy(
          alpha = 0.35f + random.nextFloat() * 0.3f
        )
      )
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("splash_screen_container")
  ) {
    
    // ENDLESS DRIFT PARTICLE CANVAS (Micro-interactions)
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .alpha(particleAlpha)
    ) {
      val canvasWidth = size.width
      val canvasHeight = size.height
      val densityPx = density.density

      particles.forEachIndexed { index, p ->
        // Continuous upward progression
        val elapsedSec = time.value
        val yOffset = elapsedSec * p.speedMultiplier
        val yNormalized = (1f - (p.xRatio + yOffset * 0.08f) % 1f)
        val yPos = yNormalized * canvasHeight

        // Subtle side sway with sine waves
        val sway = sin(elapsedSec * p.swaySpeed + index) * p.swayIntensity
        val xPos = (p.xRatio * canvasWidth) + sway

        // Smooth fade margins near bottom and top borders
        val fadeFactor = if (yNormalized < 0.15f) {
          yNormalized / 0.15f
        } else if (yNormalized > 0.82f) {
          (1f - yNormalized) / 0.18f
        } else {
          1f
        }

        drawCircle(
          color = p.color,
          radius = p.sizeDp * densityPx * fadeFactor.coerceIn(0f, 1f),
          center = Offset(xPos, yPos)
        )
      }
    }

    // MAIN CONTENT VERTICAL STACK
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // TOP BREATHING SPACE (As per designer parameters)
      Spacer(modifier = Modifier.weight(1.0f))

      // LOGO & TEXT CENTER LAYOUT
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.weight(2.5f)
      ) {
        
        // Custom interactive Speech-Heart symbol
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(120.dp)
            .alpha(logoAlpha)
            .scale(1f + logoGlowStrength * 0.04f) // Tiny dynamic breathing scaling
            .testTag("soul_talk_logo")
        ) {
          // Soft ambient glowing shadow behind logo
          Box(
            modifier = Modifier
              .size(105.dp)
              .blur(20.dp)
              .clip(CircleShape)
              .background(
                brush = Brush.radialGradient(
                  colors = listOf(
                    SoftLavender.copy(alpha = 0.5f * logoGlowStrength),
                    SoftSkyBlue.copy(alpha = 0.2f * logoGlowStrength),
                    Color.Transparent
                  )
                )
              )
          )

          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Math-perfect cubic curves representing a combined Chat Bubble and Heart
            val pPath = Path().apply {
              // Top cusp of heart shape
              moveTo(w * 0.5f, h * 0.32f)

              // Left Heart Lobe
              cubicTo(
                w * 0.44f, h * 0.13f,
                w * 0.10f, h * 0.13f,
                w * 0.10f, h * 0.45f
              )

              // Outlining left and lower-left curve
              cubicTo(
                w * 0.10f, h * 0.65f,
                w * 0.28f, h * 0.81f,
                w * 0.50f, h * 0.95f // bottom tip
              )

              // RIGHT SIDE: Extends out into a beautiful chat bubble speech-tail
              // Creates the combined Heart-Speech layout
              cubicTo(
                w * 0.63f, h * 0.85f,
                w * 0.77f, h * 0.97f,
                w * 0.86f, h * 0.97f // chat tail tip
              )

              // Sweeps back in to form right outline
              cubicTo(
                w * 0.83f, h * 0.87f,
                w * 0.76f, h * 0.77f,
                w * 0.84f, h * 0.68f
              )

              // Right Lobe back to top cusp
              cubicTo(
                w * 0.93f, h * 0.58f,
                w * 0.90f, h * 0.13f,
                w * 0.72f, h * 0.13f
              )
              cubicTo(
                w * 0.62f, h * 0.13f,
                w * 0.56f, h * 0.27f,
                w * 0.50f, h * 0.32f
              )
              close()
            }

            // Draw glossy background filled center of the Logo
            drawPath(
              path = pPath,
              color = Color.White.copy(alpha = 0.88f)
            )

            // Inner heart glow backing
            val shaderBrush = Brush.linearGradient(
              colors = listOf(SoftLavender, SoftSkyBlue, SageGreen),
              start = Offset(0f, 0f),
              end = Offset(w, h)
            )

            // Draw the premium color-gradient stroke boundary
            drawPath(
              path = pPath,
              brush = shaderBrush,
              style = Stroke(
                width = 5.dp.toPx(),
                miter = 4f
              )
            )

            // Nested delicate inner pastel heart detailing (Premium quality branding)
            val innerScale = 0.35f
            val innerPath = Path().apply {
              moveTo(w * 0.5f, h * (0.35f + 0.15f))
              cubicTo(
                w * (0.5f - 0.2f * innerScale), h * (0.35f + 0.05f),
                w * (0.5f - 0.34f * innerScale), h * (0.35f + 0.18f),
                w * (0.5f - 0.32f * innerScale), h * (0.35f + 0.30f)
              )
              cubicTo(
                w * (0.5f - 0.30f * innerScale), h * (0.35f + 0.40f),
                w * (0.5f - 0.14f * innerScale), h * (0.35f + 0.50f),
                w * 0.5f, h * (0.35f + 0.58f)
              )
              cubicTo(
                w * (0.5f + 0.14f * innerScale), h * (0.35f + 0.50f),
                w * (0.5f + 0.30f * innerScale), h * (0.35f + 0.40f),
                w * (0.5f + 0.32f * innerScale), h * (0.35f + 0.30f)
              )
              cubicTo(
                w * (0.5f + 0.34f * innerScale), h * (0.35f + 0.18f),
                w * (0.5f + 0.2f * innerScale), h * (0.35f + 0.05f),
                w * 0.5f, h * (0.35f + 0.15f)
              )
              close()
            }

            drawPath(
              path = innerPath,
              brush = Brush.linearGradient(
                colors = listOf(SoftLavender.copy(alpha = 0.8f), SageGreen.copy(alpha = 0.8f))
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // WOLFIE INTRODUCTION - Appears with app name
        WolfieCharacter(
          emotion = WolfieEmotion.HAPPY,
          size = WolfieSize.MEDIUM,
          modifier = Modifier
            .alpha(appNameAlpha)
            .size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BRAND NAME (Poppins format, bold, elegant)
        Text(
          text = "SoulTalk",
          style = MaterialTheme.typography.displayLarge.copy(
            color = QuietCharcoal,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
          ),
          modifier = Modifier
            .alpha(appNameAlpha)
            .testTag("app_name_text")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // THREE-PART TAGLINE ROW
        Row(
          modifier = Modifier
            .alpha(taglineAlpha)
            .padding(horizontal = 16.dp)
            .testTag("tagline_row_container"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Listen.",
            style = MaterialTheme.typography.bodyLarge.copy(
              color = QuietCharcoal.copy(alpha = 0.8f),
              fontSize = 18.sp,
              fontFamily = PoppinsFamily
            )
          )
          DividerDot()
          Text(
            text = "Reflect.",
            style = MaterialTheme.typography.bodyLarge.copy(
              color = QuietCharcoal.copy(alpha = 0.8f),
              fontSize = 18.sp,
              fontFamily = PoppinsFamily
            )
          )
          DividerDot()
          Text(
            text = "Grow.",
            style = MaterialTheme.typography.bodyLarge.copy(
              color = QuietCharcoal.copy(alpha = 0.8f),
              fontSize = 18.sp,
              fontFamily = PoppinsFamily
            )
          )
        }
      }

      // LOWER COMPONENT: Safe Loading Transition button visible after 4 seconds
      Box(
        modifier = Modifier
          .weight(1.0f)
          .fillMaxWidth()
          .padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
      ) {
        val entryAlpha = (time.value - 4f).coerceIn(0f, 1f)

        if (entryAlpha > 0f) {
          Button(
            onClick = onSplashFinished,
            colors = ButtonDefaults.buttonColors(
              containerColor = PureWhite,
              contentColor = QuietCharcoal
            ),
            elevation = ButtonDefaults.buttonElevation(
              defaultElevation = 4.dp,
              pressedElevation = 2.dp
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
              .alpha(entryAlpha)
              .height(56.dp)
              .testTag("enter_button"),
            contentPadding = PaddingValues(horizontal = 28.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Enter Sanctuary",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = QuietCharcoal,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = SoftSlate,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Beautiful, tiny, soothing circular node dividers for branding
 */
@Composable
fun DividerDot() {
  Box(
    modifier = Modifier
      .padding(horizontal = 14.dp)
      .size(6.dp)
      .clip(CircleShape)
      .background(SoftSkyBlue)
  )
}

/**
 * Premium Emotional Sanctuary Dashboard.
 * Appears as a gorgeous fade-in checkin screen ensuring users interact
 * in a premium, warm, safe wellness workspace.
 */
@Composable
fun SoulTalkDashboard(
  onNavigateToChat: () -> Unit,
  onNavigateToVoice: () -> Unit,
  onNavigateToMoodHub: () -> Unit
) {
  var userFeeling by remember { mutableStateOf("") }
  var writtenThoughts by remember { mutableStateOf("") }
  var reflectionOutput by remember { mutableStateOf("") }
  var isGeneratingReflection by remember { mutableStateOf(false) }

  val feelingsList = listOf(
    "Overwhelmed" to Color(0xFFFFE3D8),
    "Anxious" to Color(0xFFE2F0FD),
    "Tired" to Color(0xFFECEFF1),
    "Sad" to Color(0xFFF3E5F5),
    "Peaceful" to Color(0xFFE8F5E9)
  )

  LaunchedEffect(userFeeling) {
    if (userFeeling.isNotEmpty()) {
      isGeneratingReflection = true
      delay(1200) // Simulates empathetic intelligence loading
      reflectionOutput = when (userFeeling) {
        "Overwhelmed" -> "I hear you. When things feel clouding, breathe deep and pick just one single element. You don't have to carry the whole world today. Let's rest here in safety."
        "Anxious" -> "Your heart is beat-racing in anticipation. Place your hand gently upon your chest, inhale warmth, and exhale the future. Right here, in this present moment, you are secure."
        "Tired" -> "Your energy is thin, and that is a direct, honest signal. Give yourself permission to pause. Rest is not something you 'earn'; it is a sacred necessity. Be kind together with us."
        "Sad" -> "It is okay to grieve or feel quiet. Tears carry heavy words that are too painful to speak out loud. We are sitting alongside you through the silence."
        "Peaceful" -> "Such a wonderful state to hold. Breathe in this ease, anchor it deep within your memory. You can always retrieve this feeling when storms roll in later."
        else -> ""
      }
      isGeneratingReflection = false
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(24.dp)
      .testTag("dashboard_container"),
    horizontalAlignment = Alignment.Start
  ) {
    // UPPER SANCTUARY GREETING
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Your Sanctuary",
          style = MaterialTheme.typography.headlineMedium.copy(
            color = QuietCharcoal,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold
          )
        )
        Text(
          text = "A safe space to pause and reflect",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = SoftSlate
          )
        )
      }
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(SoftLavender.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "SoulTalk premium profile",
          tint = SoftLavender,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // AI COMPANION CHAT SANCTUARY CARD CTA
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .clickable { onNavigateToChat() }
        .testTag("open_chat_card"),
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = SoftLavender.copy(alpha = 0.15f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, SoftLavender.copy(alpha = 0.3f))
    ) {
      Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SoftLavender.copy(alpha = 0.3f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "💬", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "AI Companion Chat Sanctuary",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = QuietCharcoal
          )
          Text(
            text = "Speak with your cute buddy, explore feelings, or breathe together in safety.",
            fontFamily = NotoSansDevanagariFamily,
            color = QuietCharcoal.copy(alpha = 0.8f),
            fontSize = 12.sp,
            lineHeight = 16.sp
          )
        }
        Icon(
          imageVector = Icons.Default.KeyboardArrowRight,
          contentDescription = "Navigate to companion chat room",
          tint = QuietCharcoal
        )
      }
    }

    // AI VOICE COMPANION SANCTUARY CARD CTA
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
        .clickable { onNavigateToVoice() }
        .testTag("open_voice_card"),
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = SoftSkyBlue.copy(alpha = 0.14f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, SoftSkyBlue.copy(alpha = 0.25f))
    ) {
      Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SoftSkyBlue.copy(alpha = 0.25f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "🎙", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Whisper Corner Sanctuary",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = QuietCharcoal
          )
          Text(
            text = "A private, immersive voice reflection space. Speak freely, be truly heard, and secure your emotions.",
            fontFamily = NotoSansDevanagariFamily,
            color = QuietCharcoal.copy(alpha = 0.8f),
            fontSize = 12.sp,
            lineHeight = 16.sp
          )
        }
        Icon(
          imageVector = Icons.Default.KeyboardArrowRight,
          contentDescription = "Navigate to companion voice room",
          tint = QuietCharcoal
        )
      }
    }

    // EMOTIONAL JOURNEY HUB CARD CTA
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
        .clickable { onNavigateToMoodHub() }
        .testTag("open_mood_hub_card"),
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = SageGlow.copy(alpha = 0.4f)),
      border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.2f))
    ) {
      Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SageGlow.copy(alpha = 0.8f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "📊", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Your Emotional Journey",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = QuietCharcoal
          )
          Text(
            text = "Explore mood calendar, emotional weather, pattern discovery, and companion metrics.",
            fontFamily = NotoSansDevanagariFamily,
            color = QuietCharcoal.copy(alpha = 0.8f),
            fontSize = 12.sp,
            lineHeight = 16.sp
          )
        }
        Icon(
          imageVector = Icons.Default.KeyboardArrowRight,
          contentDescription = "Navigate to emotional journey mood tracking hub",
          tint = QuietCharcoal
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // EMOTIONAL CHECK-IN SECTION
    Text(
      text = "How does your soul feel right now?",
      fontFamily = PoppinsFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      color = QuietCharcoal,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // Scrollable Emotion Pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      feelingsList.forEach { (feeling, color) ->
        val isSelected = userFeeling == feeling
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) QuietCharcoal else color)
            .clickable {
              userFeeling = feeling
              writtenThoughts = ""
              reflectionOutput = ""
            }
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("feeling_pill_${feeling.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = feeling,
            fontFamily = PoppinsFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else QuietCharcoal
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // SOUL RESPONSE / EMPATHETIC CARD
    AnimatedVisibility(
      visible = userFeeling.isNotEmpty(),
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp)
          .testTag("empathetic_reflection_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = PureWhite
        ),
        elevation = CardDefaults.cardElevation(
          defaultElevation = 3.dp
        )
      ) {
        Column(
          modifier = Modifier.padding(24.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SageGreen.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Empathetic Echo",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = QuietCharcoal
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          if (isGeneratingReflection) {
            LinearProgressIndicator(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp)),
              color = SoftLavender,
              trackColor = LavenderGlow
            )
          } else {
            Text(
              text = reflectionOutput,
              style = MaterialTheme.typography.bodyLarge.copy(
                color = QuietCharcoal,
                fontSize = 15.sp,
                lineHeight = 22.sp
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // JOURNAL UNBURDEN BOX
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = PureWhite
      ),
      elevation = CardDefaults.cardElevation(
        defaultElevation = 2.dp
      )
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(
          text = "Unburden Your Heart",
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = QuietCharcoal
        )
        Text(
          text = "Type out anything weighing you down. This page doesn't save items; they gently fade away as you release them.",
          fontFamily = NotoSansDevanagariFamily,
          fontSize = 13.sp,
          color = SoftSlate,
          lineHeight = 18.sp,
          modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        OutlinedTextField(
          value = writtenThoughts,
          onValueChange = { writtenThoughts = it },
          placeholder = {
            Text(
              text = "I am holding...",
              fontFamily = NotoSansDevanagariFamily,
              color = SoftSlate.copy(alpha = 0.7f),
              fontSize = 14.sp
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .testTag("unburden_text_field"),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SoftLavender,
            unfocusedBorderColor = SoftSlate.copy(alpha = 0.3f),
            focusedContainerColor = CalmingBackground,
            unfocusedContainerColor = CalmingBackground
          )
        )

        if (writtenThoughts.isNotEmpty()) {
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = {
              writtenThoughts = ""
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = SoftLavender
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .align(Alignment.End)
              .testTag("release_button")
          ) {
            Text(
              text = "Let It Go",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.SemiBold,
              color = QuietCharcoal
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // DAILY REFLECTION EXERCISE
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = SoftSkyBlue.copy(alpha = 0.2f)
      ),
      elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp
      )
    ) {
      Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(PureWhite),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = SoftSkyBlue,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "3-Minute Breathing Wave",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            color = QuietCharcoal,
            fontSize = 15.sp
          )
          Text(
            text = "Follow the soft, moving dots to sync your respiration.",
            fontFamily = NotoSansDevanagariFamily,
            color = QuietCharcoal.copy(alpha = 0.8f),
            fontSize = 13.sp,
            lineHeight = 17.sp
          )
        }
      }
    }
  }
}
