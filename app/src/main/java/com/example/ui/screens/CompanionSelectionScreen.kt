package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppContainer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class CompanionInfo(
  val id: String,
  val name: String,
  val defaultName: String,
  val traits: List<String>,
  val accentColor: Color,
  val introduction: String,
  val description: String,
  val voiceQuote: String
)

@Composable
fun SoulTalkCompanionSelectionScreen(onCompanionSaved: () -> Unit) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val repository = remember { AppContainer.getRepository(context) }

  val companions = listOf(
    CompanionInfo(
      id = "mochi_cat",
      name = "Mochi Cat",
      defaultName = "Mochi",
      traits = listOf("Calm", "Friendly", "Comforting"),
      accentColor = SageGreen,
      introduction = "Hi, I'm Mochi. I'll be here whenever you need a quiet friend.",
      description = "Mochi is a master of peaceful presence. Best suited for deep listening, quiet companionship, and grounding your thoughts when things move too fast.",
      voiceQuote = "Purr... Hello! I'm Mochi. I am so glad to finally meet you. Let's rest our minds together."
    ),
    CompanionInfo(
      id = "buddy_dog",
      name = "Buddy Dog",
      defaultName = "Buddy",
      traits = listOf("Energetic", "Loyal", "Supportive"),
      accentColor = SoftSkyBlue,
      introduction = "Hi, I'm Buddy. We'll celebrate every small victory together.",
      description = "Buddy brings a warm spark of active optimism. Perfect for starting positive habits, celebrating steps, and gently encouraging you through heavy days.",
      voiceQuote = "Woof! Hi friend, I'm Buddy! You did incredible just getting through today. We are going to be best team!"
    ),
    CompanionInfo(
      id = "nova_fox",
      name = "Nova Fox",
      defaultName = "Nova",
      traits = listOf("Curious", "Smart", "Playful"),
      accentColor = SoftLavender,
      introduction = "Hi, I'm Nova. Let's discover new strengths together.",
      description = "Nova is playful, smart, and loves cognitive exploration. Best for creative distraction, daily mindfulness prompts, and finding light perspectives.",
      voiceQuote = "Hello there! I'm Nova. The world is full of secret beauty, let's go find some of it together."
    ),
    CompanionInfo(
      id = "zen_panda",
      name = "Zen Panda",
      defaultName = "Zen",
      traits = listOf("Relaxed", "Patient", "Peaceful"),
      accentColor = Color(0xFFA5D6A7),
      introduction = "Hi, I'm Zen. Let's slow down and breathe together.",
      description = "Zen is an anchor of stability. Excellent for breathing exercises, body scans, deep somatic slowing, and finding absolute quiet.",
      voiceQuote = "Breathe in... and let it drift away. I am Zen, and I am here to help you slow down. There's no rush."
    ),
    CompanionInfo(
      id = "aura_owl",
      name = "Aura Owl",
      defaultName = "Aura",
      traits = listOf("Wise", "Reflective", "Thoughtful"),
      accentColor = Color(0xFFCE93D8),
      introduction = "Hi, I'm Aura. I'll help you see things from a new perspective.",
      description = "Aura brings deep, calm reflection. Ideal for identifying emotional patterns, structured mindfulness guidance, and journaling insights.",
      voiceQuote = "Greetings. I'm Aura. Often, a tiny shift in perspective can transform a dark cloud into a canvas of wisdom."
    ),
    CompanionInfo(
      id = "lumi_firefly",
      name = "Lumi Firefly",
      defaultName = "Lumi",
      traits = listOf("Gentle", "Magical", "Hopeful"),
      accentColor = Color(0xFFFFF59D),
      introduction = "Hi, I'm Lumi. I'll light the way during difficult moments.",
      description = "Lumi glows warmest when things feel dark. Focused entirely on comfort, providing emotional warmth, light heart checks, and beautiful hope.",
      voiceQuote = "Sparkle... Hi! I'm Lumi. Even in the deepest darkness, you have a tiny light right here beside you. Breathe."
    )
  )

  var selectedCompanion by remember { mutableStateOf<CompanionInfo?>(null) }
  var showNamingDialog by remember { mutableStateOf(false) }
  var customCompanionName by remember { mutableStateOf("") }
  var isSaving by remember { mutableStateOf(false) }

  // 60FPS fluid canvas clock
  val transitionClock = rememberInfiniteTransition(label = "canvas_clock")
  val timePhase by transitionClock.animateFloat(
    initialValue = 0f,
    targetValue = 200f * Math.PI.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(60000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radial_phase"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CalmingBackground)
      .testTag("companion_selection_screen_root")
  ) {
    // Scrollable Master Layout
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // TITLE AND SUBTITLE HEADER
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = SageGreen,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "SOULTALK COMPANION",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            color = SageGreen
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Choose Your Soul Companion",
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp,
          color = QuietCharcoal,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("selection_header_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Your companion will grow, learn and journey alongside you.",
          fontFamily = NotoSansDevanagariFamily,
          fontSize = 14.sp,
          color = SoftSlate,
          textAlign = TextAlign.Center,
          lineHeight = 19.sp,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // COMPANIONS 2-COLUMN GRID
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .testTag("companions_grid"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(companions) { companion ->
          CompanionGridCard(
            companion = companion,
            time = timePhase,
            onClick = {
              selectedCompanion = companion
            }
          )
        }
      }
    }

    // EXPANDED SELECTION OVERLAY WITH SOFT BLUR/FADE EFFECT
    AnimatedVisibility(
      visible = selectedCompanion != null,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
      selectedCompanion?.let { info ->
        ExpandedDetailModal(
          companion = info,
          time = timePhase,
          onClose = { selectedCompanion = null },
          onConfirmSelect = {
            customCompanionName = info.defaultName
            showNamingDialog = true
          }
        )
      }
    }

    // CUSTOM NAMING MODAL
    if (showNamingDialog && selectedCompanion != null) {
      val companion = selectedCompanion!!
      Dialog(
        onDismissRequest = { showNamingDialog = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth(0.9f)
            .wrapContentHeight()
            .shadow(16.dp, shape = RoundedCornerShape(28.dp))
            .testTag("naming_modal_card"),
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Name Your Companion",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = QuietCharcoal
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "What would you like to call your ${companion.name}?",
              fontFamily = NotoSansDevanagariFamily,
              fontSize = 14.sp,
              color = SoftSlate,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
              value = customCompanionName,
              onValueChange = { customCompanionName = it },
              singleLine = true,
              textStyle = LocalTextStyle.current.copy(
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
              ),
              placeholder = { Text("Enter a name...") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("companion_name_input_field"),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = companion.accentColor,
                unfocusedBorderColor = SoftSlate.copy(alpha = 0.3f),
                focusedContainerColor = CalmingBackground,
                unfocusedContainerColor = CalmingBackground
              )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              TextButton(
                onClick = { showNamingDialog = false },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
              ) {
                Text(
                  text = "Cancel",
                  fontFamily = PoppinsFamily,
                  color = SoftSlate,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Button(
                onClick = {
                  isSaving = true
                  coroutineScope.launch {
                    val finalName = customCompanionName.trim().ifEmpty { companion.defaultName }
                    val success = repository.selectCompanion(
                      type = companion.id,
                      name = finalName,
                      personality = companion.traits.joinToString(", ")
                    )
                    delay(800) // Ensure realistic feedback
                    isSaving = false
                    if (success) {
                      showNamingDialog = false
                      selectedCompanion = null
                      onCompanionSaved()
                    }
                  }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                  containerColor = companion.accentColor,
                  contentColor = if (companion.accentColor == Color(0xFFFFF59D)) QuietCharcoal else Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("confirm_selection_and_name_button")
              ) {
                if (isSaving) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                  )
                } else {
                  Text(
                    text = "Confirm",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Standard grid item card displaying the companion in 2-column format.
 */
@Composable
fun CompanionGridCard(
  companion: CompanionInfo,
  time: Float,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(0.72f)
      .clickable { onClick() }
      .shadow(
        elevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        ambientColor = SoftSlate.copy(alpha = 0.1f),
        spotColor = SoftSlate.copy(alpha = 0.2f)
      )
      .testTag("companion_card_${companion.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Companion Canvas render zone
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1.0f)
          .clip(RoundedCornerShape(14.dp))
          .background(companion.accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Box(modifier = Modifier.size(105.dp)) {
          AnimatedCompanionDrawCanvas(
            companionId = companion.id,
            time = time,
            isDetailView = false
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Name & Personality
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = companion.name,
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = QuietCharcoal
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = companion.traits.firstOrNull() ?: "Friendly",
          fontFamily = NotoSansDevanagariFamily,
          fontSize = 12.sp,
          color = SoftSlate,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

/**
 * Modal window overlay showing complete companion details, animations, voice message and companion progression tracks.
 */
@Composable
fun ExpandedDetailModal(
  companion: CompanionInfo,
  time: Float,
  onClose: () -> Unit,
  onConfirmSelect: () -> Unit
) {
  var isPlayingVoice by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  // Slower background ambient particle time phase
  val ambientPhase = rememberInfiniteTransition(label = "pulse")
  val sizeMultiplier by ambientPhase.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breath"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CalmingBackground.copy(alpha = 0.96f))
      .clickable(enabled = false) {} // block clickthrough
      .testTag("detail_overlay_root"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {

      // Header actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onClose,
          modifier = Modifier
            .background(PureWhite, CircleShape)
            .testTag("close_detail_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close description",
            tint = QuietCharcoal
          )
        }

        Text(
          text = "Meet your Companion",
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = SoftSlate
        )

        Spacer(modifier = Modifier.size(40.dp))
      }

      // Main Content Scroll Column
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // High fidelity large animated model zone with backing glow
        Box(
          modifier = Modifier
            .size(200.dp)
            .shadow(
              elevation = 4.dp,
              shape = CircleShape,
              ambientColor = companion.accentColor.copy(alpha = 0.4f),
              spotColor = companion.accentColor.copy(alpha = 0.6f)
            )
            .background(companion.accentColor.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          // Extra background magic halo rings
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(companion.accentColor.copy(alpha = 0.25f), Color.Transparent),
                center = center,
                radius = size.width * 0.5f * sizeMultiplier
              )
            )
          }

          Box(modifier = Modifier.size(140.dp)) {
            AnimatedCompanionDrawCanvas(
              companionId = companion.id,
              time = time,
              isDetailView = true
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title and traits
        Text(
          text = companion.name,
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 22.sp,
          color = QuietCharcoal
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          companion.traits.forEach { trait ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(companion.accentColor.copy(alpha = 0.22f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = trait,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = QuietCharcoal
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Empathetic narrative quotes & descriptions
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "\"${companion.introduction}\"",
              fontFamily = NotoSansDevanagariFamily,
              fontWeight = FontWeight.Medium,
              fontSize = 14.sp,
              color = SageGreen,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = companion.description,
              fontFamily = NotoSansDevanagariFamily,
              fontSize = 13.sp,
              color = SoftSlate,
              textAlign = TextAlign.Center,
              lineHeight = 18.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // VOICE MESSAGE SIMULATOR WITH SOUNDWAVES
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .testTag("voice_message_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = companion.accentColor.copy(alpha = 0.08f))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = {
                if (!isPlayingVoice) {
                  isPlayingVoice = true
                  scope.launch {
                    delay(4000) // Simulates talking voice duration
                    isPlayingVoice = false
                  }
                }
              },
              modifier = Modifier
                .size(44.dp)
                .background(companion.accentColor, CircleShape)
                .testTag("voice_play_button")
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Listen to voice sample",
                tint = if (companion.accentColor == Color(0xFFFFF59D)) QuietCharcoal else Color.White
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isPlayingVoice) "Listening..." else "Hear My Voice",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = QuietCharcoal
              )

              if (isPlayingVoice) {
                Text(
                  text = companion.voiceQuote,
                  fontFamily = NotoSansDevanagariFamily,
                  fontSize = 11.sp,
                  color = QuietCharcoal.copy(alpha = 0.8f),
                  lineHeight = 15.sp,
                  modifier = Modifier.padding(top = 4.dp)
                )
              } else {
                Spacer(modifier = Modifier.height(4.dp))
                // Clean soundwave vector indicators
                Canvas(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                ) {
                  val barCount = 18
                  val barWidth = 4f
                  val gap = 6f
                  val h = size.height
                  for (i in 0 until barCount) {
                    val progressPhase = sin(time * 0.12f + i * 0.4f)
                    val barHeight = h * (0.3f + 0.6f * Math.abs(progressPhase))
                    drawRoundRect(
                      color = companion.accentColor,
                      topLeft = Offset(i * (barWidth + gap), (h - barHeight) / 2f),
                      size = Size(barWidth, barHeight),
                      cornerRadius = CornerRadius(2f, 2f)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FUTURE COMPANION GROWTH PREVIEW
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          horizontalAlignment = Alignment.Start
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = companion.accentColor,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Companion Growth Preview",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = QuietCharcoal
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 5-steps visual trail
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val stages = listOf(
              "Lvl 1" to "Tiny",
              "Lvl 2" to "Growing",
              "Lvl 3" to "Healthy",
              "Lvl 4" to "Thriving",
              "Lvl 5" to "Guardian"
            )

            stages.forEachIndexed { idx, stage ->
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                      if (idx == 0) companion.accentColor else companion.accentColor.copy(alpha = 0.15f)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${idx + 1}",
                    fontFamily = PoppinsFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (idx == 0) QuietCharcoal else SoftSlate
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = stage.second,
                  fontFamily = PoppinsFamily,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (idx == 0) QuietCharcoal else SoftSlate.copy(alpha = 0.6f)
                )
              }

              if (idx < stages.size - 1) {
                // Line connection
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(
                      if (idx == 0) companion.accentColor else SoftSlate.copy(alpha = 0.15f)
                    )
                )
              }
            }
          }
        }
      }

      // Action select buttons
      Button(
        onClick = onConfirmSelect,
        colors = ButtonDefaults.buttonColors(
          containerColor = companion.accentColor,
          contentColor = if (companion.accentColor == Color(0xFFFFF59D)) QuietCharcoal else Color.White
        ),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(26.dp),
            ambientColor = companion.accentColor.copy(alpha = 0.3f),
            spotColor = companion.accentColor.copy(alpha = 0.5f)
          )
          .testTag("onboarding_select_companion_button")
      ) {
        Text(
          text = "Select & Name ${companion.defaultName}",
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    }
  }
}

/**
 * 60 FPS Custom Canvas Procedural Drawings for companions
 */
@Composable
fun AnimatedCompanionDrawCanvas(
  companionId: String,
  time: Float,
  isDetailView: Boolean
) {
  val density = LocalDensity.current.density
  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    // Core continuous looping variables
    val blink = ((time * 0.05f) % 2f) < 0.12f // Periodic micro-blink
    val tailSway = sin(time * 0.14f)
    val floatY = cos(time * 0.12f) * (if (isDetailView) 8f else 4f) * density
    val breathe = sin(time * 0.08f) * (if (isDetailView) 3f else 1.5f) * density

    val cx = w / 2f
    val cy = h / 2f + floatY

    when (companionId) {
      "mochi_cat" -> {
        // CAT PROCEDURAL DRAW
        // Ears
        val earPathLeft = Path().apply {
          moveTo(cx - 36f * density, cy - 24f * density)
          lineTo(cx - 46f * density, cy - 54f * density + (tailSway * 3f))
          lineTo(cx - 16f * density, cy - 36f * density)
          close()
        }
        drawPath(path = earPathLeft, color = PureWhite)
        drawPath(path = earPathLeft, color = Color(0xFFFFCDD2)) // Inner pink

        val earPathRight = Path().apply {
          moveTo(cx + 36f * density, cy - 24f * density)
          lineTo(cx + 46f * density, cy - 54f * density - (tailSway * 3f))
          lineTo(cx + 16f * density, cy - 36f * density)
          close()
        }
        drawPath(path = earPathRight, color = PureWhite)
        drawPath(path = earPathRight, color = Color(0xFFFFCDD2))

        // Cute circular tail
        drawCircle(
          color = PureWhite,
          radius = 12f * density,
          center = Offset(cx + 38f * density + tailSway * 8f, cy + 24f * density)
        )

        // Chubby Head Body
        drawCircle(color = PureWhite, radius = 38f * density + breathe, center = Offset(cx, cy))

        // Eyes
        val eyeRadius = 3.5f * density
        if (!blink) {
          drawCircle(color = QuietCharcoal, radius = eyeRadius, center = Offset(cx - 14f * density, cy - 2f * density))
          drawCircle(color = QuietCharcoal, radius = eyeRadius, center = Offset(cx + 14f * density, cy - 2f * density))
        } else {
          // Closed blink arc
          drawArc(
            color = QuietCharcoal,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - 18f * density, cy - 5f * density),
            size = Size(8f * density, 6f * density),
            style = Stroke(2.5f * density)
          )
          drawArc(
            color = QuietCharcoal,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx + 10f * density, cy - 5f * density),
            size = Size(8f * density, 6f * density),
            style = Stroke(2.5f * density)
          )
        }

        // Cheeks
        drawCircle(color = Color(0xFFFF9494).copy(alpha = 0.5f), radius = 5.5f * density, center = Offset(cx - 20f * density, cy + 6f * density))
        drawCircle(color = Color(0xFFFF9494).copy(alpha = 0.5f), radius = 5.5f * density, center = Offset(cx + 20f * density, cy + 6f * density))

        // Nose-Mouth Mouth curves
        drawCircle(color = Color(0xFFE57373), radius = 2.5f * density, center = Offset(cx, cy + 3f * density))
        val mouthPath = Path().apply {
          moveTo(cx - 4f * density, cy + 7f * density)
          quadraticTo(cx - 2f * density, cy + 10f * density, cx, cy + 7f * density)
          quadraticTo(cx + 2f * density, cy + 10f * density, cx + 4f * density, cy + 7f * density)
        }
        drawPath(path = mouthPath, color = QuietCharcoal, style = Stroke(2f * density))
      }

      "buddy_dog" -> {
        // DOG PROCEDURAL DRAW
        // Head
        drawCircle(color = Color(0xFFF5C27A), radius = 35f * density + breathe, center = Offset(cx, cy))

        // Snout
        drawRoundRect(
          color = Color(0xFFFFE0B2),
          topLeft = Offset(cx - 16f * density, cy),
          size = Size(32f * density, 20f * density),
          cornerRadius = CornerRadius(10f * density, 10f * density)
        )
        // Nose
        drawRoundRect(
          color = QuietCharcoal,
          topLeft = Offset(cx - 5f * density, cy + 2f * density),
          size = Size(10f * density, 6f * density),
          cornerRadius = CornerRadius(3f * density, 3f * density)
        )

        // Tongue / Mouth panting
        val tongueAmp = Math.abs(sin(time * 0.16f)) * 5f * density
        drawRoundRect(
          color = Color(0xFFFF8A80),
          topLeft = Offset(cx - 4.5f * density, cy + 10f * density),
          size = Size(9f * density, 8f * density + tongueAmp),
          cornerRadius = CornerRadius(4.5f * density, 4.5f * density)
        )

        // Bouncy Floppy Ears
        val earOffset = sin(time * 0.12f) * 4f * density
        drawRoundRect(
          color = Color(0xFFCE934A),
          topLeft = Offset(cx - 48f * density, cy - 20f * density + earOffset),
          size = Size(16f * density, 38f * density),
          cornerRadius = CornerRadius(8f * density, 8f * density)
        )
        drawRoundRect(
          color = Color(0xFFCE934A),
          topLeft = Offset(cx + 32f * density, cy - 20f * density - earOffset),
          size = Size(16f * density, 38f * density),
          cornerRadius = CornerRadius(8f * density, 8f * density)
        )

        // Eyes
        if (!blink) {
          drawCircle(color = QuietCharcoal, radius = 4f * density, center = Offset(cx - 12f * density, cy - 4f * density))
          drawCircle(color = QuietCharcoal, radius = 4f * density, center = Offset(cx + 12f * density, cy - 4f * density))
          // Catchlight
          drawCircle(color = Color.White, radius = 1.2f * density, center = Offset(cx - 13.5f * density, cy - 5.5f * density))
          drawCircle(color = Color.White, radius = 1.2f * density, center = Offset(cx + 10.5f * density, cy - 5.5f * density))
        } else {
          drawArc(
            color = QuietCharcoal,
            startAngle = 0f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - 16f * density, cy - 6f * density), size = Size(8f * density, 5f * density),
            style = Stroke(2.5f * density)
          )
          drawArc(
            color = QuietCharcoal,
            startAngle = 0f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx + 8f * density, cy - 6f * density), size = Size(8f * density, 5f * density),
            style = Stroke(2.5f * density)
          )
        }

        // Little rapid wagging wag tail behind
        val wagOffset = tailSway * 12f * density
        val dogTail = Path().apply {
          moveTo(cx + 25f * density, cy + 20f * density)
          quadraticTo(cx + 42f * density, cy + 28f * density, cx + 50f * density + wagOffset, cy + 10f * density)
        }
        drawPath(path = dogTail, color = Color(0xFFF5C27A), style = Stroke(5f * density))
      }

      "nova_fox" -> {
        // FOX PROCEDURAL DRAW
        // Ears Pointy
        val earPointsL = Path().apply {
          moveTo(cx - 30f * density, cy - 20f * density)
          lineTo(cx - 40f * density, cy - 52f * density)
          lineTo(cx - 10f * density, cy - 30f * density)
          close()
        }
        drawPath(path = earPointsL, color = Color(0xFFE65100))
        drawPath(path = earPointsL, color = Color(0xFFFFCC80), style = Stroke(2f * density))

        val earPointsR = Path().apply {
          moveTo(cx + 30f * density, cy - 20f * density)
          lineTo(cx + 40f * density, cy - 52f * density)
          lineTo(cx + 10f * density, cy - 30f * density)
          close()
        }
        drawPath(path = earPointsR, color = Color(0xFFE65100))
        drawPath(path = earPointsR, color = Color(0xFFFFCC80), style = Stroke(2f * density))

        // Cozy Bushy Tail with white cap
        val foxTail = Path().apply {
          moveTo(cx + 20f * density, cy + 20f * density)
          cubicTo(
            cx + 45f * density, cy + 10f * density,
            cx + 45f * density + tailSway * 6f, cy - 24f * density,
            cx + 28f * density, cy - 28f * density
          )
        }
        drawPath(path = foxTail, color = Color(0xFFFF7043), style = Stroke(14f * density))
        drawCircle(color = PureWhite, radius = 6f * density, center = Offset(cx + 28f * density, cy - 28f * density))

        // Main Fox Face Base
        val facePath = Path().apply {
          moveTo(cx - 35f * density, cy - 10f * density)
          quadraticTo(cx - 45f * density, cy + 10f * density, cx, cy + 25f * density)
          quadraticTo(cx + 45f * density, cy + 10f * density, cx + 35f * density, cy - 10f * density)
          quadraticTo(cx, cy - 24f * density, cx - 35f * density, cy - 10f * density)
        }
        drawPath(path = facePath, color = Color(0xFFFF7043))

        // Cheeks Whiteness
        val cheekWhitenessL = Path().apply {
          moveTo(cx - 35f * density, cy - 10f * density)
          quadraticTo(cx - 40f * density, cy + 10f * density, cx - 12f * density, cy + 14f * density)
          quadraticTo(cx - 20f * density, cy, cx - 10f * density, cy - 10f * density)
          close()
        }
        drawPath(path = cheekWhitenessL, color = PureWhite)

        val cheekWhitenessR = Path().apply {
          moveTo(cx + 35f * density, cy - 10f * density)
          quadraticTo(cx + 40f * density, cy + 10f * density, cx + 12f * density, cy + 14f * density)
          quadraticTo(cx + 20f * density, cy, cx + 10f * density, cy - 10f * density)
          close()
        }
        drawPath(path = cheekWhitenessR, color = PureWhite)

        // Small black nose
        drawCircle(color = QuietCharcoal, radius = 3f * density, center = Offset(cx, cy + 20f * density))

        // Wise slanted closed smile or eyes
        if (!blink) {
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx - 14f * density, cy - 2f * density))
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx + 14f * density, cy - 2f * density))
        } else {
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - 18f * density, cy - 4f * density), size = Size(8f * density, 5f * density),
            style = Stroke(2.5f * density)
          )
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx + 10f * density, cy - 4f * density), size = Size(8f * density, 5f * density),
            style = Stroke(2.5f * density)
          )
        }
      }

      "zen_panda" -> {
        // PANDA PROCEDURAL DRAW
        // Black ears
        drawCircle(color = QuietCharcoal, radius = 13f * density, center = Offset(cx - 28f * density, cy - 28f * density))
        drawCircle(color = QuietCharcoal, radius = 13f * density, center = Offset(cx + 28f * density, cy - 28f * density))

        // Fluffy Face Base
        drawCircle(color = PureWhite, radius = 36f * density + breathe, center = Offset(cx, cy))

        // Cute Black Eyelash Patches
        drawOval(
          color = QuietCharcoal.copy(alpha = 0.85f),
          topLeft = Offset(cx - 22f * density, cy - 12f * density),
          size = Size(14f * density, 20f * density)
        )
        drawOval(
          color = QuietCharcoal.copy(alpha = 0.85f),
          topLeft = Offset(cx + 8f * density, cy - 12f * density),
          size = Size(14f * density, 20f * density)
        )

        // Eyes inside patches
        if (!blink) {
          drawCircle(color = PureWhite, radius = 2.5f * density, center = Offset(cx - 15f * density, cy - 3f * density))
          drawCircle(color = PureWhite, radius = 2.5f * density, center = Offset(cx + 15f * density, cy - 3f * density))
        } else {
          drawCircle(color = PureWhite, radius = 1f * density, center = Offset(cx - 15f * density, cy - 3f * density))
          drawCircle(color = PureWhite, radius = 1f * density, center = Offset(cx + 15f * density, cy - 3f * density))
        }

        // Cute smiling mouth and nose
        drawCircle(color = QuietCharcoal, radius = 2.5f * density, center = Offset(cx, cy + 5f * density))
        val pandaMouth = Path().apply {
          moveTo(cx - 4f * density, cy + 9f * density)
          quadraticTo(cx - 2f * density, cy + 12f * density, cx, cy + 9f * density)
          quadraticTo(cx + 2f * density, cy + 12f * density, cx + 4f * density, cy + 9f * density)
        }
        drawPath(path = pandaMouth, color = QuietCharcoal, style = Stroke(2f * density))

        // Green swaying bamboo stick in mouth
        val bamSway = sin(time * 0.1f) * 8f * density
        val bamboo = Path().apply {
          moveTo(cx + 3f * density, cy + 9f * density)
          quadraticTo(cx + 25f * density, cy + 12f * density, cx + 34f * density, cy - 10f * density + bamSway)
        }
        drawPath(path = bamboo, color = Color(0xFF66BB6A), style = Stroke(3f * density))
      }

      "aura_owl" -> {
        // OWL PROCEDURAL DRAW
        // Feet
        drawCircle(color = Color(0xFFFFB300), radius = 4f * density, center = Offset(cx - 12f * density, cy + 34f * density))
        drawCircle(color = Color(0xFFFFB300), radius = 4f * density, center = Offset(cx + 12f * density, cy + 34f * density))

        // Lavender round body
        drawCircle(color = Color(0xFFB39DDB), radius = 34f * density + breathe, center = Offset(cx, cy))

        // Wing flaps
        val flapOffset = Math.abs(sin(time * 0.12f)) * 8f * density
        drawOval(
          color = Color(0xFF9575CD),
          topLeft = Offset(cx - 42f * density - flapOffset, cy - 10f * density),
          size = Size(12f * density, 24f * density)
        )
        drawOval(
          color = Color(0xFF9575CD),
          topLeft = Offset(cx + 30f * density + flapOffset, cy - 10f * density),
          size = Size(12f * density, 24f * density)
        )

        // Huge Intelligent Glasses frame
        drawCircle(
          color = Color(0xFFFFD54F),
          radius = 16f * density,
          center = Offset(cx - 15f * density, cy - 4f * density),
          style = Stroke(3.5f * density)
        )
        drawCircle(
          color = Color(0xFFFFD54F),
          radius = 16f * density,
          center = Offset(cx + 15f * density, cy - 4f * density),
          style = Stroke(3.5f * density)
        )
        // Linking bridge
        drawLine(
          color = Color(0xFFFFD54F),
          start = Offset(cx - 2f * density, cy - 4f * density),
          end = Offset(cx + 2f * density, cy - 4f * density),
          strokeWidth = 3.5f * density
        )

        // Wise eyes inside
        if (!blink) {
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx - 15f * density, cy - 4f * density))
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx + 15f * density, cy - 4f * density))
          // Catchlight
          drawCircle(color = Color.White, radius = 1.2f * density, center = Offset(cx - 16f * density, cy - 5.5f * density))
          drawCircle(color = Color.White, radius = 1.2f * density, center = Offset(cx + 14f * density, cy - 5.5f * density))
        } else {
          drawCircle(color = QuietCharcoal, radius = 1f * density, center = Offset(cx - 15f * density, cy - 4f * density))
          drawCircle(color = QuietCharcoal, radius = 1f * density, center = Offset(cx + 15f * density, cy - 4f * density))
        }

        // Tiny yellow triangular beak
        val beak = Path().apply {
          moveTo(cx, cy + 2f * density)
          lineTo(cx - 4f * density, cy + 8f * density)
          lineTo(cx + 4f * density, cy + 8f * density)
          close()
        }
        drawPath(path = beak, color = Color(0xFFFFB300))
      }

      "lumi_firefly" -> {
        // LUMI FIREFLY PROCEDURAL DRAW
        // Ambient glow ring behind
        val glowSize = (50f + sin(time * 0.16f) * 12f) * density
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF59D).copy(alpha = 0.55f), Color.Transparent),
            center = Offset(cx, cy + 12f * density),
            radius = glowSize
          )
        )

        // Tiny body base
        drawOval(
          color = Color(0xFFFFF176),
          topLeft = Offset(cx - 14f * density, cy - 10f * density + breathe),
          size = Size(28f * density, 34f * density)
        )

        // Glowing bottom tail
        drawCircle(
          color = Color(0xFFFFEB3B),
          radius = 12f * density,
          center = Offset(cx, cy + 20f * density + breathe)
        )

        // Antennas with little dots
        val antSway = tailSway * 4f * density
        drawLine(
          color = QuietCharcoal,
          start = Offset(cx - 6f * density, cy - 10f * density),
          end = Offset(cx - 16f * density + antSway, cy - 25f * density),
          strokeWidth = 2f * density
        )
        drawCircle(color = Color(0xFFFFEB3B), radius = 3.5f * density, center = Offset(cx - 16f * density + antSway, cy - 25f * density))

        drawLine(
          color = QuietCharcoal,
          start = Offset(cx + 6f * density, cy - 10f * density),
          end = Offset(cx + 16f * density - antSway, cy - 25f * density),
          strokeWidth = 2f * density
        )
        drawCircle(color = Color(0xFFFFEB3B), radius = 3.5f * density, center = Offset(cx + 16f * density - antSway, cy - 25f * density))

        // Rapid fluttering magical wings
        val flapFast = sin(time * 0.44f) * 12f * density
        drawOval(
          color = Color(0xFFE0F7FA).copy(alpha = 0.65f),
          topLeft = Offset(cx - 28f * density, cy - 12f * density + flapFast),
          size = Size(16f * density, 20f * density)
        )
        drawOval(
          color = Color(0xFFE0F7FA).copy(alpha = 0.65f),
          topLeft = Offset(cx + 12f * density, cy - 12f * density - flapFast),
          size = Size(16f * density, 20f * density)
        )

        // Tiny face eyes
        if (!blink) {
          drawCircle(color = QuietCharcoal, radius = 3f * density, center = Offset(cx - 6f * density, cy - 2f * density))
          drawCircle(color = QuietCharcoal, radius = 3f * density, center = Offset(cx + 6f * density, cy - 2f * density))
        } else {
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - 9f * density, cy - 4f * density), size = Size(6f * density, 4f * density),
            style = Stroke(2f * density)
          )
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx + 3f * density, cy - 4f * density), size = Size(6f * density, 4f * density),
            style = Stroke(2f * density)
          )
        }

        // Sweet blushing cheek dots
        drawCircle(color = Color(0xFFFF8B8B), radius = 2.5f * density, center = Offset(cx - 10f * density, cy + 4f * density))
        drawCircle(color = Color(0xFFFF8B8B), radius = 2.5f * density, center = Offset(cx + 10f * density, cy + 4f * density))
      }
    }
  }
}
