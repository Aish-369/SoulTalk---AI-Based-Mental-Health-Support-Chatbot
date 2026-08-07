package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppContainer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class FirstMoodOption(
  val label: String,
  val icon: String,
  val moodKey: String, // stressed, sad, happy, etc
  val accentColor: Color,
  val backgroundGlow: Color,
  val weatherName: String,
  val weatherDesc: String,
  val speechMessage: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoulTalkFirstMoodCheckInScreen(onCheckInCompleted: () -> Unit) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val hapticFeedback = LocalHapticFeedback.current

  // Container repositories
  val companionRepo = remember { AppContainer.getRepository(context) }
  val localUser by companionRepo.userFlow.collectAsState(initial = null)
  val companionName = localUser?.companion_name ?: "Mochi"
  val companionType = localUser?.companion_type ?: "mochi_cat"

  // Mood options matching API payloads and styles
  val moodOptions = listOf(
    FirstMoodOption(
      label = "Happy",
      icon = "😊",
      moodKey = "happy",
      accentColor = SageGreen,
      backgroundGlow = SageGlow,
      weatherName = "☀️ Sunny Mind",
      weatherDesc = "Your skies are beautifully open and clear! A bright wave of energy is warming up your path. Let's bottle this positive moment inside your sanctuary profile.",
      speechMessage = "You're glowing! 😊 It sounds like today is beautiful. What's adding sunshine to your day?"
    ),
    FirstMoodOption(
      label = "Calm",
      icon = "😌",
      moodKey = "calm",
      accentColor = SoftSkyBlue,
      backgroundGlow = SkyGlow,
      weatherName = "🌟 Flourishing",
      weatherDesc = "A serene tranquility has settled of your soul. Beautifully centered, relaxed, and clear. This is a gorgeous baseline to expand your creative potential.",
      speechMessage = "A peaceful mind is such a gift. 😌 Tell me about what's keeping you relaxed and centered?"
    ),
    FirstMoodOption(
      label = "Neutral",
      icon = "😐",
      moodKey = "neutral",
      accentColor = SoftLavender,
      backgroundGlow = LavenderGlow,
      weatherName = "🌈 Recovery Mode",
      weatherDesc = "Resting gently in the soft neutral ground. No storm, no hyper sunshine, just simple being. A perfect soft place to pause and recuperate.",
      speechMessage = "Gently resting in the present moment is a lovely place to be. 😐 Is there anything you'd like to share?"
    ),
    FirstMoodOption(
      label = "Sad",
      icon = "😔",
      moodKey = "sad",
      accentColor = Color(0xFF90CAF9),
      backgroundGlow = Color(0xFFE3F2FD),
      weatherName = "🌧️ Emotional Rain",
      weatherDesc = "A cool, quiet precipitation has arrived. Tears are clean rain that irrigates the soul for future gardens. Let's allow the drops to fall safely.",
      speechMessage = "I'm right here with you. 😔 Please know that your tears are clean rain. What's weighing on your mind?"
    ),
    FirstMoodOption(
      label = "Stressed",
      icon = "😣",
      moodKey = "stressed",
      accentColor = Color(0xFFFFB74D),
      backgroundGlow = Color(0xFFFFF3E0),
      weatherName = "⛅ Cloudy Day",
      weatherDesc = "Heavy fog and cloud covers have rolled in, signaling exhaustion or mental load. We don't have to carry it all. Let's untangle this heavy weight together.",
      speechMessage = "Deep breath. We can take this slow together. 😣 Let's untangle whatever is making you overwhelmed."
    ),
    FirstMoodOption(
      label = "Anxious",
      icon = "😟",
      moodKey = "anxious",
      accentColor = Color(0xFFF48FB1),
      backgroundGlow = Color(0xFFFCE4EC),
      weatherName = "⛈️ Stormy Moment",
      weatherDesc = "Swirling electric winds and elevated heartbeat are sparking inside your chest. I hold solid, safe ground. Let's release the sparks safely into words.",
      speechMessage = "I am holding solid, safe space for you. 😟 Let's write it down to let it go. What makes you worried?"
    )
  )

  var selectedMood by remember { mutableStateOf<FirstMoodOption?>(null) }
  var journalNotes by remember { mutableStateOf("") }
  
  // Transition states
  var isSyncingMoodToServer by remember { mutableStateOf(false) }
  var computedWeatherResult by remember { mutableStateOf("") }
  var isShowingResultOverlay by remember { mutableStateOf(false) }

  // Interactive local AI feedback preview on typing
  val liveSupportiveMessage = remember(journalNotes) {
    if (journalNotes.trim().isEmpty()) "" else {
      val text = journalNotes.lowercase()
      when {
        text.contains("exam") || text.contains("test") || text.contains("work") || text.contains("study") || text.contains("pressure") || text.contains("grade") || text.contains("college") || text.contains("deadline") -> {
          "It sounds like you're carrying major academic or work pressure. We can break this giant mountain into small pebbles, together."
        }
        text.contains("sad") || text.contains("lonely") || text.contains("cry") || text.contains("broke") || text.contains("hurt") || text.contains("sick") || text.contains("lose") || text.contains("miss") -> {
          "Your feeling is completely valid. It's safe to release that weight here. I am holding space for you."
        }
        text.contains("happy") || text.contains("excited") || text.contains("good") || text.contains("fun") || text.contains("accomplished") || text.contains("love") || text.contains("friend") || text.contains("great") -> {
          "Your heart is radiant! Recording these beautiful, positive ripples keeps you grounded in joy."
        }
        text.contains("fight") || text.contains("mad") || text.contains("angry") || text.contains("annoyed") || text.contains("hate") || text.contains("tired") -> {
          "Frustration is natural and clean. Let's take a deep breath to dissolve the sharp edges."
        }
        else -> {
          "I am listening with an open heart. Translate your feelings into words to let them flow safely."
        }
      }
    }
  }

  // Continuous background cloud rotation phase
  val clock = rememberInfiniteTransition(label = "weather_clock")
  val timePhase by clock.animateFloat(
    initialValue = 0f,
    targetValue = 1500f,
    animationSpec = infiniteRepeatable(
      animation = tween(45000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "climate_loop"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CalmingBackground)
      .testTag("first_mood_checkin_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(28.dp))

      // COMPANION VIEW
      Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
      ) {
        AnimatedCompanion(
          companionType = companionType,
          companionName = companionName,
          mood = selectedMood?.moodKey,
          notes = journalNotes
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // SPEECH BUBBLE COMPONENT
      AnimatedContent(
        targetState = selectedMood,
        transitionSpec = {
          fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "speech_bubble"
      ) { activeMood ->
        val bubbleText = activeMood?.speechMessage ?: "Hi 👋 I'm excited to start this journey with you. Before we begin, I'd love to know how you're feeling today."
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
            .background(PureWhite, RoundedCornerShape(20.dp))
            .border(1.dp, SageGreen.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(18.dp)
        ) {
          Text(
            text = bubbleText,
            fontFamily = PoppinsFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = QuietCharcoal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // TITLE & MAIN SUBTITLE QUESTION
      Text(
        text = "Welcome To SoulTalk",
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = QuietCharcoal,
        textAlign = TextAlign.Center
      )

      Text(
        text = "Let's create your safe space.",
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = SoftSlate,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // MOOD SELECTION 2x3 PASTEL GRID
      Text(
        text = "SELECT YOUR CURRENT MOOD",
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
        color = SoftSlate,
        modifier = Modifier.align(Alignment.Start)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Render 2 mood items per Row mapping to all 6 requirements
        val chunkedMoods = moodOptions.chunked(2)
        chunkedMoods.forEach { rowMoods ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            rowMoods.forEach { mood ->
              val isSelected = selectedMood?.label == mood.label
              val glowColor: Color = if (isSelected) mood.accentColor else Color.Transparent

              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(60.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isSelected) mood.backgroundGlow else PureWhite)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) mood.accentColor else SoftSlate.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                  )
                  .clickable {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedMood = mood
                  }
                  .shadow(
                    elevation = if (isSelected) 6.dp else 1.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = glowColor,
                    spotColor = glowColor
                  )
                  .testTag("onboarding_mood_${mood.moodKey}"),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                  Text(text = mood.icon, fontSize = 20.sp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = mood.label,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = QuietCharcoal
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // OPTIONAL JOURNAL INPUT SECTION (FADE-IN IF MOOD SELECTED)
      AnimatedVisibility(
        visible = selectedMood != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "WOULD YOU LIKE TO TELL ME A LITTLE MORE? (OPTIONAL)",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            color = SoftSlate
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = journalNotes,
            onValueChange = { journalNotes = it },
            placeholder = {
              Text(
                text = "What's on your mind today? Write anything...",
                fontFamily = PoppinsFamily,
                fontSize = 13.5.sp,
                color = SoftSlate
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp)
              .testTag("mood_checkin_notes_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = PureWhite,
              focusedContainerColor = PureWhite,
              unfocusedBorderColor = SoftSlate.copy(alpha = 0.5f),
              focusedBorderColor = selectedMood?.accentColor ?: SageGreen
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
          )

          // AI EMOTION DETECTOR PREVIEW RESPONSE
          AnimatedVisibility(
            visible = liveSupportiveMessage.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -15 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -15 }) + fadeOut()
          ) {
            Box(
              modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .background(
                  color = (selectedMood?.backgroundGlow ?: SageGlow).copy(alpha = 0.5f),
                  shape = RoundedCornerShape(12.dp)
                )
                .border(
                  width = 1.dp,
                  color = (selectedMood?.accentColor ?: SageGreen).copy(alpha = 0.15f),
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛡️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = liveSupportiveMessage,
                  fontFamily = PoppinsFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = QuietCharcoal,
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(35.dp))

      // STATIC CARING PRIVACY FOOTER CARD
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.03f))
          .background(PureWhite, RoundedCornerShape(16.dp))
          .padding(16.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🔒", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Your conversations stay private.",
              fontFamily = PoppinsFamily,
              fontSize = 12.sp,
              color = SoftSlate
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🔒", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Your emotional data is secure.",
              fontFamily = PoppinsFamily,
              fontSize = 12.sp,
              color = SoftSlate
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🔒", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "You control your information.",
              fontFamily = PoppinsFamily,
              fontSize = 12.sp,
              color = SoftSlate
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(35.dp))

      // CONTINUE BUTTON
      ElevatedButton(
        onClick = {
          if (selectedMood == null) return@ElevatedButton
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
          isSyncingMoodToServer = true

          coroutineScope.launch {
            val apiResponse = companionRepo.logMood(selectedMood!!.moodKey, journalNotes)
            computedWeatherResult = apiResponse.weather
            isSyncingMoodToServer = false
            isShowingResultOverlay = true

            delay(3500)
            onCheckInCompleted()
          }
        },
        enabled = selectedMood != null && !isSyncingMoodToServer,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("continue_journey_button"),
        colors = ButtonDefaults.elevatedButtonColors(
          containerColor = selectedMood?.accentColor ?: SageGreen,
          contentColor = PureWhite,
          disabledContainerColor = SoftSlate.copy(alpha = 0.25f),
          disabledContentColor = SoftSlate.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
          defaultElevation = 4.dp,
          pressedElevation = 8.dp,
          disabledElevation = 0.dp
        ),
        shape = RoundedCornerShape(28.dp)
      ) {
        val buttonText = if (isSyncingMoodToServer) "Analyzing..." else "Continue Journey"
        Text(
          text = buttonText,
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 16.sp,
          letterSpacing = 0.5.sp
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }

    // FULL SCREEN RESULTS ATMOSPHERE WEATHER REVEAL TRANSITION
    AnimatedVisibility(
      visible = isShowingResultOverlay,
      enter = fadeIn(animationSpec = tween(600)),
      exit = fadeOut(animationSpec = tween(500))
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(selectedMood?.backgroundGlow ?: SageGlow)
          .testTag("emotional_weather_result_overlay"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          // Dynamic Companion Jump Jiggle reaction
          val infiniteTransitionResult = rememberInfiniteTransition(label = "res_jiggle")
          val jumpY by infiniteTransitionResult.animateFloat(
            initialValue = -12f,
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
              animation = tween(400, easing = EaseInOutBounce),
              repeatMode = RepeatMode.Reverse
            ),
            label = "jumping"
          )

          Box(
            modifier = Modifier
              .offset(y = jumpY.dp)
              .scale(1.15f)
          ) {
            AnimatedCompanion(
              companionType = companionType,
              companionName = companionName,
              mood = "happy", // Companion is celebrating/jumping of success!
              notes = ""
            )
          }

          Spacer(modifier = Modifier.height(28.dp))

          // Weather Label Card Bubble
          Box(
            modifier = Modifier
              .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = selectedMood?.accentColor ?: SageGreen)
              .background(PureWhite, RoundedCornerShape(24.dp))
              .border(1.9.dp, selectedMood?.accentColor ?: SageGreen, RoundedCornerShape(24.dp))
              .padding(horizontal = 24.dp, vertical = 16.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "YOUR EMOTIONAL ATMOSPHERE",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = selectedMood?.accentColor ?: SageGreen
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = computedWeatherResult,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = QuietCharcoal
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Supportive weather quote
          Text(
            text = "“Every journey starts with understanding how we feel.”",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = QuietCharcoal,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Setting up your home dashboard space. Hold tight...",
            fontFamily = PoppinsFamily,
            fontSize = 13.sp,
            color = SoftSlate,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(28.dp))

          // Canvas visuals background details
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
          ) {
            ProceduralWeatherRadarCanvas(
              moodType = selectedMood?.label ?: "Happy",
              time = timePhase,
              accentColor = selectedMood?.accentColor ?: SageGreen
            )
          }
        }
      }
    }
  }
}

/**
 * Animated Companion Core component. Renders adaptively depending on companionType.
 */
@Composable
fun AnimatedCompanion(
  companionType: String,
  companionName: String,
  mood: String?,
  notes: String
) {
  val infiniteTransition = rememberInfiniteTransition(label = "companion_anim")

  // Floating ambient offset
  val floatOffset by infiniteTransition.animateFloat(
    initialValue = -6f,
    targetValue = 6f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "floating"
  )

  // Breathing size scale
  val breathScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathing"
  )

  // Blink logic parameter
  var isBlinking by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    while (true) {
      delay((3000..6000).random().toLong())
      isBlinking = true
      delay(150)
      isBlinking = false
    }
  }

  // Draw Companion Core Canvas
  Box(
    modifier = Modifier
      .size(130.dp)
      .offset(y = floatOffset.dp)
      .scale(breathScale)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = w / 2f
      val cy = h / 2f
      val density = 3f // reference modifier

      // Determine colors based on companionType configuration
      val faceColor = when (companionType) {
        "mochi_cat" -> Color(0xFFF5F5F7)
        "star_rabbit" -> Color(0xFFFFF9F5)
        "cozy_bear" -> Color(0xFFECE0D1)
        else -> Color(0xFFF5F5F7) // fallback cat
      }

      val earInnerColor = Color(0xFFFFCDD2)
      val cheekAlpha = if (mood == "happy" || mood == "calm") 0.6f else 0.2f
      val eyeYOffset = if (mood == "sad") 2.5f else 0f

      // EAR SHAPES DRAW
      if (companionType == "cozy_bear") {
        // Rounded bear ears
        drawCircle(
          color = faceColor,
          radius = 18f,
          center = Offset(cx - 30f, cy - 35f)
        )
        drawCircle(
          color = earInnerColor,
          radius = 10f,
          center = Offset(cx - 30f, cy - 35f)
        )
        drawCircle(
          color = faceColor,
          radius = 18f,
          center = Offset(cx + 30f, cy - 35f)
        )
        drawCircle(
          color = earInnerColor,
          radius = 10f,
          center = Offset(cx + 30f, cy - 35f)
        )
      } else if (companionType == "star_rabbit") {
        // Tall bunny ears
        val leftEar = Path().apply {
          moveTo(cx - 24f, cy - 30f)
          quadraticBezierTo(cx - 35f, cy - 75f, cx - 25f, cy - 80f)
          quadraticBezierTo(cx - 10f, cy - 75f, cx - 8f, cy - 30f)
          close()
        }
        val rightEar = Path().apply {
          moveTo(cx + 8f, cy - 30f)
          quadraticBezierTo(cx + 10f, cy - 75f, cx + 25f, cy - 80f)
          quadraticBezierTo(cx + 35f, cy - 75f, cx + 24f, cy - 30f)
          close()
        }
        drawPath(leftEar, faceColor)
        drawPath(rightEar, faceColor)
      } else {
        // Default: Mochi Cat pointed ears
        val leftEar = Path().apply {
          moveTo(cx - 32f, cy - 25f)
          lineTo(cx - 48f, cy - 65f)
          lineTo(cx - 12f, cy - 35f)
          close()
        }
        val rightEar = Path().apply {
          moveTo(cx + 32f, cy - 25f)
          lineTo(cx + 48f, cy - 65f)
          lineTo(cx + 12f, cy - 35f)
          close()
        }
        drawPath(leftEar, faceColor)
        drawPath(rightEar, faceColor)

        val leftEarIn = Path().apply {
          moveTo(cx - 28f, cy - 27f)
          lineTo(cx - 40f, cy - 55f)
          lineTo(cx - 16f, cy - 34f)
          close()
        }
        val rightEarIn = Path().apply {
          moveTo(cx + 28f, cy - 27f)
          lineTo(cx + 40f, cy - 55f)
          lineTo(cx + 16f, cy - 34f)
          close()
        }
        drawPath(leftEarIn, earInnerColor)
        drawPath(rightEarIn, earInnerColor)
      }

      // FACE MAIN MASS
      drawCircle(
        color = faceColor,
        radius = 45f,
        center = Offset(cx, cy + 5f)
      )

      // CHEEKS
      drawCircle(
        color = Color(0xFFFFB2B2).copy(alpha = cheekAlpha),
        radius = 8f,
        center = Offset(cx - 25f, cy + 15f)
      )
      drawCircle(
        color = Color(0xFFFFB2B2).copy(alpha = cheekAlpha),
        radius = 8f,
        center = Offset(cx + 25f, cy + 15f)
      )

      // BLINKING & EYE GRAPHICS
      if (isBlinking) {
        drawLine(
          color = Color(0xFF333333),
          start = Offset(cx - 22f, cy + 5f + eyeYOffset),
          end = Offset(cx - 12f, cy + 5f + eyeYOffset),
          strokeWidth = 3f
        )
        drawLine(
          color = Color(0xFF333333),
          start = Offset(cx + 12f, cy + 5f + eyeYOffset),
          end = Offset(cx + 22f, cy + 5f + eyeYOffset),
          strokeWidth = 3f
        )
      } else {
        // Render pretty circles with internal white sparkle reflections
        drawCircle(
          color = Color(0xFF333333),
          radius = 5.5f,
          center = Offset(cx - 17f, cy + 4f + eyeYOffset)
        )
        drawCircle(
          color = Color(0xFF333333),
          radius = 5.5f,
          center = Offset(cx + 17f, cy + 4f + eyeYOffset)
        )

        drawCircle(
          color = Color.White,
          radius = 1.8f,
          center = Offset(cx - 18.5f, cy + 2.5f + eyeYOffset)
        )
        drawCircle(
          color = Color.White,
          radius = 1.8f,
          center = Offset(cx + 15.5f, cy + 2.5f + eyeYOffset)
        )
      }

      // NOSE / EMOTIONAL MOUTH CHANNELS
      if (mood == "happy" || mood == "calm") {
        val mouth = Path().apply {
          moveTo(cx - 5f, cy + 11f)
          quadraticBezierTo(cx, cy + 16f, cx + 5f, cy + 11f)
        }
        drawPath(mouth, Color(0xFF424242), style = Stroke(2.5f))
      } else if (mood == "sad" || mood == "anxious") {
        val mouth = Path().apply {
          moveTo(cx - 5f, cy + 14f)
          quadraticBezierTo(cx, cy + 9f, cx + 5f, cy + 14f)
        }
        drawPath(mouth, Color(0xFF424242), style = Stroke(2.5f))
      } else {
        // default cute smiling dash
        drawLine(
          color = Color(0xFF424242),
          start = Offset(cx - 3f, cy + 12f),
          end = Offset(cx + 3f, cy + 12f),
          strokeWidth = 2.5f
        )
      }
    }
  }
}

/**
 * Custom procedural vector Canvas generating dynamic weather depending on selected feeling.
 */
@Composable
fun ProceduralWeatherRadarCanvas(
  moodType: String,
  time: Float,
  accentColor: Color
) {
  val density = LocalDensity.current.density
  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    when (moodType) {
      "Happy" -> {
        // Sunny rotation
        val pulse = 1f + 0.08f * sin(time * 0.12f)
        val shadowRadius = 35f * density * pulse
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(cx, cy),
            radius = shadowRadius * 2f
          )
        )
        drawCircle(
          color = accentColor,
          radius = 24f * density,
          center = Offset(cx, cy)
        )
        // Little circular glowing ray sparks
        for (i in 0 until 8) {
          val angle = (i * Math.PI / 4f) + (time * 0.015f)
          val sx = cx + (36f * density * pulse) * cos(angle).toFloat()
          val sy = cy + (36f * density * pulse) * sin(angle).toFloat()
          drawCircle(
            color = accentColor.copy(alpha = 0.8f),
            radius = 3.5f * density,
            center = Offset(sx, sy)
          )
        }
      }

      "Calm" -> {
        // Drifting aura pulse
        val auraSize = 30f * density + sin(time * 0.08f) * 8f * density
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(cx, cy),
            radius = auraSize * 2.5f
          )
        )
        drawCircle(
          color = accentColor.copy(alpha = 0.2f),
          radius = auraSize,
          center = Offset(cx, cy)
        )
      }

      "Neutral" -> {
        // Misty foggy cloud lines drifting left and right
        val driftOffset = sin(time * 0.05f) * 25f * density
        for (i in 0 until 3) {
          val mistY = cy - 20f * density + (i * 18f * density)
          val len = 80f * density + cos(time * 0.08f + i) * 12f * density
          drawLine(
            color = accentColor.copy(alpha = 0.3f),
            start = Offset(cx - len / 2f + driftOffset, mistY),
            end = Offset(cx + len / 2f + driftOffset, mistY),
            strokeWidth = 3.5f * density,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
          )
        }
      }

      "Sad" -> {
        // Falling raindrops
        val dropCount = 5
        for (i in 0 until dropCount) {
          val rx = cx - 50f * density + (i * 25f * density)
          val ry = ((time * 3f + i * 35f) % (h + 15f * density)) - 10f * density
          if (ry < h - 10f * density) {
            drawLine(
              color = accentColor.copy(alpha = 0.7f),
              start = Offset(rx, ry),
              end = Offset(rx, ry + 10f * density),
              strokeWidth = 2f * density
            )
          }
        }
      }

      "Stressed" -> {
        // Fluffy heavy overlay clouds
        val cloudL = cx - 20f * density + sin(time * 0.04f) * 15f * density
        val cloudR = cx + 20f * density - cos(time * 0.03f) * 10f * density
        drawCircle(
          color = accentColor.copy(alpha = 0.25f),
          radius = 28f * density,
          center = Offset(cloudL, cy - 5f * density)
        )
        drawCircle(
          color = accentColor.copy(alpha = 0.35f),
          radius = 32f * density,
          center = Offset(cloudR, cy + 4f * density)
        )
      }

      else -> {
        // Lightning spark bolt for Anxiety
        val boltPath = Path().apply {
          moveTo(cx - 3f * density, cy - 30f * density)
          lineTo(cx - 15f * density, cy)
          lineTo(cx - 1f * density, cy)
          lineTo(cx - 8f * density, cy + 30f * density)
          lineTo(cx + 8f * density, cy - 1f * density)
          lineTo(cx - 1f * density, cy - 1f * density)
          close()
        }
        drawPath(path = boltPath, color = accentColor)
      }
    }
  }
}
