package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.WolfieCharacter
import com.example.ui.components.WolfieEmotion
import com.example.ui.components.WolfieSize
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Technical specification of the Onboarding Screens
 */
sealed class OnboardingPage(
  val title: String,
  val description: String,
  val colorAccent: Color
) {
  object Page1 : OnboardingPage(
    title = "A Safe Space For Your Thoughts",
    description = "Talk freely, reflect deeply and explore your emotions with a supportive AI companion.",
    colorAccent = SoftLavender
  )

  object Page2 : OnboardingPage(
    title = "Understand Your Emotional Journey",
    description = "Track moods, discover patterns and celebrate progress one step at a time.",
    colorAccent = SoftSkyBlue
  )

  object Page3 : OnboardingPage(
    title = "Grow Together Every Day",
    description = "Build healthy habits, unlock companion growth and create meaningful moments.",
    colorAccent = SageGreen
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoulTalkOnboardingScreen(onOnboardingFinished: () -> Unit) {
  val pages = listOf(
    OnboardingPage.Page1,
    OnboardingPage.Page2,
    OnboardingPage.Page3
  )
  
  val pagerState = rememberPagerState(pageCount = { pages.size })
  val coroutineScope = rememberCoroutineScope()
  
  // Continuous high-frequency time variable for 60FPS fluid canvas graphics
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

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .testTag("onboarding_screen_root"),
    color = CalmingBackground
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background subtle ambient color glow that matches the page accent
      val activeAccent = pages[pagerState.currentPage].colorAccent
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            brush = Brush.radialGradient(
              colors = listOf(
                activeAccent.copy(alpha = 0.12f),
                CalmingBackground,
                CalmingBackground
              ),
              center = Offset(500f, 400f),
              radius = 1200f
            ),
            alpha = 1.0f
          )
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        
        // PAGER SCROLL AREA
        HorizontalPager(
          state = pagerState,
          modifier = Modifier
            .weight(1.0f)
            .fillMaxWidth()
        ) { pageIndex ->
          
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            
            // ILLUSTRATION WORKSPACE - Featuring Wolfie
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .padding(bottom = 16.dp),
              contentAlignment = Alignment.Center
            ) {
              // Wolfie with different emotions for each onboarding page
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
              ) {
                // Show Wolfie with emotion matching the page
                WolfieCharacter(
                  emotion = when (pageIndex) {
                    0 -> WolfieEmotion.LISTENING  // Safe space - listening
                    1 -> WolfieEmotion.THINKING   // Emotional journey - thinking
                    else -> WolfieEmotion.CELEBRATING  // Growth - celebrating
                  },
                  size = WolfieSize.LARGE,
                  modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Subtle decorative text below Wolfie
                Text(
                  text = when (pageIndex) {
                    0 -> "Meet Wolfie"
                    1 -> "Your Emotional Guide"
                    else -> "Growing Together"
                  },
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Medium,
                    color = SoftSlate,
                    fontSize = 14.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TITLES AND DESCRIPTIONS
            Text(
              text = pages[pageIndex].title,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                color = QuietCharcoal,
                fontSize = 26.sp,
                lineHeight = 32.sp
              ),
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("onboarding_title_page_$pageIndex")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = pages[pageIndex].description,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = NotoSansDevanagariFamily,
                fontWeight = FontWeight.Normal,
                color = SoftSlate,
                fontSize = 16.sp,
                lineHeight = 24.sp
              ),
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("onboarding_desc_page_$pageIndex")
            )
          }
        }

        // BOTTOM ACTION FOOTER
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp)
            .testTag("onboarding_footer"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          
          // PREVIOUS BUTTON
          val isFirstPage = pagerState.currentPage == 0
          if (!isFirstPage) {
            TextButton(
              onClick = {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
              },
              modifier = Modifier
                .height(50.dp)
                .testTag("onboarding_prev_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = SoftSlate,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Back",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = SoftSlate
              )
            }
          } else {
            // Invisible placeholder of equal size to maintain visual grid centers
            Spacer(modifier = Modifier.size(width = 80.dp, height = 50.dp))
          }

          // SWIFT DOTS PAGE INDICATOR
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("onboarding_indicator_dots")
          ) {
            pages.forEachIndexed { index, page ->
              val isSelected = pagerState.currentPage == index
              val widthMultiplier by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot_width"
              )
              val activeColor = if (isSelected) page.colorAccent else SoftSlate.copy(alpha = 0.25f)
              
              Box(
                modifier = Modifier
                  .size(height = 8.dp, width = widthMultiplier)
                  .clip(CircleShape)
                  .background(activeAccent)
              )
            }
          }

          // NAVIGATION BUTTONS
          val isLastPage = pagerState.currentPage == pages.size - 1
          if (isLastPage) {
            ElevatedButton(
              onClick = onOnboardingFinished,
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = SageGreen,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(26.dp),
              elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
              ),
              modifier = Modifier
                .height(52.dp)
                .testTag("onboarding_get_started_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "Get Started",
                  fontFamily = PoppinsFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 15.sp,
                  letterSpacing = 0.3.sp
                )
                Icon(
                  imageVector = Icons.Default.Done,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          } else {
            ElevatedButton(
              onClick = {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
              },
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = SageGreen,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(26.dp),
              elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 6.dp
              ),
              modifier = Modifier
                .height(52.dp)
                .testTag("onboarding_next_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "Next",
                  fontFamily = PoppinsFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 15.sp,
                  letterSpacing = 0.3.sp
                )
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

        }
      }
    }
  }
}

/**
 * Screen 1 Illustration: A young person sitting peacefully, with a small glowing companion beside them.
 */
@Composable
fun AnimatedSafeSpaceIllustration(time: Float) {
  val density = LocalDensity.current.density
  
  // Custom high-fidelity canvas illustration
  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .testTag("safe_space_illustration")
  ) {
    val w = size.width
    val h = size.height

    // Master breathing scale
    val breathingWave = sin(time * 0.08f)
    val floatY = cos(time * 0.12f) * 12f

    // Draw peaceful radiant background warm light
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          SoftLavender.copy(alpha = 0.22f),
          Color.Transparent
        ),
        center = Offset(w * 0.5f, h * 0.5f),
        radius = w * 0.45f
      )
    )

    // 1. Draw Supportive Cushion/Platform for person
    drawRoundRect(
      color = Color(0xFFECEFF1),
      topLeft = Offset(w * 0.15f, h * 0.72f),
      size = Size(w * 0.7f, h * 0.12f),
      cornerRadius = CornerRadius(24f * density, 24f * density)
    )

    // 2. DRAW PERSON (Minimalist organic geometry)
    // Left shoulder / bent leg curves
    val personBodyBrush = Brush.linearGradient(
      colors = listOf(SoftSkyBlue, SoftSkyBlue.copy(alpha = 0.7f))
    )
    
    // Draw Torso
    drawRoundRect(
      brush = personBodyBrush,
      topLeft = Offset(w * 0.35f, h * 0.44f - breathingWave * 2f),
      size = Size(w * 0.14f, h * 0.28f + breathingWave * 2f),
      cornerRadius = CornerRadius(16f * density, 16f * density)
    )
    
    // Draw peaceful head with a small breathing offset
    drawCircle(
      color = Color(0xFFFFDAB9), // Warm soft skin tone
      radius = 22f * density,
      center = Offset(w * 0.42f, h * 0.35f - breathingWave * 3.5f)
    )

    // Peaceful closed eye arc
    val eyePath = Path().apply {
      moveTo(w * 0.40f, h * 0.35f - breathingWave * 3.5f)
      quadraticTo(
        w * 0.42f, h * 0.37f - breathingWave * 3.5f,
        w * 0.44f, h * 0.35f - breathingWave * 3.5f
      )
    }
    drawPath(
      path = eyePath,
      color = QuietCharcoal.copy(alpha = 0.7f),
      style = Stroke(width = 2.5f * density, pathEffect = PathEffect.cornerPathEffect(4f))
    )

    // Folded arms / resting leg arcs
    drawRoundRect(
      color = SoftSkyBlue.copy(alpha = 0.85f),
      topLeft = Offset(w * 0.26f, h * 0.65f),
      size = Size(w * 0.25f, h * 0.08f),
      cornerRadius = CornerRadius(10f * density, 10f * density)
    )

    // 3. DRAW GLOWING COMPANION (Cute floating heart/cloud spirit)
    val companionCenter = Offset(w * 0.68f, h * 0.52f + floatY)
    
    // Glow rings backing
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          SoftLavender.copy(alpha = 0.4f * (1f + breathingWave * 0.15f)),
          Color.Transparent
        ),
        center = companionCenter,
        radius = 55f * density
      )
    )

    // Drawn cloud/heart shape body
    val spiritPath = Path().apply {
      val cx = companionCenter.x
      val cy = companionCenter.y
      val rad = 32f * density
      
      moveTo(cx, cy - rad * 0.2f)
      // Left lobe
      cubicTo(
        cx - rad * 0.7f, cy - rad * 1.1f,
        cx - rad * 1.5f, cy - rad * 0.2f,
        cx - rad, cy + rad * 0.5f
      )
      // Tail speech arc towards the person
      quadraticTo(
        cx - rad * 0.4f, cy + rad * 0.9f,
        cx - rad * 1.2f, cy + rad * 1.2f
      )
      quadraticTo(
        cx - rad * 0.2f, cy + rad * 0.9f,
        cx, cy + rad * 0.8f
      )
      // Right lobe
      cubicTo(
        cx + rad * 1.5f, cy + rad * 0.1f,
        cx + rad * 0.9f, cy - rad * 1.1f,
        cx, cy - rad * 0.2f
      )
      close()
    }

    drawPath(
      path = spiritPath,
      color = Color.White
    )

    drawPath(
      path = spiritPath,
      brush = Brush.linearGradient(
        colors = listOf(SoftLavender, SoftSkyBlue)
      ),
      style = Stroke(width = 3.5f * density)
    )

    // Cute face for the companion
    val faceY = companionCenter.y + 2f * density
    // Peaceful eyes
    drawCircle(
      color = QuietCharcoal,
      radius = 2.5f * density,
      center = Offset(companionCenter.x - 8f * density, faceY)
    )
    drawCircle(
      color = QuietCharcoal,
      radius = 2.5f * density,
      center = Offset(companionCenter.x + 8f * density, faceY)
    )
    // Tiny happy cheek blushes
    drawCircle(
      color = Color(0xFFFF9494).copy(alpha = 0.5f),
      radius = 4f * density,
      center = Offset(companionCenter.x - 12f * density, faceY + 4f * density)
    )
    drawCircle(
      color = Color(0xFFFF9494).copy(alpha = 0.5f),
      radius = 4f * density,
      center = Offset(companionCenter.x + 12f * density, faceY + 4f * density)
    )
    // Smiling mouth arc
    val mouthPath = Path().apply {
      moveTo(companionCenter.x - 3f * density, faceY + 3f * density)
      quadraticTo(
        companionCenter.x, faceY + 6f * density,
        companionCenter.x + 3f * density, faceY + 3f * density
      )
    }
    drawPath(
      path = mouthPath,
      color = QuietCharcoal,
      style = Stroke(width = 2f * density, pathEffect = PathEffect.cornerPathEffect(2f))
    )

    // Slow ambient connection sparks moving from companion to the person
    for (i in 0..4) {
      val progress = ((time * 0.05f + i * 0.25f) % 1f)
      val x = w * 0.68f - progress * (w * 0.26f)
      // Gentle arching height path
      val y = (h * 0.52f + floatY) - sin(progress * Math.PI.toFloat()) * 40f
      val alphaVal = if (progress < 0.2f) progress / 0.2f else if (progress > 0.8f) (1f - progress) / 0.2f else 1f
      
      drawCircle(
        color = SoftLavender.copy(alpha = alphaVal * 0.6f),
        radius = (3f + sin(time * 0.2f + i) * 1.5f) * density,
        center = Offset(x, y)
      )
    }
  }
}

/**
 * Screen 2 Illustration: Mood entries transforming into a positive emotional journey.
 */
@Composable
fun AnimatedEmotionalJourneyIllustration(time: Float) {
  val density = LocalDensity.current.density

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .testTag("emotional_journey_illustration")
  ) {
    val w = size.width
    val h = size.height

    // Radial backing gradient Representing calm dawn
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          SoftSkyBlue.copy(alpha = 0.24f),
          Color.Transparent
        ),
        center = Offset(w * 0.5f, h * 0.45f),
        radius = w * 0.45f
      )
    )

    // Wave/Pathway representing the Emotional Journey
    val pathPoints = List(40) { index ->
      val x = w * 0.15f + (w * 0.7f * index / 39f)
      // Upward arching path that loops down and goes even higher (representing healing journey)
      val param = (index / 39f) * Math.PI.toFloat() * 1.8f
      val y = h * 0.75f - (index / 39f) * (h * 0.4f) + sin(param + time * 0.03f) * 20f
      Offset(x, y)
    }

    val journeyPath = Path().apply {
      moveTo(pathPoints[0].x, pathPoints[0].y)
      for (i in 1 until pathPoints.size) {
        lineTo(pathPoints[i].x, pathPoints[i].y)
      }
    }

    // Outer thick path glow
    drawPath(
      path = journeyPath,
      color = SoftSkyBlue.copy(alpha = 0.25f),
      style = Stroke(width = 12f * density, pathEffect = PathEffect.cornerPathEffect(16f))
    )

    // Solid precise stream line
    drawPath(
      path = journeyPath,
      brush = Brush.linearGradient(
        colors = listOf(SoftSkyBlue, SoftLavender, SageGreen)
      ),
      style = Stroke(
        width = 4.5f * density,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f * density, 10f * density), time * 0.4f)
      )
    )

    // Drawn Emotional Landmarks representing mood entries becoming clearer/calmer
    val landmarks = listOf(
      Triple("Tired", Offset(w * 0.22f, h * 0.72f), Color(0xFFFFCCA3)),
      Triple("Anxious", Offset(w * 0.43f, h * 0.56f), Color(0xFFC4D5FF)),
      Triple("Calm", Offset(w * 0.63f, h * 0.42f), Color(0xFFE1F7E3)),
      Triple("Joyful", Offset(w * 0.78f, h * 0.26f), Color(0xFFFFF6C3))
    )

    landmarks.forEachIndexed { index, mark ->
      val floatOffset = sin(time * 0.07f + index) * 5f * density

      // Glow behind landmarks
      drawCircle(
        color = mark.third.copy(alpha = 0.6f),
        radius = 24f * density,
        center = Offset(mark.second.x, mark.second.y + floatOffset)
      )

      // Ring border
      drawCircle(
        color = Color.White,
        radius = 18f * density,
        center = Offset(mark.second.x, mark.second.y + floatOffset)
      )

      drawCircle(
        color = mark.third,
        radius = 15f * density,
        center = Offset(mark.second.x, mark.second.y + floatOffset),
        style = Stroke(width = 3.5f * density)
      )

      // Draw beautiful stylized simplified emotion visual symbols
      val cx = mark.second.x
      val cy = mark.second.y + floatOffset
      val r = 8f * density

      when (index) {
        0 -> { // Tired (Drooping peaceful eyelid arc)
          val tiredPath = Path().apply {
            moveTo(cx - r, cy)
            quadraticTo(cx, cy + r * 0.8f, cx + r, cy)
          }
          drawPath(path = tiredPath, color = QuietCharcoal, style = Stroke(width = 2.5f * density))
        }
        1 -> { // Anxious (Two small listening focus stars/sparkles)
          drawCircle(color = QuietCharcoal, radius = 2.5f * density, center = Offset(cx - 3.5f * density, cy))
          drawCircle(color = QuietCharcoal, radius = 2.5f * density, center = Offset(cx + 3.5f * density, cy))
        }
        2 -> { // Calm (Soothing heart or horizontal infinite loop)
          val calmPath = Path().apply {
            moveTo(cx - r, cy)
            quadraticTo(cx - r * 0.4f, cy - r * 0.6f, cx, cy)
            quadraticTo(cx + r * 0.4f, cy + r * 0.6f, cx + r, cy)
          }
          drawPath(path = calmPath, color = QuietCharcoal, style = Stroke(width = 2.5f * density))
        }
        3 -> { // Joyful (Sparkling diamond burst)
          val sparkle = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.5f, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.5f, cy)
            close()
          }
          drawPath(path = sparkle, color = QuietCharcoal)
        }
      }
    }
  }
}

/**
 * Screen 3 Illustration: A companion companion evolving and growing alongside the user.
 */
@Composable
fun AnimatedCompanionGrowthIllustration(time: Float) {
  val density = LocalDensity.current.density

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .testTag("companion_growth_illustration")
  ) {
    val w = size.width
    val h = size.height

    // Radial backing represent positive solar energy / brightness
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          SageGreen.copy(alpha = 0.22f),
          Color.Transparent
        ),
        center = Offset(w * 0.5f, h * 0.45f),
        radius = w * 0.45f
      )
    )

    // Growth stage controlled sequentially by time (or continuous looping sequence showing sprout -> bud -> evolved angel spirit)
    val loopCycle = (time * 0.05f) % 3f
    val stage = loopCycle.toInt() // 0: Sprout, 1: Bud, 2: Sparkly Angel Cloud

    val bouncePhase = sin(time * 0.08f) * 6f * density
    val centerPos = Offset(w * 0.5f, h * 0.5f + bouncePhase)

    // Draw Sanctuary ground pot
    val baseWidth = 90f * density
    drawRoundRect(
      color = Color(0xFFCFD8DC),
      topLeft = Offset(w * 0.5f - baseWidth * 0.5f, h * 0.72f),
      size = Size(baseWidth, 18f * density),
      cornerRadius = CornerRadius(10f * density, 10f * density)
    )

    when (stage) {
      0 -> { // SPROUT STATE
        // Draw magical soil base
        drawCircle(
          color = Color(0xFF8D6E63),
          radius = 18f * density,
          center = Offset(w * 0.5f, h * 0.72f)
        )
        // Green stem curve
        val stem = Path().apply {
          moveTo(w * 0.5f, h * 0.72f)
          quadraticTo(w * 0.48f, h * 0.62f + bouncePhase, centerPos.x, centerPos.y)
        }
        drawPath(path = stem, color = SageGreen, style = Stroke(width = 4.5f * density))

        // Cute dual sprouting leaves
        val leaf1 = Path().apply {
          moveTo(centerPos.x, centerPos.y)
          quadraticTo(centerPos.x - 22f * density, centerPos.y - 12f * density, centerPos.x - 14f * density, centerPos.y - 24f * density)
          quadraticTo(centerPos.x - 4f * density, centerPos.y - 14f * density, centerPos.x, centerPos.y)
        }
        drawPath(path = leaf1, color = SageGreen)

        val leaf2 = Path().apply {
          moveTo(centerPos.x, centerPos.y)
          quadraticTo(centerPos.x + 22f * density, centerPos.y - 12f * density, centerPos.x + 14f * density, centerPos.y - 24f * density)
          quadraticTo(centerPos.x + 4f * density, centerPos.y - 14f * density, centerPos.x, centerPos.y)
        }
        drawPath(path = leaf2, color = SageGreen)

        // Floating sparkles
        drawCircle(color = SoftSkyBlue.copy(alpha = 0.8f), radius = 3.5f * density, center = Offset(centerPos.x - 30f * density, centerPos.y - 30f * density))
        drawCircle(color = SoftSkyBlue.copy(alpha = 0.8f), radius = 2f * density, center = Offset(centerPos.x + 30f * density, centerPos.y - 15f * density))
      }
      1 -> { // BUD / SHELL STATE
        // Draw stem
        val stem = Path().apply {
          moveTo(w * 0.5f, h * 0.72f)
          quadraticTo(w * 0.51f, h * 0.64f, centerPos.x, centerPos.y)
        }
        drawPath(path = stem, color = SageGreen, style = Stroke(width = 5f * density))

        // Bud shape bulb
        drawCircle(
          color = SoftLavender,
          radius = 24f * density,
          center = centerPos
        )
        // Wrapped outer protection leaves
        drawCircle(
          color = SageGreen,
          radius = 25f * density,
          center = centerPos,
          style = Stroke(width = 3.5f * density)
        )

        // Cute mini face peek-a-boo
        drawCircle(color = QuietCharcoal, radius = 2.5f * density, center = Offset(centerPos.x - 6f * density, centerPos.y - 2f * density))
        drawCircle(color = QuietCharcoal, radius = 2.5f * density, center = Offset(centerPos.x + 6f * density, centerPos.y - 2f * density))
        val smile = Path().apply {
          moveTo(centerPos.x - 2f * density, centerPos.y + 3f * density)
          quadraticTo(centerPos.x, centerPos.y + 5f * density, centerPos.x + 2f * density, centerPos.y + 3f * density)
        }
        drawPath(path = smile, color = QuietCharcoal, style = Stroke(width = 2f * density))
      }
      2 -> { // FULLY GROWN EVOLVED SPIRIT (Crowned glowing happy companion with fluttering wings)
        val cy = centerPos.y - 10f * density

        // Magical companion halo glow
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              SoftLavender.copy(alpha = 0.5f),
              Color.Transparent
            ),
            center = Offset(centerPos.x, cy),
            radius = 65f * density
          )
        )

        // Left fluttering wing
        val wingLeft = Path().apply {
          moveTo(centerPos.x - 14f * density, cy)
          cubicTo(
            centerPos.x - 48f * density, cy - 22f * density,
            centerPos.x - 52f * density, cy + 18f * density,
            centerPos.x - 14f * density, cy + 12f * density
          )
          close()
        }
        drawPath(path = wingLeft, color = SoftSkyBlue.copy(alpha = 0.6f))

        // Right wing
        val wingRight = Path().apply {
          moveTo(centerPos.x + 14f * density, cy)
          cubicTo(
            centerPos.x + 48f * density, cy - 22f * density,
            centerPos.x + 52f * density, cy + 18f * density,
            centerPos.x + 14f * density, cy + 12f * density
          )
          close()
        }
        drawPath(path = wingRight, color = SoftSkyBlue.copy(alpha = 0.6f))

        // Cloud body
        drawCircle(color = Color.White, radius = 25f * density, center = Offset(centerPos.x, cy))
        drawCircle(color = Color.White, radius = 17f * density, center = Offset(centerPos.x - 18f * density, cy + 4f * density))
        drawCircle(color = Color.White, radius = 17f * density, center = Offset(centerPos.x + 18f * density, cy + 4f * density))

        // Premium outer lining
        drawCircle(
          color = SoftLavender,
          radius = 26f * density,
          center = Offset(centerPos.x, cy),
          style = Stroke(width = 3.5f * density)
        )

        // Joyful face (winking happy expression)
        drawCircle(color = QuietCharcoal, radius = 3f * density, center = Offset(centerPos.x + 8f * density, cy))
        // Wink curve
        val wink = Path().apply {
          moveTo(centerPos.x - 11f * density, cy - 1f * density)
          quadraticTo(centerPos.x - 7f * density, cy - 4f * density, centerPos.x - 3f * density, cy - 1f * density)
        }
        drawPath(path = wink, color = QuietCharcoal, style = Stroke(width = 3f * density))

        // Rose blushing cheeks
        drawCircle(color = Color(0xFFFF8B8B), radius = 4f * density, center = Offset(centerPos.x - 13f * density, cy + 6f * density))
        drawCircle(color = Color(0xFFFF8B8B), radius = 4f * density, center = Offset(centerPos.x + 13f * density, cy + 6f * density))

        // Golden sprout crown over head
        val crown = Path().apply {
          moveTo(centerPos.x, cy - 25f * density)
          lineTo(centerPos.x - 7f * density, cy - 35f * density)
          lineTo(centerPos.x, cy - 30f * density)
          lineTo(centerPos.x + 7f * density, cy - 35f * density)
          close()
        }
        drawPath(path = crown, color = Color(0xFFFFD54F))
      }
    }
  }
}
