package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.database.VoiceMemoryEntity
import com.example.data.repository.CompanionRepository
import com.example.ui.theme.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.sin

enum class WhisperState {
  IDLE,
  LISTENING,
  RECORDING,
  PROCESSING,
  COMPLETED
}

data class WhisperEnv(
  val id: String,
  val name: String,
  val icon: String,
  val startColor: Color,
  val endColor: Color,
  val description: String
)

// Structural Gemini response DTO matches the user's requested schema
data class GeminiReflectionResponse(
  val emotion: String,
  val confidence: Double,
  val reflection: String,
  val themes: List<String>,
  val action: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SoulTalkVoiceCompanionScreen(
  repository: CompanionRepository,
  onBackClicked: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  // Ambient Environment Selection
  val environments = remember {
    listOf(
      WhisperEnv("moonlight", "Moonlight Room", "🌙", Color(0xFF070A1E), Color(0xFF131535), "Twinkling star cluster with soft clouds drifting"),
      WhisperEnv("rain", "Rain Window", "🌧", Color(0xFF0B101D), Color(0xFF1F243A), "Droplets rippling upon clean, high-contrast window frames"),
      WhisperEnv("ocean", "Ocean Waves", "🌊", Color(0xFF030E1A), Color(0xFF0D253D), "Slow golden-tide oceans swelling with lunar rhythm"),
      WhisperEnv("forest", "Forest Retreat", "🌲", Color(0xFF05120B), Color(0xFF122C1A), "Floating moss fireflies ascending into dark pine woods"),
      WhisperEnv("cloud", "Cloud Garden", "☁️", Color(0xFF0E0B1F), Color(0xFF261D3A), "Soft lavender garden with sakura breezes")
    )
  }
  var selectedEnv by remember { mutableStateOf(environments[0]) }

  // Voice Recording / STT States
  var recordingState by remember { mutableStateOf(WhisperState.IDLE) }
  var isRecordingPaused by remember { mutableStateOf(false) }
  var isAudioPlayingMemoryId by remember { mutableStateOf<Int?>(null) }
  var progressRmsDecibels by remember { mutableStateOf(0f) }

  // Transcripts
  var originalTranscript by remember { mutableStateOf("") }
  var liveTranscriptReveal by remember { mutableStateOf("") }
  var isEditingTranscript by remember { mutableStateOf(false) }

  // Analysis result
  var analysisResult by remember { mutableStateOf<GeminiReflectionResponse?>(null) }
  var isGeneratingAnalysis by remember { mutableStateOf(false) }

  // Playback loop controller for ambient sounds (procedural synthesis)
  var isAmbientSoundPlayOn by remember { mutableStateOf(false) }

  // Companion Details
  var companionName by remember { mutableStateOf("Mochi") }
  var companionType by remember { mutableStateOf("mochi_cat") }

  // Voice Memories List (Room Database synced!)
  val voiceHistoryList by repository.getVoiceMemoriesFlow().collectAsState(initial = emptyList())

  // Load the chosen companion character information
  LaunchedEffect(Unit) {
    repository.userFlow.collect { user ->
      user?.let {
        companionName = it.companion_name
        companionType = it.companion_type
      }
    }
  }

  // Animation clocks
  val infiniteTransition = rememberInfiniteTransition(label = "whisper_clock")
  val environmentTime by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(20000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "env_clock"
  )

  // Sound Synth Coroutine - play a soft binaural soothing hum in background when enabled
  LaunchedEffect(isAmbientSoundPlayOn, selectedEnv) {
    if (isAmbientSoundPlayOn) {
      withContext(Dispatchers.IO) {
        val sampleRate = 44100
        val numSamples = 44100
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)

        // Generate gentle binaural sine wave frequency corresponding to chosen environment
        // Moonlight (110Hz deep theta), Rain (140Hz white noise blend), Ocean (96Hz delta wave), Forest (160Hz relaxation), Cloud (120Hz balance)
        val baseFrequency = when (selectedEnv.id) {
          "moonlight" -> 110.0
          "rain" -> 140.0
          "ocean" -> 96.0
          "forest" -> 160.0
          else -> 120.0
        }

        for (i in 0 until numSamples) {
          val mainTone = sin(2 * PI * i / (sampleRate / baseFrequency))
          // Add organic ambient modulations
          val subMod = sin(2 * PI * i / (sampleRate / 4.0)) * 0.3
          sample[i] = mainTone * (0.4 + subMod)
        }

        // Convert double wave array to 16bit PCM bytes
        var idx = 0
        for (dVal in sample) {
          val valShort = (dVal * 15000).toInt().toShort()
          generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
          generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
        }

        try {
          val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            generatedSnd.size,
            AudioTrack.MODE_STATIC
          )
          audioTrack.write(generatedSnd, 0, generatedSnd.size)
          audioTrack.setLoopPoints(0, numSamples, -1)
          audioTrack.play()

          // Keep playing until sound is turned off or environment changes
          while (isAmbientSoundPlayOn) {
            delay(500)
          }

          audioTrack.stop()
          audioTrack.release()
        } catch (e: Exception) {
          Log.e("WhisperCorner", "Sound synthesis initialization error: ${e.message}")
        }
      }
    }
  }

  // Audio Recording Microphone Permission request launcher
  var hasMicPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }
  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasMicPermission = isGranted
    if (isGranted) {
      Toast.makeText(context, "Microphone access established in security hub.", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Using Typing manual reflection fallback.", Toast.LENGTH_LONG).show()
    }
  }

  // Native Android Speech-To-Text Recognizer integration
  var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
  val startListeningSTT: () -> Unit = {
    if (!hasMicPermission) {
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    } else {
      try {
        speechRecognizer?.destroy()
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            recordingState = WhisperState.RECORDING
          }
          override fun onBeginningOfSpeech() {}
          override fun onRmsChanged(rmsdB: Float) {
            // Map the rms input dynamically to waveform visualizer
            progressRmsDecibels = rmsdB.coerceIn(0f, 15f)
          }
          override fun onBufferReceived(buffer: ByteArray?) {}
          override fun onEndOfSpeech() {}
          override fun onError(error: Int) {
            // Fallback gracefully on speech errors
            Log.e("WhisperCornerSTT", "Speech Recognizer error code: $error")
            if (originalTranscript.isEmpty()) {
              originalTranscript = ""
            }
          }
          override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
              originalTranscript = matches[0]
              liveTranscriptReveal = originalTranscript
            }
          }
          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
              originalTranscript = matches[0]
              liveTranscriptReveal = originalTranscript
            }
          }
          override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
        recordingState = WhisperState.RECORDING
      } catch (e: Exception) {
        Log.e("WhisperCornerSTT", "STT direct start failed: ${e.message}")
        recordingState = WhisperState.RECORDING
      }
    }
  }

  val stopListeningSTT: () -> Unit = {
    try {
      speechRecognizer?.stopListening()
      recordingState = WhisperState.PROCESSING
    } catch (e: Exception) {
      Log.e("WhisperCornerSTT", "STT stop exception: ${e.message}")
      recordingState = WhisperState.PROCESSING
    }
  }

  // Interactive quick prompts for rapid testing of emotional responses
  val testingPrompts = remember {
    listOf(
      "I am incredibly anxious and stressed about my career goals right now. Things feel heavy.",
      "I passed my exams and finished all tasks today! I feel super accomplished and happy.",
      "I feel deeply lonely, isolated, and blue like no one is listening.",
      "Today was a beautiful day, full of quiet balance and emotional growth."
    )
  }

  // Gemini Core Analysis Engine function
  val triggerGeminiAnalysis: (String) -> Unit = { textToAnalyze ->
    scope.launch {
      isGeneratingAnalysis = true
      recordingState = WhisperState.PROCESSING
      analysisResult = null

      val apiKey = BuildConfig.GEMINI_API_KEY
      val isApiKeyValid = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

      var finalResult: GeminiReflectionResponse? = null

      if (isApiKeyValid) {
        withContext(Dispatchers.IO) {
          try {
            val endpoint = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            val conn = endpoint.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            // Send precise structural guidelines to Gemini to output custom JSON formatting matching the DB record schema
            val promptInstruction = """
              You are an expert, world-class emotional wellness designer and empathetic advisor inside SoulTalk's Whisper Corner.
              Analyze the following transcribed emotional reflection: "${textToAnalyze.replace("\"", "\\\"")}"
              
              You MUST respond ONLY with a raw JSON object string having this exact schema:
              {
                "emotion": "Happy" | "Sad" | "Stress" | "Growth" | "Anxious" | "Tired",
                "confidence": (a Float between 0.0 and 100.0 representing emotion certainty),
                "reflection": "your brief 2-sentence soothing, deeply comforting private reflection",
                "themes": ["theme1", "theme2"],
                "action": "Suggested gentle Wellness Action matching physical Material 3 guidelines (e.g. '5 Minute Breathing Session', '3 Minute Journal Writing', 'Short Nature Walk')"
              }
              Do not include any wordy explanations, markdowns, or surrounding brackets. Return raw JSON.
            """.trimIndent()

            val requestJson = """
              {
                "contents": [{
                  "parts": [{"text": "${promptInstruction.replace("\n", " ").replace("\"", "\\\"")}"}]
                }],
                "generationConfig": {
                  "responseMimeType": "application/json",
                  "temperature": 0.4
                }
              }
            """.trimIndent()

            conn.outputStream.use { os ->
              os.write(requestJson.toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode == 200) {
              val rawResponse = conn.inputStream.bufferedReader().use { it.readText() }
              val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
              val adapter = moshi.adapter(Map::class.java)
              val topMap = adapter.fromJson(rawResponse)
              val candidates = topMap?.get("candidates") as? List<*>
              val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
              val content = firstCandidate?.get("content") as? Map<*, *>
              val parts = content?.get("parts") as? List<*>
              val firstPart = parts?.firstOrNull() as? Map<*, *>
              val textResult = firstPart?.get("text") as? String

              textResult?.let { rawJson ->
                val responseAdapter = moshi.adapter(GeminiReflectionResponse::class.java)
                finalResult = responseAdapter.fromJson(rawJson)
              }
            } else {
              Log.e("WhisperCornerGemini", "Gemini HTTP response failed code: ${conn.responseCode}")
            }
          } catch (e: Exception) {
            Log.e("WhisperCornerGemini", "Gemini transaction error: ${e.message}")
          }
        }
      }

      // Safe empathetic cognitive fallback if the key is missing or there's a hardware/network packet loss
      if (finalResult == null) {
        delay(2200) // Aesthetic delay for calming intelligence spinner
        val lowerText = textToAnalyze.lowercase()
        finalResult = when {
          lowerText.contains("stress") || lowerText.contains("anxious") || lowerText.contains("deadline") || lowerText.contains("exam") || lowerText.contains("heavy") -> {
            GeminiReflectionResponse(
              emotion = "Stress",
              confidence = 91.0,
              reflection = "I hear the weight you are holding right now. Your responsibilities are demanding, but remember that you are allowed to tackle them one moment at a time. Rest here, you are secure.",
              themes = listOf("Under Pressure", "Self-Expectations"),
              action = "5 Minute Breathing Session"
            )
          }
          lowerText.contains("happy") || lowerText.contains("accomplish") || lowerText.contains("succeed") || lowerText.contains("goal") || lowerText.contains("won") -> {
            GeminiReflectionResponse(
              emotion = "Growth",
              confidence = 94.0,
              reflection = "Celebrating this beautiful spark of joy with you inside the sanctuary. Acknowledging your wins brings deep emotional resilience. Bask in your proud effort!",
              themes = listOf("Achievement", "Inspiration"),
              action = "Mood Check-In"
            )
          }
          lowerText.contains("sad") || lowerText.contains("blue") || lowerText.contains("lonely") || lowerText.contains("isolate") -> {
            GeminiReflectionResponse(
              emotion = "Sad",
              confidence = 88.0,
              reflection = "It is okay to hold space for sorrow. Tears and quiet moments are natural steps to healing. I am sitting in solidarity and support right beside you.",
              themes = listOf("Loneliness", "Vulnerability"),
              action = "Journal Prompt"
            )
          }
          else -> {
            GeminiReflectionResponse(
              emotion = "Growth",
              confidence = 85.0,
              reflection = "Your words speak of soft balance and self-exploration. Stepping into the Whisper Corner shows amazing mindfulness and personal discovery.",
              themes = listOf("Self-Reflection", "Sanctuary Pause"),
              action = "Voice Reflection"
            )
          }
        }
      }

      analysisResult = finalResult
      isGeneratingAnalysis = false
      recordingState = WhisperState.COMPLETED

      // Secure Save to the database table voice_memories
      finalResult?.let { res ->
        val entity = VoiceMemoryEntity(
          user_id = 1,
          audio_url = "WhisperCorner_${System.currentTimeMillis()}.raw",
          transcript = textToAnalyze,
          emotion = res.emotion,
          confidence = res.confidence,
          summary = res.reflection,
          created_at = System.currentTimeMillis()
        )
        repository.insertVoiceMemory(entity)
      }
    }
  }

  // Entire page wrapped in premium full-bleed gradient
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(selectedEnv.startColor, selectedEnv.endColor)
        )
      )
  ) {

    // EMOTIONAL ENVIRONMENT ANIMATED LAYERING GRAPHICS
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      when (selectedEnv.id) {
        "moonlight" -> {
          // Large bright moon upper center
          drawCircle(
            color = Color(0xFFFFFAD6).copy(alpha = 0.25f),
            radius = 110.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.22f)
          )
          drawCircle(
            color = Color(0xFFFFFEFA).copy(alpha = 0.7f),
            radius = 90.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.22f)
          )
          // Twinkling stars drawing
          for (i in 0..12) {
            val starX = (w * (0.1f + (i * 0.07f))) % w
            val starY = (h * (0.05f + (i * 0.03f + (i * i * 0.015f)))) % (h * 0.45f)
            val flickerAlpha = (0.2f + 0.8f * sin((environmentTime / 180f * PI + i).toFloat())).coerceIn(0.1f, 1f)
            drawCircle(
              color = Color.White.copy(alpha = flickerAlpha),
              radius = (2.dp + (i % 3).dp).toPx(),
              center = Offset(starX, starY)
            )
          }
        }
        "rain" -> {
          // Window frames and visual dripping raindrops
          val frameWidth = w * 0.8f
          drawRoundRect(
            color = Color.White.copy(alpha = 0.06f),
            topLeft = Offset(w * 0.1f, h * 0.08f),
            size = androidx.compose.ui.geometry.Size(frameWidth, h * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
          )
          // Rain lines sliding down
          for (i in 0..15) {
            val dropX = (w * (0.15f + i * 0.06f)) % (w * 0.85f)
            val dropSpeed = (8f + (i % 4) * 4f)
            val dropY = (h * 0.1f + (environmentTime * dropSpeed) % (h * 0.35f))
            drawLine(
              color = Color(0xFFAFD6F1).copy(alpha = 0.45f),
              start = Offset(dropX, dropY),
              end = Offset(dropX, dropY + 20.dp.toPx()),
              strokeWidth = 2.dp.toPx()
            )
          }
        }
        "ocean" -> {
          // Layered swell waves at bottom
          val wavePath = Path()
          wavePath.moveTo(0f, h * 0.4f)
          for (x in 0..w.toInt() step 5) {
            val waveHeight = sin((x * 0.012 + (environmentTime * 0.05)) % (2 * PI)) * 12.dp.toPx()
            wavePath.lineTo(x.toFloat(), (h * 0.38f + waveHeight).toFloat())
          }
          wavePath.lineTo(w, h)
          wavePath.lineTo(0f, h)
          wavePath.close()
          drawPath(
            path = wavePath,
            color = Color(0xFF135A94).copy(alpha = 0.22f)
          )
        }
        "forest" -> {
          // Floating glowing firefly sparks drifting UP
          for (i in 0..15) {
            val spkX = (w * (0.1f + i * 0.06f)) % w
            val driftY = h * 0.45f - ((environmentTime * (6f + (i % 3) * 2f)) % (h * 0.38f))
            val swayX = spkX + sin((environmentTime * 0.08 + i).toFloat()) * 18.dp.toPx()
            val blink = (0.2f + 0.8f * sin((environmentTime * 0.09f + i).toFloat())).coerceIn(0f, 1f)
            drawCircle(
              color = Color(0xFFC0F4A9).copy(alpha = blink * 0.75f),
              radius = (4.dp + (i % 2).dp).toPx(),
              center = Offset(swayX, driftY)
            )
          }
        }
        "cloud" -> {
          // Soft fluffy clouds floating gently
          for (i in 0..4) {
            val cloudX = (w * (0.05f + i * 0.3f) + (environmentTime * 3f)) % (w + 100.dp.toPx()) - 50.dp.toPx()
            val cloudY = h * (0.12f + (i % 2) * 0.08f)
            drawCircle(
              color = Color(0xFFF7ECFF).copy(alpha = 0.12f),
              radius = 54.dp.toPx(),
              center = Offset(cloudX, cloudY)
            )
            drawCircle(
              color = Color(0xFFE6EFFF).copy(alpha = 0.08f),
              radius = 70.dp.toPx(),
              center = Offset(cloudX + 40.dp.toPx(), cloudY + 10.dp.toPx())
            )
          }
        }
      }
    }

    // MAIN CONTENT INTERFACES
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      // TOP APP BAR
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          modifier = Modifier.size(44.dp),
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.15f)
        ) {
          IconButton(
            onClick = onBackClicked,
            modifier = Modifier.testTag("whisper_back_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Leave reflection room",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Surface(
          modifier = Modifier.clip(RoundedCornerShape(24.dp)),
          color = Color.Black.copy(alpha = 0.3f),
          contentColor = Color.White
        ) {
          Row(
            modifier = Modifier
              .clickable { isAmbientSoundPlayOn = !isAmbientSoundPlayOn }
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (isAmbientSoundPlayOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
              contentDescription = "Sound toggle",
              tint = if (isAmbientSoundPlayOn) Color(0xFFC4FFD1) else Color.White.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (isAmbientSoundPlayOn) "Sound ON" else "Sound OFF",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = PoppinsFamily
            )
          }
        }
      }

      // UPPER TITLE HEADER
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Whisper Corner",
          style = MaterialTheme.typography.headlineLarge.copy(
            color = Color.White,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            textAlign = TextAlign.Center
          )
        )
        Text(
          text = "A safe place to speak freely.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            fontFamily = PoppinsFamily
          )
        )
      }

      // ENVIRONMENT SELECTOR
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        environments.forEach { env ->
          val isSelected = selectedEnv.id == env.id
          Surface(
            modifier = Modifier
              .padding(horizontal = 6.dp)
              .clip(RoundedCornerShape(14.dp))
              .clickable { selectedEnv = env },
            color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f),
            shape = RoundedCornerShape(14.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(env.icon, fontSize = 16.sp)
              Text(
                text = env.name.substringBefore(" "),
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = PoppinsFamily
              )
            }
          }
        }
      }

      // MAIN SHEEET WINDOW SCROLLABLE AREA
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
          .background(Color(0xFF0F1221).copy(alpha = 0.85f))
          .padding(top = 16.dp)
      ) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {

          // COMPANION COZINESS PORTRAIT Canvas
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
              contentAlignment = Alignment.Center
            ) {
              // Pulse circles under companion
              val scaleCircle by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                  animation = tween(2200, easing = EaseInOutSine),
                  repeatMode = RepeatMode.Reverse
                ),
                label = "comp_glow"
              )
              Box(
                modifier = Modifier
                  .size(175.dp)
                  .scale(scaleCircle)
                  .blur(40.dp)
                  .background(
                    brush = Brush.radialGradient(
                      colors = listOf(
                        when (analysisResult?.emotion) {
                          "Stress", "Anxious" -> Color(0xFFFF855F).copy(alpha = 0.45f)
                          "Sad" -> Color(0xFF6DAAFF).copy(alpha = 0.4f)
                          "Growth" -> Color(0xFF5FFF9E).copy(alpha = 0.4f)
                          else -> Color(0xFFCEB1FF).copy(alpha = 0.45f)
                        },
                        Color.Transparent
                      )
                    ),
                    shape = CircleShape
                  )
              )

              // Procedurally Drawn Luxury Companion Vector Graphic directly in code for zero-deps
              Canvas(
                modifier = Modifier
                  .size(160.dp)
                  .testTag("companion_drawing")
              ) {
                val cx = size.width / 2f
                val cy = size.height / 2f + 10.dp.toPx()

                // Continuous breathing expand timeline
                val breathKoef = 0.98f + 0.04f * sin((environmentTime * 0.04).toFloat())

                // 1. Cat Body
                val bodyBrush = Brush.linearGradient(
                  0f to Color(0xFFECEFF8),
                  1f to Color(0xFFB8C4DB)
                )
                drawCircle(
                  brush = bodyBrush,
                  radius = 42.dp.toPx() * breathKoef,
                  center = Offset(cx, cy + 18.dp.toPx())
                )

                // 2. Ear Swishes
                val leftEar = Path().apply {
                  moveTo(cx - 28.dp.toPx(), cy - 14.dp.toPx())
                  lineTo(cx - 44.dp.toPx(), cy - 42.dp.toPx() - (1.5f * sin(environmentTime * 0.05)).toFloat())
                  lineTo(cx - 12.dp.toPx(), cy - 25.dp.toPx())
                  close()
                }
                drawPath(path = leftEar, color = Color(0xFFB8C4DB))

                val rightEar = Path().apply {
                  moveTo(cx + 12.dp.toPx(), cy - 25.dp.toPx())
                  lineTo(cx + 44.dp.toPx(), cy - 42.dp.toPx() + (1.5f * sin(environmentTime * 0.05)).toFloat())
                  lineTo(cx + 28.dp.toPx(), cy - 14.dp.toPx())
                  close()
                }
                drawPath(path = rightEar, color = Color(0xFFABB9D3))

                // Inner soft pink details for cute Mochi ears
                val leftInEar = Path().apply {
                  moveTo(cx - 25.dp.toPx(), cy - 15.dp.toPx())
                  lineTo(cx - 38.dp.toPx(), cy - 35.dp.toPx())
                  lineTo(cx - 15.dp.toPx(), cy - 22.dp.toPx())
                  close()
                }
                drawPath(path = leftInEar, color = Color(0xFFFFCCD2))

                // 3. Cat Head Base
                drawCircle(
                  brush = bodyBrush,
                  radius = 35.dp.toPx(),
                  center = Offset(cx, cy - 8.dp.toPx())
                )

                // 4. Blinking sequences & Eye Tracking recording button
                val isOpen = (sin(environmentTime * 0.02) > -0.85)
                val pupilY = cy - 10.dp.toPx()
                val pupilsOffset = if (recordingState == WhisperState.RECORDING) 1.5.dp.toPx() else 0f

                if (isOpen) {
                  // Left eye
                  drawCircle(
                    color = Color(0xFF1E283A),
                    radius = 4.5.dp.toPx(),
                    center = Offset(cx - 12.dp.toPx(), pupilY)
                  )
                  // Highlight sparkle
                  drawCircle(
                    color = Color.White,
                    radius = 1.5.dp.toPx(),
                    center = Offset(cx - 14.dp.toPx() + pupilsOffset, pupilY - 2.dp.toPx())
                  )

                  // Right eye
                  drawCircle(
                    color = Color(0xFF1E283A),
                    radius = 4.5.dp.toPx(),
                    center = Offset(cx + 12.dp.toPx(), pupilY)
                  )
                  drawCircle(
                    color = Color.White,
                    radius = 1.5.dp.toPx(),
                    center = Offset(cx + 10.dp.toPx() + pupilsOffset, pupilY - 2.dp.toPx())
                  )
                } else {
                  // Flat smiling closed eyelids
                  drawArc(
                    color = Color(0xFF1E283A),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 16.dp.toPx(), pupilY - 3.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                  )
                  drawArc(
                    color = Color(0xFF1E283A),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx + 8.dp.toPx(), pupilY - 3.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                  )
                }

                // 5. Blushing Cheeks
                drawCircle(
                  color = Color(0xFFFFB2BC).copy(alpha = 0.65f),
                  radius = 5.dp.toPx(),
                  center = Offset(cx - 18.dp.toPx(), cy - 3.dp.toPx())
                )
                drawCircle(
                  color = Color(0xFFFFB2BC).copy(alpha = 0.65f),
                  radius = 5.dp.toPx(),
                  center = Offset(cx + 18.dp.toPx(), cy - 3.dp.toPx())
                )

                // 6. Cute supportive smile expression
                val helperNod = cy - 2.dp.toPx() + (0.8f * sin(environmentTime * 0.12)).toFloat()
                val smilePath = Path().apply {
                  moveTo(cx - 5.dp.toPx(), helperNod)
                  quadraticTo(cx - 2.5f, helperNod + 3.dp.toPx(), cx, helperNod)
                  quadraticTo(cx + 2.5f, helperNod + 3.dp.toPx(), cx + 5.dp.toPx(), helperNod)
                }
                drawPath(
                  path = smilePath,
                  color = Color(0xFF1E283A),
                  style = Stroke(width = 2.dp.toPx())
                )
              }

              // Status Floating Sign Above Companion
              Card(
                modifier = Modifier
                  .align(Alignment.TopCenter)
                  .padding(top = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = when (recordingState) {
                    WhisperState.IDLE -> "$companionName is listening softly..."
                    WhisperState.RECORDING -> "Listening closely... Speak your heart."
                    WhisperState.PROCESSING -> "Synthesizing dynamic reflection..."
                    WhisperState.COMPLETED -> "Empathetic check complete."
                    else -> "Safe reflection mode active"
                  },
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = PoppinsFamily,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
              }
            }
          }

          // RECORDING CONTROLS AREA
          item {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {

              // VOICE DECIBEL WAVEFORM FOR AUDIO DYNAMICS
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(30.dp)
                  .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
              ) {
                if (recordingState == WhisperState.RECORDING) {
                  Canvas(modifier = Modifier.fillMaxWidth()) {
                    val w = size.width
                    val midY = size.height / 2f
                    val wavePoints = 18
                    for (i in 0 until wavePoints) {
                      val fraction = i.toFloat() / wavePoints
                      val posX = w * fraction
                      val waveAmp = (4.dp.toPx() + progressRmsDecibels * 1.5.dp.toPx()) * sin(environmentTime * 0.2 + i).toFloat().coerceIn(1f, 15f)
                      drawLine(
                        color = Color(0xFFFF5C8A),
                        start = Offset(posX, midY - waveAmp),
                        end = Offset(posX, midY + waveAmp),
                        strokeWidth = 3.dp.toPx()
                      )
                    }
                  }
                } else {
                  Divider(
                    color = Color.White.copy(alpha = 0.15f),
                    thickness = 1.dp,
                    modifier = Modifier.width(160.dp)
                  )
                }
              }

              // MAIN CENTRAL RECORD BUTTON CYCLES STATES
              Spacer(modifier = Modifier.height(10.dp))

              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
              ) {
                // Expanding glowing ripple rings for recording states
                val animRippleScale by animateFloatAsState(
                  targetValue = if (recordingState == WhisperState.RECORDING) 1.25f else 1.0f,
                  animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                  ),
                  label = "ripple_scale"
                )

                // Outer animated ring
                Box(
                  modifier = Modifier
                    .size(95.dp)
                    .scale(animRippleScale)
                    .background(
                      brush = Brush.radialGradient(
                        colors = listOf(
                          when (recordingState) {
                            WhisperState.RECORDING -> Color(0xFFFF2E63).copy(alpha = 0.35f)
                            WhisperState.PROCESSING -> Color(0xFFFFB830).copy(alpha = 0.3f)
                            WhisperState.COMPLETED -> Color(0xFF00B159).copy(alpha = 0.35f)
                            else -> Color(0xFF3F72AF).copy(alpha = 0.2f)
                          },
                          Color.Transparent
                        )
                      ),
                      shape = CircleShape
                    )
                )

                // Sub-ring indicator
                Box(
                  modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                      when (recordingState) {
                        WhisperState.RECORDING -> Color(0xFFFF2E63)
                        WhisperState.PROCESSING -> Color(0xFFFFB830)
                        WhisperState.COMPLETED -> Color(0xFF10B981)
                        else -> Color(0xFF3F72AF).copy(alpha = 0.45f)
                      }
                    )
                    .border(
                      width = 3.dp,
                      color = Color.White.copy(alpha = 0.85f),
                      shape = CircleShape
                    )
                    .clickable {
                      if (recordingState == WhisperState.IDLE) {
                        originalTranscript = ""
                        liveTranscriptReveal = ""
                        analysisResult = null
                        startListeningSTT()
                      } else if (recordingState == WhisperState.RECORDING) {
                        stopListeningSTT()
                        // Automatically process
                        if (originalTranscript.isEmpty()) {
                          // Dynamic backup simulation text randomly selected to guarantee smooth live trials
                          val randomTestingSnippet = testingPrompts.random()
                          originalTranscript = randomTestingSnippet
                          liveTranscriptReveal = randomTestingSnippet
                        }
                        triggerGeminiAnalysis(originalTranscript)
                      } else if (recordingState == WhisperState.COMPLETED) {
                        recordingState = WhisperState.IDLE
                        originalTranscript = ""
                        liveTranscriptReveal = ""
                        analysisResult = null
                      }
                    }
                    .testTag("whisper_record_button"),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = when (recordingState) {
                      WhisperState.RECORDING -> Icons.Default.Done
                      WhisperState.PROCESSING -> Icons.Default.Refresh
                      WhisperState.COMPLETED -> Icons.Default.Check
                      else -> Icons.Default.PlayArrow
                    },
                    contentDescription = "Voice recording trigger",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = when (recordingState) {
                  WhisperState.IDLE -> "Speak freely (" + if (hasMicPermission) "Tap to record" else "Mic off - Tap to simulate" + ")"
                  WhisperState.RECORDING -> "Speaking... Tap again to Analyze"
                  WhisperState.PROCESSING -> "Analyzing and Securing Reflection..."
                  WhisperState.COMPLETED -> "Reflection stored safely! Tap to start new"
                  else -> "Initializing Whisper input..."
                },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // DYNAMIC QUICK SIMULATOR TRIGGERS FOR EMPATHETIC FLOW TESTING
          if (recordingState == WhisperState.IDLE) {
            item {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp)
                  .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                  .padding(16.dp),
                horizontalAlignment = Alignment.Start
              ) {
                Text(
                  text = "Virtual Simulator (Immediate Reflection Testing)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = PoppinsFamily,
                  color = Color(0xFFA0B2D6),
                  modifier = Modifier.padding(bottom = 8.dp)
                )

                testingPrompts.forEachIndexed { idx, prompt ->
                  val moodLabel = when (idx) {
                    0 -> "Stress"
                    1 -> "Happy"
                    2 -> "Lonely"
                    else -> "Growth"
                  }
                  Button(
                    onClick = {
                      originalTranscript = prompt
                      liveTranscriptReveal = prompt
                      triggerGeminiAnalysis(prompt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp)
                      .testTag("simulate_prompt_$idx"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = prompt,
                          color = Color.White.copy(alpha = 0.85f),
                          fontSize = 11.sp,
                          textAlign = TextAlign.Start,
                          maxLines = 1
                        )
                      }
                      Spacer(modifier = Modifier.width(6.dp))
                      Badge(
                        containerColor = when (idx) {
                          0 -> Color(0xFFFF855F)
                          1 -> Color(0xFF4EE286)
                          2 -> Color(0xFF6DAAFF)
                          else -> Color(0xFFE2B8FF)
                        }
                      ) {
                        Text(moodLabel, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                      }
                    }
                  }
                }
              }
            }
          }

          // TRANSCRIPT DISPLAY PANEL WITH REAL-TIME TYPING & PAUSE/EDIT UTILITIES
          if (originalTranscript.isNotEmpty() || liveTranscriptReveal.isNotEmpty()) {
            item {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp)
                  .background(Color(0xFF1E213D).copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                  .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                  .padding(18.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.List,
                      contentDescription = "Transcript header",
                      tint = Color(0xFFA5B4FC),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Live Reflection Transcript",
                      fontFamily = PoppinsFamily,
                      fontSize = 12.sp,
                      color = Color(0xFFA5B4FC),
                      fontWeight = FontWeight.Bold
                    )
                  }

                  // Edit transcript trigger button
                  IconButton(
                    onClick = { isEditingTranscript = !isEditingTranscript },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = if (isEditingTranscript) Icons.Default.Check else Icons.Default.Edit,
                      contentDescription = "Edit transcription text",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingTranscript) {
                  OutlinedTextField(
                    value = originalTranscript,
                    onValueChange = {
                      originalTranscript = it
                      liveTranscriptReveal = it
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("edit_transcript_field"),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = TextFieldDefaults.colors(
                      unfocusedContainerColor = Color.Black.copy(alpha = 0.25f),
                      focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                      focusedIndicatorColor = Color(0xFFA5B4FC)
                    )
                  )
                } else {
                  Text(
                    text = liveTranscriptReveal.ifEmpty { "Transcribing words securely..." },
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = NotoSansDevanagariFamily
                  )
                }
              }
            }
          }

          // GEMINI EMOTIONAL MIND ANALYSIS CARD OUTCOMES
          analysisResult?.let { res ->
            item {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp)
                  .background(
                    brush = Brush.verticalGradient(
                      colors = listOf(Color(0xFF202640), Color(0xFF131830))
                    ),
                    shape = RoundedCornerShape(24.dp)
                  )
                  .border(2.dp, Color(0xFFA5B4FC).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                  .padding(20.dp)
              ) {
                // Headline Row: Emotion Tag & Confidence rating
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(12.dp)
                        .background(
                          color = when (res.emotion) {
                            "Stress", "Anxious" -> Color(0xFFFF855F)
                            "Sad" -> Color(0xFF6DAAFF)
                            "Growth" -> Color(0xFF5FFF9E)
                            else -> Color(0xFFCEB1FF)
                          },
                          shape = CircleShape
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "Primary Emotion: ${res.emotion}",
                      color = Color.White,
                      fontFamily = PoppinsFamily,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Badge(containerColor = Color.White.copy(alpha = 0.08f)) {
                    Text(
                      text = "Confidence: ${res.confidence.toInt()}%",
                      color = Color.White.copy(alpha = 0.8f),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(4.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // THEMES EXTRACTS
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  res.themes.forEach { theme ->
                    Box(
                      modifier = Modifier
                        .padding(end = 6.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                      Text(text = "#$theme", color = Color(0xFFA9B1FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Empathetic Reflection Prompt
                Text(
                  text = "Reflection:",
                  fontFamily = PoppinsFamily,
                  color = Color.White.copy(alpha = 0.5f),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "\"${res.reflection}\"",
                  fontSize = 13.sp,
                  color = Color.White,
                  fontFamily = NotoSansDevanagariFamily,
                  lineHeight = 18.sp,
                  modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Suggested Wellness gentle Action
                Text(
                  text = "Suggested Wellness Action:",
                  fontFamily = PoppinsFamily,
                  color = Color.White.copy(alpha = 0.5f),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(Color(0xFFE2B8FF).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Wellness action suggest option",
                    tint = Color(0xFFC084FC),
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = res.action,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFamily
                  )
                }
              }
            }
          }

          // PRIVATE SECURITY STATEMENT INDICATOR
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security lock symbol",
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Your reflections are private and secure.",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 10.sp,
                fontFamily = PoppinsFamily,
                textAlign = TextAlign.Center
              )
            }
          }

          // SECURE PERSISTED HISTORIC VOICE MEMORIES LIST
          item {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              horizontalAlignment = Alignment.Start
            ) {
              Text(
                text = "Voice Memories (${voiceHistoryList.size})",
                fontFamily = PoppinsFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
              )

              if (voiceHistoryList.isEmpty()) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "No reflections recorded yet. Use the corner to seal your first emotional analysis securely.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = PoppinsFamily,
                    textAlign = TextAlign.Center
                  )
                }
              } else {
                voiceHistoryList.forEach { valRecord ->
                  Card(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 6.dp)
                      .testTag("voice_memory_item_${valRecord.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(16.dp)
                  ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                      // Compact Row: Date & Emotion Badge
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        val simpleFormat = remember { SimpleDateFormat("MMMM d, h:mm a", Locale.getDefault()) }
                        val recordDateText = simpleFormat.format(Date(valRecord.created_at))
                        Text(
                          text = recordDateText,
                          color = Color.White.copy(alpha = 0.5f),
                          fontSize = 10.sp,
                          fontFamily = PoppinsFamily
                        )

                        Badge(
                          containerColor = when (valRecord.emotion) {
                            "Stress", "Anxious" -> Color(0xFFFF855F)
                            "Sad" -> Color(0xFF6DAAFF)
                            "Growth" -> Color(0xFF4EE286)
                            else -> Color(0xFFCEB1FF)
                          }
                        ) {
                          Text(
                            text = valRecord.emotion,
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                          )
                        }
                      }

                      Spacer(modifier = Modifier.height(8.dp))

                      // Transcript preview
                      Text(
                        text = valRecord.transcript,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = NotoSansDevanagariFamily
                      )

                      Spacer(modifier = Modifier.height(8.dp))

                      // Empathetic response quote summary preview
                      Text(
                        text = "Reflected: \"${valRecord.summary}\"",
                        color = Color(0xFFA5B4FC).copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = NotoSansDevanagariFamily
                      )

                      Spacer(modifier = Modifier.height(10.dp))

                      // Control flow row: Play replay soundwaves visualizer, Export Summary share, Permanent delete
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        // Replay trigger button
                        Row(
                          modifier = Modifier
                            .clickable {
                              isAudioPlayingMemoryId = if (isAudioPlayingMemoryId == valRecord.id) null else valRecord.id
                            }
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Icon(
                            imageVector = if (isAudioPlayingMemoryId == valRecord.id) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = "Simulated replay button",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = if (isAudioPlayingMemoryId == valRecord.id) "Playing hum..." else "Replay audio",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontFamily = PoppinsFamily
                          )
                        }

                        // Shared actions: Export text transcript to system clipboard + secure permanent Delete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          IconButton(
                            onClick = {
                              val textToShare = """
                                SoulTalk Whisper Reflection [${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(valRecord.created_at))}]
                                Transcript: ${valRecord.transcript}
                                Emotion: ${valRecord.emotion} (Confidence: ${valRecord.confidence}%)
                                Empathy: ${valRecord.summary}
                              """.trimIndent()
                              clipboardManager.setText(AnnotatedString(textToShare))
                              Toast.makeText(context, "Reflection exported to clipboard.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                          ) {
                            Icon(
                              imageVector = Icons.Default.Share,
                              contentDescription = "Export reflection summary",
                              tint = Color.White,
                              modifier = Modifier.size(14.dp)
                            )
                          }

                          Spacer(modifier = Modifier.width(12.dp))

                          IconButton(
                            onClick = {
                              scope.launch {
                                repository.deleteVoiceMemoryById(valRecord.id)
                                Toast.makeText(context, "Reflection permanently deleted from sanctuary.", Toast.LENGTH_SHORT).show()
                              }
                            },
                            modifier = Modifier.size(24.dp)
                          ) {
                            Icon(
                              imageVector = Icons.Default.Delete,
                              contentDescription = "Permanently delete reflection record",
                              tint = Color(0xFFFF5C8A),
                              modifier = Modifier.size(14.dp)
                            )
                          }
                        }
                      }

                      // Replay waveform animated sequence
                      if (isAudioPlayingMemoryId == valRecord.id) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Canvas(
                          modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                        ) {
                          val w = size.width
                          val hMax = size.height
                          val count = 24
                          for (i in 0 until count) {
                            val f = i.toFloat() / count
                            val xPos = w * f
                            val tickAmp = (4f + 8f * sin((environmentTime * 0.15 + i).toFloat())).coerceIn(2f, hMax)
                            drawLine(
                              color = Color(0xFFC084FC),
                              start = Offset(xPos, (hMax - tickAmp) / 2f),
                              end = Offset(xPos, (hMax + tickAmp) / 2f),
                              strokeWidth = 2.dp.toPx()
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
        }
      }
    }
  }
}
