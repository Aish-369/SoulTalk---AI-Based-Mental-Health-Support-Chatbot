package com.example.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppContainer
import com.example.data.api.*
import com.example.ui.theme.*
import com.example.ui.components.WolfieCharacter
import com.example.ui.components.WolfieEmotion
import com.example.ui.components.WolfieSize
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoulTalkCompanionChatScreen(
  onBackClicked: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val companionRepo = remember { AppContainer.getRepository(context) }

  // Chat conversation memory state (initialized from DB or fallback)
  var chatMessages by remember { mutableStateOf<List<ChatMessageDto>>(emptyList()) }
  var isLoadingHistory by remember { mutableStateOf(true) }

  // Character metadata custom state (Wolfie is the sole companion)
  var companionName by remember { mutableStateOf("Wolfie") }
  var companionType by remember { mutableStateOf("wolfie") }
  var personalityType by remember { mutableStateOf("Emotionally Intelligent, Wise, Compassionate") }
  var emotionalTrends by remember { mutableStateOf<List<String>>(listOf("neutral")) }

  // UI state variables
  var writtenMessage by remember { mutableStateOf("") }
  var isThinking by remember { mutableStateOf(false) }
  var detectedEmotionState by remember { mutableStateOf("neutral") }
  var wolfieCurrentEmotion by remember { mutableStateOf(WolfieEmotion.LISTENING) }
  
  // Update Wolfie emotion based on detected emotion
  LaunchedEffect(detectedEmotionState) {
    wolfieCurrentEmotion = when (detectedEmotionState) {
      "happy" -> WolfieEmotion.HAPPY
      "sad" -> WolfieEmotion.SUPPORTIVE
      "anxious" -> WolfieEmotion.LISTENING
      "stressed" -> WolfieEmotion.LISTENING
      "calm" -> WolfieEmotion.MEDITATING
      else -> WolfieEmotion.LISTENING
    }
  }
  
  // Interactive Overlays
  var showBreatheOverlay by remember { mutableStateOf(false) }
  var showJournalOverlay by remember { mutableStateOf(false) }
  var journalSuccessMsg by remember { mutableStateOf("") }

  // Speech systems
  var tts by remember { mutableStateOf<TextToSpeech?>(null) }
  var isSpeechActive by remember { mutableStateOf(false) }
  var isListeningState by remember { mutableStateOf(false) }
  var listeningProgress by remember { mutableStateOf(0f) }

  // Automatic scrolling handle
  val lazyListState = rememberLazyListState()

  // Initialize Kotlin Native Voice Text To Speech Engine
  LaunchedEffect(Unit) {
    tts = TextToSpeech(context) { status ->
      if (status != TextToSpeech.ERROR) {
        tts?.language = Locale.getDefault()
        tts?.setSpeechRate(0.82f) // Extra calming slowly paced cozy speech voice reply
        tts?.setPitch(1.08f)      // Soft cute companion friendly pitch
      }
    }

    // Load Chat Context (character personality & preferences)
    try {
      val ctx = companionRepo.getChatContext()
      companionName = ctx.companion_name
      companionType = ctx.companion_type
      personalityType = ctx.personality_type
      emotionalTrends = ctx.recent_emotional_trends
    } catch (e: Exception) {
      Log.w("ChatScreen", "Context load fallback: ${e.localizedMessage}")
    }

    // Load message history from repository
    try {
      val history = companionRepo.getChatHistory()
      chatMessages = history
      if (history.isNotEmpty()) {
        detectedEmotionState = history.last().emotion ?: "neutral"
      }
    } catch (e: Exception) {
      Log.w("ChatScreen", "History load fallback: ${e.localizedMessage}")
    } finally {
      isLoadingHistory = false
    }
  }

  // Auto Scroll down to latest turn
  LaunchedEffect(chatMessages.size, isThinking) {
    if (chatMessages.isNotEmpty()) {
      delay(200)
      lazyListState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  // Trigger Android speech synthesis voice reply
  fun speakText(text: String) {
    tts?.stop()
    isSpeechActive = true
    val cleanText = text.replace(Regex("[^a-zA-Z0-9\\s.,!?']"), "") // Strip complex animal emoji noise
    tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "companion_voice")
  }

  // Stop voice reply
  fun stopVoice() {
    tts?.stop()
    isSpeechActive = false
  }

  // Clean-up voice engine
  DisposableEffect(Unit) {
    onDispose {
      tts?.stop()
      tts?.shutdown()
    }
  }

  // Trigger user message submission
  fun submitMessage(msgText: String) {
    val cleanMsg = msgText.trim()
    if (cleanMsg.isEmpty()) return

    // 1. Instantly append user bubble local-first (60fps reactive feedback)
    val tempUserMsg = ChatMessageDto(
      id = (1000..9999).random(),
      role = "user",
      message = cleanMsg,
      emotion = "neutral",
      created_at = System.currentTimeMillis()
    )
    chatMessages = chatMessages + tempUserMsg
    writtenMessage = ""
    isThinking = true

    // 2. Perform background dispatch
    coroutineScope.launch {
      try {
        val sendResponse = companionRepo.sendChatMessage(cleanMsg)
        
        // 3. Append helper companion reply inside the state
        val companionReply = ChatMessageDto(
          id = sendResponse.message_id,
          role = "companion",
          message = sendResponse.reply,
          emotion = sendResponse.emotion,
          created_at = System.currentTimeMillis()
        )
        chatMessages = chatMessages + companionReply
        detectedEmotionState = sendResponse.emotion

        // 4. Automatically trigger warm text-to-speech reading response
        speakText(sendResponse.reply)

      } catch (err: Exception) {
        Log.e("ChatScreen", "Send message failed: ${err.localizedMessage}")
      } finally {
        isThinking = false
      }
    }
  }

  // Pulse voice systems simulation loop
  LaunchedEffect(isListeningState) {
    if (isListeningState) {
      var direction = 1f
      while (isListeningState) {
        listeningProgress += direction * 0.08f
        if (listeningProgress >= 1f) {
          listeningProgress = 1f
          direction = -1f
        } else if (listeningProgress <= 0.1f) {
          listeningProgress = 0.1f
          direction = 1f
        }
        delay(40)
      }
    } else {
      listeningProgress = 0f
    }
  }

  // Main UI Canvas Layout Scaffolding
  Scaffold(
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PureWhite,
          titleContentColor = QuietCharcoal,
          scrolledContainerColor = PureWhite
        ),
        navigationIcon = {
          IconButton(
            onClick = {
              stopVoice()
              onBackClicked()
            },
            modifier = Modifier.testTag("chat_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Default.ArrowBack,
              contentDescription = "Return to Dashboard",
              tint = SageGreen
            )
          }
        },
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              modifier = Modifier.size(40.dp),
              shape = CircleShape,
              color = SoftLavender.copy(alpha = 0.15f)
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
              ) {
                Text(text = "🐺", fontSize = 20.sp)
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = companionName,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = QuietCharcoal
              )
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val listeningPulse = rememberInfiniteTransition("listening").animateFloat(
                  initialValue = 0.3f,
                  targetValue = 1f,
                  animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                  ),
                  label = "pulse"
                )
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(SageGreen.copy(alpha = listeningPulse.value))
                )
                Text(
                  text = if (isThinking) "Thinking..." else "Listening",
                  fontSize = 10.sp,
                  color = SageGreen,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        },
        actions = {
          if (isSpeechActive) {
            IconButton(
              onClick = { stopVoice() },
              modifier = Modifier.testTag("mute_tts_button")
            ) {
              Icon(Icons.Default.VolumeOff, "Mute", tint = SageGreen, modifier = Modifier.size(20.dp))
            }
          }
        },
        modifier = Modifier.shadow(2.dp)
      )
    },
    modifier = Modifier
      .fillMaxSize()
      .background(CalmingBackground)
      .testTag("soul_companion_chat_root")
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(
          Brush.verticalGradient(
            colors = listOf(PureWhite, CalmingBackground)
          )
        )
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        
        // ──────────────────────────────────────────────
        // COMPANION AREA - ALWAYS VISIBLE HEAD
        // ──────────────────────────────────────────────
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(PureWhite)
            .border(
              width = 1.dp,
              color = CalmingBackground,
              shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            CompanionInteractiveGraphicsCanvas(
              companionType = companionType,
              emotion = detectedEmotionState,
              isThinking = isThinking,
              modifier = Modifier
                .size(130.dp)
                .testTag("live_3d_companion_area")
            )
            
            // Dynamic emotion label badge
            Box(
              modifier = Modifier
                .offset(y = (-4).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  when(detectedEmotionState) {
                    "happy", "excited" -> SageGreen.copy(alpha = 0.12f)
                    "sad", "lonely" -> SoftSkyBlue.copy(alpha = 0.12f)
                    "stressed", "anxious" -> SoftLavender.copy(alpha = 0.15f)
                    else -> SoftSlate.copy(alpha = 0.08f)
                  }
                )
                .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${companionName} feels ${detectedEmotionState}",
                fontFamily = PoppinsFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = when(detectedEmotionState) {
                  "happy", "excited" -> SageGreen
                  "sad", "lonely" -> SoftSkyBlue
                  "stressed", "anxious" -> SoftLavender
                  else -> QuietCharcoal
                }
              )
            }
          }
        }

        // ──────────────────────────────────────────────
        // CHAT CONVERSATION WRAPPER
        // ──────────────────────────────────────────────
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          if (isLoadingHistory) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(color = SoftLavender)
            }
          } else if (chatMessages.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Share anything weighing on your heart... I am sitting right here beside you. ❤️",
                fontFamily = PoppinsFamily,
                fontSize = 14.sp,
                color = SoftSlate,
                textAlign = TextAlign.Center
              )
            }
          } else {
            LazyColumn(
              state = lazyListState,
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(chatMessages) { chatMessage ->
                ChatBubbleRow(chatMessage, companionName)
              }
              
              if (isThinking) {
                item {
                  CompanionThinkingBubbleRow(companionName)
                }
              }
            }
          }
        }

        // ──────────────────────────────���────��──────────
        // QUICK ACTIONS CARDS
        // ──────────────────────────────────────────────
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Quick Action: Breathe Together
          Card(
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .clickable { showBreatheOverlay = true }
              .testTag("action_breathe"),
            colors = CardDefaults.cardColors(containerColor = SageGlow),
            shape = RoundedCornerShape(16.dp)
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Text("🌬", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "Breathe Together",
                  fontFamily = PoppinsFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = QuietCharcoal
                )
              }
            }
          }

          // Quick Action: Journal This
          Card(
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .clickable { showJournalOverlay = true }
              .testTag("action_journal"),
            colors = CardDefaults.cardColors(containerColor = LavenderGlow),
            shape = RoundedCornerShape(16.dp)
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Text("📔", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "Journal Thought",
                  fontFamily = PoppinsFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = QuietCharcoal
                )
              }
            }
          }
        }

        // INPUT CONTROLS SECTION
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
          shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
          colors = CardDefaults.cardColors(containerColor = PureWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = writtenMessage,
              onValueChange = { writtenMessage = it },
              placeholder = {
                Text(
                  text = "Share what's on your mind...",
                  fontFamily = NotoSansDevanagariFamily,
                  fontSize = 13.sp,
                  color = SoftSlate.copy(alpha = 0.6f)
                )
              },
              modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("companion_chat_input_field"),
              shape = RoundedCornerShape(26.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CalmingBackground,
                unfocusedContainerColor = CalmingBackground,
                focusedBorderColor = SoftLavender.copy(alpha = 0.7f),
                unfocusedBorderColor = DividerLight,
                focusedTextColor = QuietCharcoal,
                unfocusedTextColor = QuietCharcoal
              ),
              leadingIcon = {
                val currentSymbol = when {
                  writtenMessage.isEmpty() -> "😊"
                  writtenMessage.lowercase().contains("sad") || writtenMessage.lowercase().contains("blue") -> "😔"
                  writtenMessage.lowercase().contains("stress") || writtenMessage.lowercase().contains("busy") -> "😣"
                  else -> "😊"
                }
                Box(
                  modifier = Modifier.padding(start = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = currentSymbol, fontSize = 18.sp)
                }
              },
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyMedium
            )

            // Voice Button - Microphone system
            Card(
              modifier = Modifier
                .size(52.dp)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(),
                  enabled = true
                ) {
                  if (isListeningState) {
                    isListeningState = false
                    val simulatedSpeechTexts = listOf(
                      "I feel so stressed and overwhelmed about tomorrow",
                      "I am really happy and excited because of my friend",
                      "I have been feeling lonely and sad all day"
                    )
                    submitMessage(simulatedSpeechTexts.random())
                  } else {
                    stopVoice()
                    isListeningState = true
                  }
                }
                .testTag("chat_voice_button"),
              shape = CircleShape,
              colors = CardDefaults.cardColors(
                containerColor = if (isListeningState) SoftLavender else SageGreen.copy(alpha = 0.2f)
              ),
              elevation = CardDefaults.cardElevation(
                defaultElevation = if (isListeningState) 4.dp else 2.dp
              )
            ) {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                if (isListeningState) {
                  val scaleFactor = 1f + (listeningProgress * 0.2f)
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .scale(scaleFactor)
                      .clip(CircleShape)
                      .background(SoftLavender.copy(alpha = 0.3f))
                  )
                }
                Icon(
                  imageVector = if (isListeningState) Icons.Default.Stop else Icons.Default.Mic,
                  contentDescription = if (isListeningState) "Stop listening" else "Start listening",
                  tint = if (isListeningState) PureWhite else SageGreen,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            // Submit Button
            Card(
              modifier = Modifier
                .size(52.dp)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(),
                  enabled = writtenMessage.isNotEmpty()
                ) {
                  if (writtenMessage.isNotEmpty()) submitMessage(writtenMessage)
                }
                .testTag("companion_chat_send_button"),
              shape = CircleShape,
              colors = CardDefaults.cardColors(
                containerColor = if (writtenMessage.isNotEmpty()) SoftLavender else DisabledGray.copy(alpha = 0.2f)
              ),
              elevation = CardDefaults.cardElevation(
                defaultElevation = if (writtenMessage.isNotEmpty()) 4.dp else 0.dp
              )
            ) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                  imageVector = Icons.AutoMirrored.Default.Send,
                  contentDescription = "Send message",
                  tint = if (writtenMessage.isNotEmpty()) PureWhite else SoftSlate.copy(alpha = 0.5f),
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }
      }

      // Voice dictation listening prompt box overlay
      AnimatedVisibility(
        visible = isListeningState,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = Modifier.align(Alignment.BottomCenter)
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .offset(y = (-80).dp),
          colors = CardDefaults.cardColors(containerColor = PureWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
          shape = RoundedCornerShape(20.dp)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "🐱 Listening to your voice...",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = QuietCharcoal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Speak freely. Tap Mic icon again to finalize your thoughts.",
              fontFamily = NotoSansDevanagariFamily,
              fontSize = 12.sp,
              color = SoftSlate,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Simulated voice waves moving procedurally
              repeat(5) { i ->
                val waveHeight = (10 + (25 * listeningProgress)).dp
                Box(
                  modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(4.dp)
                    .height(waveHeight)
                    .clip(CircleShape)
                    .background(SoftLavender)
                )
              }
            }
          }
        }
      }

      // 🌬 BREATHETOGETHER FULL SCREEN OVERLAY IMPLEMENTATION
      AnimatedVisibility(
        visible = showBreatheOverlay,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(QuietCharcoal.copy(alpha = 0.95f))
            .clickable(enabled = true) { /* Consume taps */ },
          contentAlignment = Alignment.Center
        ) {
          var breathState by remember { mutableStateOf("Inhale") }
          var breathCircleScale by remember { mutableStateOf(1f) }

          // Calming circular respiration guide
          LaunchedEffect(showBreatheOverlay) {
            while (showBreatheOverlay) {
              breathState = "Inhale"
              // Expand circle slowly
              animate(
                initialValue = 1f,
                targetValue = 1.9f,
                animationSpec = tween(4000, easing = EaseInOutSine)
              ) { v, _ -> breathCircleScale = v }
              
              breathState = "Hold Ease"
              delay(4000)

              breathState = "Exhale"
              // Shrink circle slowly
              animate(
                initialValue = 1.9f,
                targetValue = 1.0f,
                animationSpec = tween(4000, easing = EaseInOutSine)
              ) { v, _ -> breathCircleScale = v }
              
              breathState = "Hold Ease"
              delay(4000)
            }
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Text(
              text = "Somatic Respiration Therapy",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = Color.White
            )
            Text(
              text = "Sync with ${companionName}'s custom floating wave",
              fontFamily = NotoSansDevanagariFamily,
              fontSize = 13.sp,
              color = SoftSlate,
              modifier = Modifier.padding(bottom = 40.dp)
            )

            // Animated breathing circle concentric rings
            Box(
              modifier = Modifier.size(240.dp),
              contentAlignment = Alignment.Center
            ) {
              // Outer rings
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .scale(breathCircleScale * 0.9f)
                  .clip(CircleShape)
                  .background(SageGreen.copy(alpha = 0.15f))
              )
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .scale(breathCircleScale * 0.75f)
                  .clip(CircleShape)
                  .background(SageGreen.copy(alpha = 0.25f))
              )
              
              // Core ring
              Box(
                modifier = Modifier
                  .size(110.dp)
                  .scale(breathCircleScale * 0.6f)
                  .clip(CircleShape)
                  .background(SageGreen)
              ) {
                Box(
                  modifier = Modifier.fillMaxSize(),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "🌬",
                    fontSize = 24.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
              text = breathState.uppercase(),
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp,
              color = SageGreen,
              letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(
              onClick = { showBreatheOverlay = false },
              colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
              shape = RoundedCornerShape(20.dp)
            ) {
              Text(
                "Return to Conversation",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                color = QuietCharcoal
              )
            }
          }
        }
      }

      // 📔 JOURNAL THIS THOUGHT OVERLAY IMPLEMENTATION
      AnimatedVisibility(
        visible = showJournalOverlay,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(QuietCharcoal.copy(alpha = 0.7f))
            .clickable(enabled = true) { /* Consume taps */ },
          contentAlignment = Alignment.Center
        ) {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(24.dp)
          ) {
            Column(
              modifier = Modifier.padding(24.dp)
            ) {
              Text(
                text = "Release to Journal Sanctuary 📔",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = QuietCharcoal
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Do you want to seal this conversation's current emotion as a permanent journal entry?",
                fontFamily = NotoSansDevanagariFamily,
                fontSize = 13.sp,
                color = SoftSlate,
                lineHeight = 18.sp
              )
              
              Spacer(modifier = Modifier.height(16.dp))

              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CalmingBackground),
                border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = "Mood logged: $detectedEmotionState",
                    fontFamily = PoppinsFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftLavender
                  )
                  Text(
                    text = "Contains history: ${chatMessages.size} entries",
                    fontFamily = NotoSansDevanagariFamily,
                    fontSize = 12.sp,
                    color = QuietCharcoal
                  )
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              if (journalSuccessMsg.isNotEmpty()) {
                Text(
                  text = journalSuccessMsg,
                  fontFamily = PoppinsFamily,
                  fontSize = 13.sp,
                  color = SageGreen,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 12.dp)
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                TextButton(
                  onClick = {
                    journalSuccessMsg = ""
                    showJournalOverlay = false
                  }
                ) {
                  Text("Dismiss", fontFamily = PoppinsFamily, color = SoftSlate)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                  onClick = {
                    coroutineScope.launch {
                      try {
                        companionRepo.logMood(
                          mood = detectedEmotionState,
                          notes = "Journal Entry containing ${chatMessages.size} conversational chats."
                        )
                        journalSuccessMsg = "Entry sealed successfully! 🔐"
                        delay(1200)
                        journalSuccessMsg = ""
                        showJournalOverlay = false
                      } catch (e: Exception) {
                        journalSuccessMsg = "Error saving journal."
                      }
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = SoftLavender),
                  shape = RoundedCornerShape(16.dp)
                ) {
                  Text("Seal Entry", fontFamily = PoppinsFamily, color = QuietCharcoal)
                }
              }
            }
          }
        }
      }

    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPANION VIEW CANVAS GRAPHICS DECLARATIONS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CompanionInteractiveGraphicsCanvas(
  companionType: String,
  emotion: String,
  isThinking: Boolean,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "canvas_companion")

  // Idle vertical translation floating offset
  val floatOffset by infiniteTransition.animateFloat(
    initialValue = -5f,
    targetValue = 5f,
    animationSpec = infiniteRepeatable(
      animation = tween(2500, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "float_canvas"
  )

  // Calming somatic expansion ratio
  val breathingScale by infiniteTransition.animateFloat(
    initialValue = 0.97f,
    targetValue = 1.03f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breath_canvas"
  )

  // Blinking cycle interval parameter
  var blinkState by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    while (true) {
      delay((4000..7000).random().toLong())
      blinkState = true
      delay(120)
      blinkState = false
    }
  }

  // Draw procedural companion canvas elements
  Box(
    modifier = modifier
      .offset(y = floatOffset.dp)
      .scale(breathingScale)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = w / 2f
      val cy = h / 2f

      // Configure companion parameters
      val faceColor = when (companionType) {
        "mochi_cat" -> Color(0xFFF2F4F7)
        "star_rabbit" -> Color(0xFFFFF7F2)
        "cozy_bear" -> Color(0xFFECDAC6)
        else -> Color(0xFFF2F4F7)
      }
      val innerEarColor = Color(0xFFFFC0BD)
      val faceOutlineColor = SoftSlate.copy(alpha = 0.15f)

      // DRAW COMPANION BACKGLOW PULSES (LONELY GLOW / EMOTIONS)
      val ambientGlowColor = when(emotion) {
        "lonely" -> SoftLavender.copy(alpha = 0.25f)
        "excited" -> Color(0xFFFFE082).copy(alpha = 0.3f)
        "happy" -> SageGreen.copy(alpha = 0.2f)
        else -> SoftLavender.copy(alpha = 0.08f)
      }
      drawRadiusRing(cx, cy + 4f, 54f, ambientGlowColor)

      // 1. EAR ANATOMY SHAPES
      if (companionType == "cozy_bear") {
        // Left Ear
        drawCircle(faceColor, radius = cx * 0.28f, center = Offset(cx - cx * 0.45f, cy - cy * 0.45f))
        drawCircle(innerEarColor, radius = cx * 0.15f, center = Offset(cx - cx * 0.45f, cy - cy * 0.45f))
        
        // Right Ear
        val earOffset = if (isThinking) (-3f).dp.toPx() else 0f
        drawCircle(faceColor, radius = cx * 0.28f, center = Offset(cx + cx * 0.45f, cy - cy * 0.45f + earOffset))
        drawCircle(innerEarColor, radius = cx * 0.15f, center = Offset(cx + cx * 0.45f, cy - cy * 0.45f + earOffset))
      } 
      else if (companionType == "star_rabbit") {
        // Left bunny ear
        val leftEar = Path().apply {
          moveTo(cx - cx * 0.38f, cy - cy * 0.3f)
          quadraticBezierTo(cx - cx * 0.58f, cy - cy * 1.1f, cx - cx * 0.38f, cy - cy * 1.15f)
          quadraticBezierTo(cx - cx * 0.18f, cy - cy * 1.15f, cx - cx * 0.15f, cy - cy * 0.3f)
          close()
        }
        drawPath(leftEar, faceColor)
        
        // Right bunny ear - twitch if thinking or happy
        val twitchEarOffset = if (isThinking || emotion == "excited") (-12f) else 0f
        val rightEar = Path().apply {
          moveTo(cx + cx * 0.15f, cy - cy * 0.3f)
          quadraticBezierTo(cx + cx * 0.18f, cy - cy * 1.1f + twitchEarOffset, cx + cx * 0.38f, cy - cy * 1.15f + twitchEarOffset)
          quadraticBezierTo(cx + cx * 0.58f, cy - cy * 1.15f + twitchEarOffset, cx + cx * 0.38f, cy - cy * 0.3f)
          close()
        }
        drawPath(rightEar, faceColor)
      } 
      else {
        // Default: Mochi pointed cat ears
        val leftEar = Path().apply {
          moveTo(cx - cx * 0.55f, cy - cy * 0.25f)
          lineTo(cx - cx * 0.8f, cy - cy * 0.85f)
          lineTo(cx - cx * 0.2f, cy - cy * 0.45f)
          close()
        }
        val rightEar = Path().apply {
          moveTo(cx + cx * 0.55f, cy - cy * 0.25f)
          lineTo(cx + cx * 0.8f, cy - cy * 0.85f)
          lineTo(cx + cx * 0.2f, cy - cy * 0.45f)
          close()
        }
        drawPath(leftEar, faceColor)
        drawPath(rightEar, faceColor)

        // Cute inner pink cat ears
        val leftEarIn = Path().apply {
          moveTo(cx - cx * 0.46f, cy - cy * 0.28f)
          lineTo(cx - cx * 0.68f, cy - cy * 0.72f)
          lineTo(cx - cx * 0.25f, cy - cy * 0.42f)
          close()
        }
        val rightEarIn = Path().apply {
          moveTo(cx + cx * 0.46f, cy - cy * 0.28f)
          lineTo(cx + cx * 0.68f, cy - cy * 0.72f)
          lineTo(cx + cx * 0.25f, cy - cy * 0.42f)
          close()
        }
        drawPath(leftEarIn, innerEarColor)
        drawPath(rightEarIn, innerEarColor)
      }

      // 2. FACE MAIN VOLUME CIRCLE
      val faceRadiusHeight = cy * 0.76f
      drawCircle(
        color = faceColor,
        radius = faceRadiusHeight,
        center = Offset(cx, cy + 3f)
      )

      // 3. FLUID EMOTIONAL TAIL DETAILS
      if (companionType == "mochi_cat" || companionType == "star_rabbit") {
        // Smooth tail swishing procedurally
        val swishFreq = if (emotion == "excited" || emotion == "happy") 12 else 4
        val swishOffset = Math.sin(System.currentTimeMillis() * 0.005 * swishFreq) * 12.0
        drawCircle(
          color = faceColor,
          radius = 11f,
          center = Offset(cx + cx * 0.62f + swishOffset.toFloat(), cy + cy * 0.45f)
        )
      }

      // 4. EMBELLISH ROTATING / EMOTIONAL CHEEKS BLUSH GENTLY
      val blushAlpha = if (emotion == "happy" || emotion == "excited") 0.7f else 0.25f
      drawCircle(
        color = Color(0xFFFF9E9D).copy(alpha = blushAlpha),
        radius = cx * 0.12f,
        center = Offset(cx - cx * 0.45f, cy + cy * 0.2f)
      )
      drawCircle(
        color = Color(0xFFFF9E9D).copy(alpha = blushAlpha),
        radius = cx * 0.12f,
        center = Offset(cx + cx * 0.45f, cy + cy * 0.2f)
      )

      // 5. BLINKING AND DYNAMIC GRAPHIC EYES
      val eyeYOffset = if (emotion == "sad" || emotion == "stressed") 4f else 0f
      val renderEyesClosed = blinkState || emotion == "stressed" || emotion == "anxious"

      if (renderEyesClosed) {
        // Curved calming eyes line
        drawLine(
          color = Color(0xFF2C3E50),
          start = Offset(cx - cx * 0.35f, cy + eyeYOffset),
          end = Offset(cx - cx * 0.18f, cy + eyeYOffset),
          strokeWidth = 4f
        )
        drawLine(
          color = Color(0xFF2C3E50),
          start = Offset(cx + cx * 0.18f, cy + eyeYOffset),
          end = Offset(cx + cx * 0.35f, cy + eyeYOffset),
          strokeWidth = 4f
        )
      } else {
        // Round open high-fidelity eye or arched joyful curves
        if (emotion == "happy" || emotion == "excited") {
          // Joyous crescents
          val leftEyeArch = Path().apply {
            moveTo(cx - cx * 0.35f, cy + cy * 0.08f)
            quadraticBezierTo(cx - cx * 0.27f, cy - cy * 0.06f, cx - cx * 0.19f, cy + cy * 0.08f)
          }
          val rightEyeArch = Path().apply {
            moveTo(cx + cx * 0.19f, cy + cy * 0.08f)
            quadraticBezierTo(cx + cx * 0.27f, cy - cy * 0.06f, cx + cx * 0.35f, cy + cy * 0.08f)
          }
          drawPath(leftEyeArch, Color(0xFF2C3E50), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.5f))
          drawPath(rightEyeArch, Color(0xFF2C3E50), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.5f))
        } else {
          // Default round shiny eyes with dynamic white reflections
          drawCircle(
            color = Color(0xFF2C3E50),
            radius = cx * 0.09f,
            center = Offset(cx - cx * 0.28f, cy + eyeYOffset)
          )
          drawCircle(
            color = Color(0xFF2C3E50),
            radius = cx * 0.09f,
            center = Offset(cx + cx * 0.28f, cy + eyeYOffset)
          )
          // Sparkle specular rays
          drawCircle(
            color = Color.White,
            radius = cx * 0.028f,
            center = Offset(cx - cx * 0.31f, cy - cy * 0.02f + eyeYOffset)
          )
          drawCircle(
            color = Color.White,
            radius = cx * 0.028f,
            center = Offset(cx + cx * 0.25f, cy - cy * 0.02f + eyeYOffset)
          )
        }
      }

      // 6. ADORABLE NOSE AND EMOTIONAL MOUTH LINE RENDER
      val nosePath = Path().apply {
        moveTo(cx - 3.5f, cy + cy * 0.12f)
        lineTo(cx + 3.5f, cy + cy * 0.12f)
        lineTo(cx, cy + cy * 0.18f)
        close()
      }
      drawPath(nosePath, Color(0xFFE5A199))

      // Mouth Curve
      val mouthPath = Path().apply {
        if (emotion == "happy" || emotion == "excited") {
          // Open joyous tongue smiley shape
          moveTo(cx - 9f, cy + cy * 0.24f)
          quadraticBezierTo(cx, cy + cy * 0.44f, cx + 9f, cy + cy * 0.24f)
        } else if (emotion == "sad") {
          // Drooping curve line
          moveTo(cx - 8f, cy + cy * 0.29f)
          quadraticBezierTo(cx, cy + cy * 0.21f, cx + 8f, cy + cy * 0.29f)
        } else {
          // Cozy double claw cat anchor mouth line curves
          moveTo(cx - cx * 0.1f, cy + cy * 0.23f)
          quadraticBezierTo(cx - cx * 0.05f, cy + cy * 0.29f, cx, cy + cy * 0.23f)
          quadraticBezierTo(cx + cx * 0.05f, cy + cy * 0.29f, cx + cx * 0.1f, cy + cy * 0.23f)
        }
      }
      drawPath(
        path = mouthPath,
        color = Color(0xFF2C3E50),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
      )
    }
  }
}

// Draw back ambient halos safely
private fun DrawScope.drawRadiusRing(cx: Float, cy: Float, radius: Float, color: Color) {
  drawCircle(
    color = color,
    radius = radius * 1.5f,
    center = Offset(cx, cy)
  )
}


// ─────────────────────────────────────────────────────────────────────────────
// CONVERSATION BUBBLES SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ChatBubbleRow(
  chatMessage: ChatMessageDto,
  companionName: String
) {
  val isUser = chatMessage.role == "user"

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      // Small companion head profile alignment icon beside reply
      Box(
        modifier = Modifier
          .padding(top = 4.9.dp, end = 8.dp)
          .size(32.dp)
          .clip(CircleShape)
          .background(SoftLavender.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "💚", fontSize = 14.sp)
      }
    }

    // Message context bubble card wrapper
    Card(
      modifier = Modifier
        .widthIn(max = 285.dp)
        .shadow(
          elevation = 1.3.dp,
          shape = if (isUser) {
            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 2.dp)
          } else {
            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 2.dp)
          }
        )
        .testTag(if (isUser) "user_bubble" else "companion_bubble"),
      shape = if (isUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 2.dp)
      } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 2.dp)
      },
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) SoftLavender else PureWhite
      ),
      border = if (isUser) null else BorderStroke(1.dp, SoftSlate.copy(alpha = 0.08f))
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
      ) {
        Text(
          text = chatMessage.message,
          fontFamily = NotoSansDevanagariFamily,
          fontSize = 14.2.sp,
          color = QuietCharcoal,
          lineHeight = 19.8.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Micro-tag displaying message creation timestamp
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val timestampText = remember(chatMessage.created_at) {
            val cal = Calendar.getInstance().apply { timeInMillis = chatMessage.created_at }
            String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
          }
          Text(
            text = timestampText,
            fontSize = 9.sp,
            color = SoftSlate.copy(alpha = 0.8f),
            fontFamily = PoppinsFamily
          )
        }
      }
    }
  }
}

// Companion typing display dots (Thinking Animation)
@Composable
fun CompanionThinkingBubbleRow(companionName: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .padding(end = 8.dp)
        .size(32.dp)
        .clip(CircleShape)
        .background(SoftLavender.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Text(text = "⏳", fontSize = 14.sp)
    }

    Card(
      modifier = Modifier
        .width(85.dp)
        .shadow(1.dp, RoundedCornerShape(18.dp)),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhite),
      border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.05f))
    ) {
      Row(
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 14.dp)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Floating dots animations
        val infiniteTransition = rememberInfiniteTransition(label = "thinking_dots")
        repeat(3) { index ->
          val dotPulse by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
              animation = tween(600, delayMillis = index * 180, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            ),
            label = "dot"
          )
          Box(
            modifier = Modifier
              .size(6.5.dp)
              .clip(CircleShape)
              .background(SoftLavender.copy(alpha = dotPulse))
          )
        }
      }
    }
  }
}
