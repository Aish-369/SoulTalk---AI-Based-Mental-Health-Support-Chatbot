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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppContainer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Three distinct animation states for our visual procedural companion representation.
 */
enum class CompanionAnimState {
  Idle,
  Smile,
  SuccessJump
}

@Composable
fun SoulTalkLoginSignupScreen(onAuthSucceeded: () -> Unit) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val hapticFeedback = LocalHapticFeedback.current

  // Container repositories
  val companionRepo = remember { AppContainer.getRepository(context) }
  val authRepo = remember { AppContainer.getAuthRepository(context) }

  // Load selected companion details from SQLite context
  val localUser by companionRepo.userFlow.collectAsState(initial = null)
  val companionId = localUser?.companion_type ?: "mochi_cat"
  val companionName = localUser?.companion_name ?: "Mochi"

  // Credentials and visual state triggers
  var isRegisterMode by remember { mutableStateOf(false) }
  var nameInput by remember { mutableStateOf("") }
var emailInput by remember { mutableStateOf("") }
var passwordInput by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var isAuthenticating by remember { mutableStateOf(false) }
  var authErrorMessage by remember { mutableStateOf("") }

  // Animated states for the companion model
  var companionAnimState by remember { mutableStateOf(CompanionAnimState.Idle) }

  // 60FPS fluid continuous clock
  val continuousTransition = rememberInfiniteTransition(label = "login_clock")
  val continuousTime by continuousTransition.animateFloat(
    initialValue = 0f,
    targetValue = 200f * Math.PI.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(60000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "float_phase"
  )

  // Staggered page load animations state variables
  var animateBaseFade by remember { mutableStateOf(false) }
  var animateCompanionShow by remember { mutableStateOf(false) }
  var animateSpeechBubbleShow by remember { mutableStateOf(false) }
  var animateFormSlideIn by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    // Elegant staggered orchestrations
    animateBaseFade = true
    delay(200)
    animateCompanionShow = true
    delay(400)
    animateSpeechBubbleShow = true
    delay(300)
    animateFormSlideIn = true
  }

  // Handle successful login micro-animations
  val triggerSuccessfulFlow: () -> Unit = {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    companionAnimState = CompanionAnimState.SuccessJump
    coroutineScope.launch {
      delay(1500) // Delay to enjoy the beautiful happy success animation jump
      onAuthSucceeded()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CalmingBackground)
      .testTag("login_signup_screen_root")
  ) {
    // Ambient floating background particles
    BackgroundWaterParticles(time = continuousTime)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // COMPANION SECTOR WITH STAGGERED FADE-IN
      AnimatedVisibility(
        visible = animateCompanionShow,
        enter = fadeIn(animationSpec = tween(600)) + expandVertically(animationSpec = tween(650)),
        exit = fadeOut()
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Adaptive Companion visual name tag
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(SageGreen.copy(alpha = 0.12f))
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = "💖 Carrying ${companionName.uppercase()}",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = SageGreen,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // LARGE ANIMATED RENDER CONTAINER
          Box(
            modifier = Modifier
              .size(160.dp)
              .testTag("companion_login_render_container"),
            contentAlignment = Alignment.Center
          ) {
            // Soft Radial Background Pulse Shadow
            Box(
              modifier = Modifier
                .fillMaxSize()
                .scale(if (companionAnimState == CompanionAnimState.Smile) 1.25f else 1.0f)
                .background(SageGreen.copy(alpha = 0.05f), CircleShape)
            )

            Box(modifier = Modifier.size(114.dp)) {
              AnimatedLoginCompanionDraw(
                companionId = companionId,
                animState = companionAnimState,
                time = continuousTime
              )
            }
          }
        }
      }

      // WELCOME MESSAGE IN SPEECH BUBBLE
      AnimatedVisibility(
        visible = animateSpeechBubbleShow,
        enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + scaleIn(),
        exit = fadeOut()
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          SpeechBubble(
            text = if (isRegisterMode) {
              "Hi there 👋 Let's connect you securely..."
            } else {
              "Welcome back 👋 I'm excited to start this journey with you."
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // CORE HEAVY BRANDING
      Text(
        text = "Welcome To SoulTalk",
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        color = QuietCharcoal,
        modifier = Modifier.testTag("auth_welcome_title")
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Let's create your safe space.",
        fontFamily = NotoSansDevanagariFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = SoftSlate,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // AUTHENTICATION CONTENT FORM SLIDE-UP
      AnimatedVisibility(
        visible = animateFormSlideIn,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
          initialOffsetY = { it / 3 },
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut()
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Google Login Button
          ElevatedButton(
            onClick = {
              if (isAuthenticating) return@ElevatedButton
              isAuthenticating = true
              authErrorMessage = ""
              companionAnimState = CompanionAnimState.Smile
              hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

              coroutineScope.launch {
                delay(1200)
                val success = authRepo.loginWithGoogle("google_mock_id_token")
                isAuthenticating = false
                if (success) {
                  triggerSuccessfulFlow()
                } else {
                  companionAnimState = CompanionAnimState.Idle
                  authErrorMessage = "Failed to launch Google authenticated framework."
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("continue_with_google_button"),
            colors = ButtonDefaults.elevatedButtonColors(
              containerColor = PureWhite,
              contentColor = QuietCharcoal
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(
              defaultElevation = 2.dp,
              pressedElevation = 6.dp
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()
            ) {
              GoogleLogoVectorIcon()
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Continue with Google",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // CUSTOM EMOTIONAL OR SEPARATOR BLOCK
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(QuietCharcoal.copy(alpha = 0.1f))
            )
            Text(
              text = "OR",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = SoftSlate,
              modifier = Modifier.padding(horizontal = 16.dp),
              letterSpacing = 1.sp
            )
            Box(
              modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(QuietCharcoal.copy(alpha = 0.1f))
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // INPUT FIELDS WITH COZY TEXTFIELDS SAGE SENSITIVITY
          if (isRegisterMode) {
            // Full name registration input
            Text(
              text = "Full Name",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = QuietCharcoal,
              modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            OutlinedTextField(
              value = nameInput,
              onValueChange = { nameInput = it },
              leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SageGreen) },
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("full_name_input"),
              placeholder = {
    Text(
        "E.g. Aishwarya",
        color = SoftSlate.copy(alpha = 0.6f)
    )
}
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SageGreen,
                unfocusedBorderColor = QuietCharcoal.copy(alpha = 0.15f),
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite
              ),
              singleLine = true
            )
          }

          // Email Input field
          Text(
            text = "Email Address",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = QuietCharcoal,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
          )

          OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = SageGreen) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp)
              .testTag("email_input"),
            placeholder = { Text("yourname@example.com", color = SoftSlate.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SageGreen,
              unfocusedBorderColor = QuietCharcoal.copy(alpha = 0.15f),
              focusedContainerColor = PureWhite,
              unfocusedContainerColor = PureWhite
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
          )

          // Password Input field & Visibility Toggles
          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Password",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = QuietCharcoal,
              modifier = Modifier.padding(start = 4.dp)
            )

            if (!isRegisterMode) {
              Text(
                text = "Forgot Password?",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = SoftSkyBlue,
                modifier = Modifier
                  .clickable {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    authErrorMessage = "A restful password recovery link has been simulated for your device."
                  }
                  .padding(end = 4.dp)
              )
            }
          }

          OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SageGreen) },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Text(
                  text = if (isPasswordVisible) "HIDE" else "SHOW",
                  fontFamily = PoppinsFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = SageGreen
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp)
              .testTag("password_input"),
            placeholder = { Text("At least 6 characters", color = SoftSlate.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SageGreen,
              unfocusedBorderColor = QuietCharcoal.copy(alpha = 0.15f),
              focusedContainerColor = PureWhite,
              unfocusedContainerColor = PureWhite
            ),
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
          )

          if (authErrorMessage.isNotEmpty()) {
            Text(
              text = authErrorMessage,
              fontFamily = NotoSansDevanagariFamily,
              fontSize = 13.sp,
              color = Color(0xFFE57373),
              modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
              textAlign = TextAlign.Start
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // AUTH SUBMIT BUTTON
          ElevatedButton(
            onClick = {
              if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                authErrorMessage = "Please complete email and password requirements."
                return@ElevatedButton
              }
              isAuthenticating = true
              authErrorMessage = ""
              companionAnimState = CompanionAnimState.Smile
              hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

              coroutineScope.launch {
                delay(1000)
                val success = if (isRegisterMode) {
                  authRepo.registerWithPassword(nameInput, emailInput, passwordInput, "en")
                } else {
                  authRepo.loginWithPassword(emailInput, passwordInput)
                }
                isAuthenticating = false
                if (success) {
                  triggerSuccessfulFlow()
                } else {
                  companionAnimState = CompanionAnimState.Idle
                  authErrorMessage = "Security validation offline or invalid parameters."
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("auth_primary_submit_button"),
            colors = ButtonDefaults.elevatedButtonColors(
              containerColor = SageGreen,
              contentColor = PureWhite,
              disabledContainerColor = SageGreen.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(
              defaultElevation = 4.dp,
              pressedElevation = 8.dp
            ),
            enabled = !isAuthenticating
          ) {
            if (isAuthenticating) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = PureWhite,
                strokeWidth = 2.5.dp
              )
            } else {
              Text(
                text = if (isRegisterMode) "Create Account" else "Sign In",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 0.3.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // SWIFT FOOTER TO CHOOSE MODE
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isRegisterMode) "Already have an account?" else "New to SoulTalk?",
              fontFamily = NotoSansDevanagariFamily,
              color = SoftSlate,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isRegisterMode) "Sign In" else "Sign Up",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              color = SageGreen,
              fontSize = 14.sp,
              modifier = Modifier
                .clickable {
                  hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  isRegisterMode = !isRegisterMode
                  authErrorMessage = ""
                }
                .testTag("toggle_auth_mode_button")
            )
          }

          Spacer(modifier = Modifier.height(28.dp))

          // BEAUTIFUL SOFT CARD PRIVACY ASSURANCE SECTOR
          PrivacyAssuranceSection()

          Spacer(modifier = Modifier.height(32.dp))
        }
      }
    }
  }
}

/**
 * Beautiful vector presentation of a Speech Bubble indicating personal connection.
 */
@Composable
fun SpeechBubble(text: String) {
  val bubbleShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val r = 16.dp.value
    addRoundRect(androidx.compose.ui.geometry.RoundRect(rect = androidx.compose.ui.geometry.Rect(0f, 0f, w, h - 8.dp.value), cornerRadius = CornerRadius(r, r)))
    // Sweet triangle peak on bottom middle
    moveTo(w / 2f - 10.dp.value, h - 8.dp.value)
    lineTo(w / 2f, h)
    lineTo(w / 2f + 10.dp.value, h - 8.dp.value)
    close()
  }

  Column(
    modifier = Modifier
      .padding(horizontal = 20.dp, vertical = 6.dp)
      .fillMaxWidth(0.88f)
      .shadow(elevation = 2.dp, shape = bubbleShape)
      .background(Color(0xFFE8F5E9), bubbleShape) // Comforting SageGlow backing
      .border(1.dp, SageGreen.copy(alpha = 0.2f), bubbleShape)
      .padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = text,
      fontFamily = NotoSansDevanagariFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 13.5.sp,
      color = QuietCharcoal,
      textAlign = TextAlign.Center,
      lineHeight = 18.2.sp
    )
  }
}

/**
 * Simple beautiful vector representing Google's symbol outline to maintain premium styling.
 */
@Composable
fun GoogleLogoVectorIcon() {
  Canvas(modifier = Modifier.size(20.dp)) {
    val r = size.width / 2f
    val strokeW = 3.5f
    // Draw outer G sector curves
    drawArc(
      color = Color(0xFFEA4335), // Top Curve Red
      startAngle = 180f,
      sweepAngle = 120f,
      useCenter = false,
      topLeft = Offset(strokeW, strokeW),
      size = Size(size.width - strokeW*2, size.height - strokeW*2),
      style = Stroke(strokeW)
    )
    drawArc(
      color = Color(0xFFA7C7E7), // Right bar / Blue sector
      startAngle = 300f,
      sweepAngle = 110f,
      useCenter = false,
      topLeft = Offset(strokeW, strokeW),
      size = Size(size.width - strokeW*2, size.height - strokeW*2),
      style = Stroke(strokeW)
    )
    drawArc(
      color = Color(0xFF34A853), // Bottom green curve
      startAngle = 50f,
      sweepAngle = 130f,
      useCenter = false,
      topLeft = Offset(strokeW, strokeW),
      size = Size(size.width - strokeW*2, size.height - strokeW*2),
      style = Stroke(strokeW)
    )
    // Horizontal alignment bar
    drawLine(
      color = Color(0xFFA7C7E7),
      start = Offset(r, r),
      end = Offset(size.width - strokeW, r),
      strokeWidth = strokeW
    )
  }
}

/**
 * Modern startup soft-card ensuring conversational and emotional data stays 100% secure.
 */
@Composable
fun PrivacyAssuranceSection() {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("privacy_assurance_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(18.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = SageGreen,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Your Privacy Shield",
          fontFamily = PoppinsFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = QuietCharcoal
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      val bulletPoints = listOf(
        "Your conversations stay private.",
        "Your emotional data is fully secure.",
        "You retain full control over your information at all times."
      )

      bulletPoints.forEach { text ->
        Row(
          modifier = Modifier.padding(vertical = 4.dp),
          verticalAlignment = Alignment.Top
        ) {
          Text(text = "🔒 ", fontSize = 13.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = text,
            fontFamily = NotoSansDevanagariFamily,
            fontSize = 12.5.sp,
            color = SoftSlate,
            lineHeight = 17.sp
          )
        }
      }
    }
  }
}

/**
 * Dynamic drifting backgrounds indicating atmospheric safety and living visual state.
 */
@Composable
fun BackgroundWaterParticles(time: Float) {
  val density = LocalDensity.current.density
  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    // 5 cute floating sage and sky circles drifting slowly
    val particles = listOf(
      Offset(w * 0.15f + cos(time * 0.08f) * 40f, h * 0.25f + sin(time * 0.11f) * 60f),
      Offset(w * 0.85f - sin(time * 0.09f) * 35f, h * 0.18f + cos(time * 0.07f) * 50f),
      Offset(w * 0.22f + sin(time * 0.12f) * 50f, h * 0.72f + cos(time * 0.1f) * 60f),
      Offset(w * 0.78f + cos(time * 0.07f) * 45f, h * 0.82f - sin(time * 0.13f) * 50f),
      Offset(w * 0.5f - cos(time * 0.05f) * 60f, h * 0.55f + sin(time * 0.08f) * 45f)
    )

    particles.forEachIndexed { i, offset ->
      val pSize = (16f + (i * 8f)) * density
      val pColor = if (i % 2 == 0) SageGreen.copy(alpha = 0.035f) else SoftSkyBlue.copy(alpha = 0.035f)
      drawCircle(
        color = pColor,
        radius = pSize,
        center = offset
      )
    }
  }
}

/**
 * Production procedural graphics system handling idle/smile/happy-jumping state animations.
 */
@Composable
fun AnimatedLoginCompanionDraw(
  companionId: String,
  animState: CompanionAnimState,
  time: Float
) {
  val density = LocalDensity.current.density
  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    // Loop components
    val blink = ((time * 0.05f) % 2f) < 0.12f
    val tailSway = sin(time * 0.14f)

    // Evaluate jumping coordinates upon Auth completion
    val jumpAmplitude = if (animState == CompanionAnimState.SuccessJump) {
      val t = (time * 6.5f) % (2f * Math.PI.toFloat())
      // Smooth sinusoidal jump bounce
      -abs(sin(t)) * 25f * density
    } else {
      0f
    }

    // Gentle float on idle
    val floatY = if (animState != CompanionAnimState.SuccessJump) {
      cos(time * 0.12f) * 6f * density
    } else {
      0f
    }

    val cx = w / 2f
    val cy = h / 2f + floatY + jumpAmplitude
    val breathe = sin(time * 0.08f) * 2.5f * density

    // Big special blush / smile multiplier for Google login moments
    val smilingSmile = animState == CompanionAnimState.Smile || animState == CompanionAnimState.SuccessJump

    when (companionId) {
      "mochi_cat" -> {
        // EAR LEFT
        val earLeft = Path().apply {
          moveTo(cx - 36f * density, cy - 24f * density)
          lineTo(cx - 46f * density, cy - 54f * density + (tailSway * 3f))
          lineTo(cx - 16f * density, cy - 36f * density)
          close()
        }
        drawPath(path = earLeft, color = PureWhite)
        drawPath(path = earLeft, color = Color(0xFFFFCDD2)) // pink inner

        // EAR RIGHT
        val earRight = Path().apply {
          moveTo(cx + 36f * density, cy - 24f * density)
          lineTo(cx + 46f * density, cy - 54f * density - (tailSway * 3f))
          lineTo(cx + 16f * density, cy - 36f * density)
          close()
        }
        drawPath(path = earRight, color = PureWhite)
        drawPath(path = earRight, color = Color(0xFFFFCDD2))

        // TAIL
        drawCircle(
          color = PureWhite,
          radius = 12f * density,
          center = Offset(cx + 38f * density + tailSway * 8f, cy + 24f * density)
        )

        // BODY
        drawCircle(color = PureWhite, radius = 38f * density + breathe, center = Offset(cx, cy))

        // EYES
        val eyeY = cy - 2f * density
        if (!blink && !smilingSmile) {
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx - 14f * density, eyeY))
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx + 14f * density, eyeY))
        } else {
          // Curved happy closed arcs
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - 19f * density, eyeY - 4f * density), size = Size(10f * density, 8f * density),
            style = Stroke(2.5f * density)
          )
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx + 9f * density, eyeY - 4f * density), size = Size(10f * density, 8f * density),
            style = Stroke(2.5f * density)
          )
        }

        // CHEEKS BLUSH (Glow extra large when smiling!)
        val cheekGlowRadius = if (smilingSmile) 9f * density else 5.5f * density
        drawCircle(color = Color(0xFFFF9494).copy(alpha = 0.5f), radius = cheekGlowRadius, center = Offset(cx - 20f * density, cy + 6f * density))
        drawCircle(color = Color(0xFFFF9494).copy(alpha = 0.5f), radius = cheekGlowRadius, center = Offset(cx + 20f * density, cy + 6f * density))

        // Cute smiling tiny mouth
        drawCircle(color = Color(0xFFE57373), radius = 2.5f * density, center = Offset(cx, cy + 3f * density))
        val mouthDetails = Path().apply {
          moveTo(cx - 4f * density, cy + 7f * density)
          quadraticTo(cx - 2f * density, cy + (if (smilingSmile) 12f else 10f) * density, cx, cy + 7f * density)
          quadraticTo(cx + 2f * density, cy + (if (smilingSmile) 12f else 10f) * density, cx + 4f * density, cy + 7f * density)
        }
        drawPath(path = mouthDetails, color = QuietCharcoal, style = Stroke(2f * density))
      }

      "buddy_dog" -> {
        // BODY / HEAD
        drawCircle(color = Color(0xFFF5C27A), radius = 35f * density + breathe, center = Offset(cx, cy))

        // SNOUT
        drawRoundRect(
          color = Color(0xFFFFE0B2),
          topLeft = Offset(cx - 16f * density, cy),
          size = Size(32f * density, 20f * density),
          cornerRadius = CornerRadius(10f * density, 10f * density)
        )
        // NOSE
        drawRoundRect(
          color = QuietCharcoal,
          topLeft = Offset(cx - 5f * density, cy + 2f * density),
          size = Size(10f * density, 6f * density),
          cornerRadius = CornerRadius(3f * density, 3f * density)
        )

        // TONGUE active wag panting
        val tongueAmp = if (smilingSmile) 9f * density else (Math.abs(sin(time * 0.16f)) * 5f * density)
        drawRoundRect(
          color = Color(0xFFFF8A80),
          topLeft = Offset(cx - 4.5f * density, cy + 10f * density),
          size = Size(9f * density, 8f * density + tongueAmp),
          cornerRadius = CornerRadius(4.5f * density, 4.5f * density)
        )

        // EAR LEFT
        val earOffset = sin(time * 0.12f) * 4f * density
        drawRoundRect(
          color = Color(0xFFCE934A),
          topLeft = Offset(cx - 48f * density, cy - 20f * density + earOffset),
          size = Size(16f * density, 38f * density),
          cornerRadius = CornerRadius(8f * density, 8f * density)
        )
        // EAR RIGHT
        drawRoundRect(
          color = Color(0xFFCE934A),
          topLeft = Offset(cx + 32f * density, cy - 20f * density - earOffset),
          size = Size(16f * density, 38f * density),
          cornerRadius = CornerRadius(8f * density, 8f * density)
        )

        // EYES
        val eyeY = cy - 4f * density
        if (!blink && !smilingSmile) {
          drawCircle(color = QuietCharcoal, radius = 4f * density, center = Offset(cx - 12f * density, eyeY))
          drawCircle(color = QuietCharcoal, radius = 4f * density, center = Offset(cx + 12f * density, eyeY))
          drawCircle(color = Color.White, radius = 1.2f * density, center = Offset(cx - 13.5f * density, eyeY - 1.5f * density))
          drawCircle(color = Color.White, radius = 1.2f * density, center = Offset(cx + 10.5f * density, eyeY - 1.5f * density))
        } else {
          // Closed smiling arches
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - 17f * density, eyeY - 5f * density), size = Size(10f * density, 8f * density),
            style = Stroke(3f * density)
          )
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx + 7f * density, eyeY - 5f * density), size = Size(10f * density, 8f * density),
            style = Stroke(3f * density)
          )
        }

        // COZY WAG TAIL
        val wagAngle = if (smilingSmile) (sin(time * 0.35f) * 16f) else (tailSway * 12f)
        val dogTail = Path().apply {
          moveTo(cx + 25f * density, cy + 20f * density)
          quadraticTo(cx + 42f * density, cy + 28f * density, cx + 50f * density + wagAngle * density, cy + 10f * density)
        }
        drawPath(path = dogTail, color = Color(0xFFF5C27A), style = Stroke(5f * density))
      }

      "nova_fox" -> {
        // EAR LEFT
        val earPointsL = Path().apply {
          moveTo(cx - 30f * density, cy - 20f * density)
          lineTo(cx - 40f * density, cy - 52f * density)
          lineTo(cx - 10f * density, cy - 30f * density)
          close()
        }
        drawPath(path = earPointsL, color = Color(0xFFE65100))
        drawPath(path = earPointsL, color = Color(0xFFFFCC80), style = Stroke(2f * density))

        // EAR RIGHT
        val earPointsR = Path().apply {
          moveTo(cx + 30f * density, cy - 20f * density)
          lineTo(cx + 40f * density, cy - 52f * density)
          lineTo(cx + 10f * density, cy - 30f * density)
          close()
        }
        drawPath(path = earPointsR, color = Color(0xFFE65100))
        drawPath(path = earPointsR, color = Color(0xFFFFCC80), style = Stroke(2f * density))

        // COZY TAIL
        val tailS = if (smilingSmile) (sin(time * 0.28f) * 10f) else (tailSway * 6f)
        val foxTail = Path().apply {
          moveTo(cx + 20f * density, cy + 20f * density)
          cubicTo(
            cx + 45f * density, cy + 10f * density,
            cx + 45f * density + tailS * density, cy - 24f * density,
            cx + 28f * density, cy - 28f * density
          )
        }
        drawPath(path = foxTail, color = Color(0xFFFF7043), style = Stroke(14f * density))
        drawCircle(color = PureWhite, radius = 6f * density, center = Offset(cx + 28f * density, cy - 28f * density))

        // FACE BASE
        val faceG = Path().apply {
          moveTo(cx - 35f * density, cy - 10f * density)
          quadraticTo(cx - 45f * density, cy + 10f * density, cx, cy + 25f * density)
          quadraticTo(cx + 45f * density, cy + 10f * density, cx + 35f * density, cy - 10f * density)
          quadraticTo(cx, cy - 24f * density, cx - 35f * density, cy - 10f * density)
        }
        drawPath(path = faceG, color = Color(0xFFFF7043))

        // CHEEKS
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

        // NOSE
        drawCircle(color = QuietCharcoal, radius = 3f * density, center = Offset(cx, cy + 20f * density))

        // EYES
        val eyeY = cy - 2f * density
        if (!blink && !smilingSmile) {
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx - 14f * density, eyeY))
          drawCircle(color = QuietCharcoal, radius = 3.5f * density, center = Offset(cx + 14f * density, eyeY))
        } else {
          // Beautiful happy slit loops
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - 18f * density, eyeY - 4f * density), size = Size(8f * density, 6f * density),
            style = Stroke(2.5f * density)
          )
          drawArc(
            color = QuietCharcoal,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx + 10f * density, eyeY - 4f * density), size = Size(8f * density, 6f * density),
            style = Stroke(2.5f * density)
          )
        }
      }

      else -> {
        // STATIC FALLBACK IF EXTENDED CHANNELS UNUSED
        drawCircle(color = SageGreen, radius = 35f * density + breathe, center = Offset(cx, cy))
        drawCircle(color = QuietCharcoal, radius = 4f * density, center = Offset(cx - 12f * density, cy - 4f * density))
        drawCircle(color = QuietCharcoal, radius = 4f * density, center = Offset(cx + 12f * density, cy - 4f * density))
        // Blush smile
        val happyMouth = Path().apply {
          moveTo(cx - 6f * density, cy + 6f * density)
          quadraticTo(cx, cy + (if (smilingSmile) 14f else 10f) * density, cx + 6f * density, cy + 6f * density)
        }
        drawPath(path = happyMouth, color = QuietCharcoal, style = Stroke(2.5f * density))
      }
    }
  }
}
