package com.example.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AIInsightsResponse
import com.example.data.api.EmotionalWeatherDto
import com.example.data.api.MoodLogDto
import com.example.data.database.UserEntity
import com.example.data.repository.CompanionRepository
import com.example.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class MoodHubUiState {
  object Loading : MoodHubUiState()
  data class Success(
    val logs: List<MoodLogDto>,
    val calendarLogs: List<MoodLogDto>,
    val weatherHistory: List<EmotionalWeatherDto>,
    val insights: AIInsightsResponse,
    val companion: UserEntity?
  ) : MoodHubUiState()
  data class Error(val message: String) : MoodHubUiState()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoulTalkMoodTrackingHubScreen(
  repository: CompanionRepository,
  onBackClicked: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var uiState by remember { mutableStateOf<MoodHubUiState>(MoodHubUiState.Loading) }
  var selectedCalendarLog by remember { mutableStateOf<MoodLogDto?>(null) }
  var selectedCalendarDateStr by remember { mutableStateOf("") }
  
  // Animation ticker for dynamic Pixar canvas companion sway
  val animTimeState = rememberInfiniteTransition()
  val ticker by animTimeState.animateFloat(
    initialValue = 0f,
    targetValue = 100f,
    animationSpec = infiniteRepeatable(
      animation = tween(6000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )

  // Trigger loading initially
  LaunchedEffect(Unit) {
    scope.launch {
      try {
        val userDetails = repository.userFlow.firstOrNull() ?: UserEntity(
          companion_type = "mochi_cat",
          companion_name = "Mochi",
          personality_type = "Calm, Friendly, Comforting"
        )
        val logs = repository.getMoodHistory()
        val calLogs = repository.getMoodCalendar()
        val weatherHist = repository.getWeatherHistory()
        val aiInsights = repository.getAIInsights()

        uiState = MoodHubUiState.Success(
          logs = logs,
          calendarLogs = calLogs,
          weatherHistory = weatherHist,
          insights = aiInsights,
          companion = userDetails
        )

        // Default calendar selection to today or latest log
        if (logs.isNotEmpty()) {
          selectedCalendarLog = logs.first()
          val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
          selectedCalendarDateStr = sdf.format(Date(selectedCalendarLog.created_at))
        }
      } catch (e: Exception) {
        Log.e("MoodHubScreen", "Failed to retrieve mood records: ${e.localizedMessage}")
        uiState = MoodHubUiState.Error(e.localizedMessage ?: "Unknown Error occurred")
      }
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = Modifier
      .fillMaxSize()
      .background(CalmingBackground)
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (val state = uiState) {
        is MoodHubUiState.Loading -> {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            CircularProgressIndicator(color = SoftSkyBlue)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Gently aligning your emotional stars...",
              fontFamily = NotoSansDevanagariFamily,
              color = SoftSlate,
              fontSize = 15.sp
            )
          }
        }
        is MoodHubUiState.Error -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "😔", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Sanctuary connection issue: ${state.message}",
              fontFamily = PoppinsFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = QuietCharcoal,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { uiState = MoodHubUiState.Loading },
              colors = ButtonDefaults.buttonColors(containerColor = SoftSkyBlue)
            ) {
              Text("Try Connecting Again", fontFamily = PoppinsFamily)
            }
          }
        }
        is MoodHubUiState.Success -> {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
          ) {
            // TOP APP BAR
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                IconButton(
                  onClick = onBackClicked,
                  modifier = Modifier.testTag("back_button")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to dashboard",
                    tint = SageGreen,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Text(
                  text = "Mood Tracking",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = QuietCharcoal,
                  modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
              }
            }

            // HEADER CARD WITH COMPANION
            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Your Emotional Journey",
                      style = MaterialTheme.typography.headlineSmall,
                      fontWeight = FontWeight.Bold,
                      color = QuietCharcoal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Every feeling tells a story.",
                      style = MaterialTheme.typography.bodyMedium,
                      color = SageGreen,
                      fontWeight = FontWeight.Medium
                    )
                    if (state.companion != null) {
                      Spacer(modifier = Modifier.height(12.dp))
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SageGreen.copy(alpha = 0.1f)
                      ) {
                        Text(
                          text = "Companion: ${state.companion.companion_name}",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.SemiBold,
                          color = SageGreen,
                          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                      }
                    }
                  }

                  Surface(
                    modifier = Modifier.size(92.dp),
                    shape = CircleShape,
                    color = SoftLavender.copy(alpha = 0.15f)
                  ) {
                    Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier.fillMaxSize()
                    ) {
                      AnimatedCompanionDrawCanvas(
                        companionId = state.companion?.companion_type ?: "mochi_cat",
                        time = ticker,
                        isDetailView = false
                      )
                    }
                  }
                }
              }
            }

            // TODAY'S STATUS CARD
            item {
              val latestLog = state.logs.firstOrNull()
              Text(
                text = "Today's Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = QuietCharcoal,
                modifier = Modifier.padding(bottom = 8.dp)
              )
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(24.dp)) {
                  if (latestLog != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                      Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = SageGlow.copy(alpha = 0.5f)
                      ) {
                        Box(
                          contentAlignment = Alignment.Center,
                          modifier = Modifier.fillMaxSize()
                        ) {
                          Text(text = latestLog.mood, fontSize = 28.sp)
                        }
                      }
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "Current Mood",
                          style = MaterialTheme.typography.labelSmall,
                          color = SageGreen,
                          fontWeight = FontWeight.SemiBold
                        )
                        Text(
                          text = latestLog.emotion,
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = QuietCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = SageGreen.copy(alpha = 0.12f)
                        ) {
                          Text(
                            text = state.insights.most_common_emotion.replace("Calm", "🌈 Recovery").replace("Happy", "☀️ Sunny").replace("Anxious", "⛈️ Stormy").replace("Stressed", "☁️ Cloudy"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = SageGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                          )
                        }
                      }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = DividerLight, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                      text = "Wolfie's Reflection",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = SoftLavender
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = when (latestLog.emotion.lowercase()) {
                        "happy" -> "I'm leaping around with joy for you! Keep spreading this wonderful light!"
                        "calm" -> "I curl up happily beside you. This is such a lovely ground state."
                        "sad" -> "I sit right beside you quietly, putting a warm paw on yours. It's okay."
                        "stressed" -> "I begin a micro-breathing wave to help unload your heavy chest."
                        "anxious" -> "We can float this worry cloud out together. Breathe with me..."
                        else -> "I'm looking at you lovingly. Absolute support is always here."
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = QuietCharcoal,
                      lineHeight = 20.sp
                    )
                  } else {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "You haven't checked in with yourself yet today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftSlate,
                        textAlign = TextAlign.Center
                      )
                    }
                  }
                }
              }
            }

            // MOOD CALENDAR
            item {
              Text(
                text = "Mood Calendar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = QuietCharcoal,
                modifier = Modifier.padding(bottom = 8.dp)
              )
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(20.dp)) {
                  val calNow = Calendar.getInstance()
                  val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                  Text(
                    text = sdfMonth.format(calNow.time),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = QuietCharcoal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(bottom = 16.dp)
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    val daysLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                    daysLabels.forEach { label ->
                      Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SoftSlate,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(12.dp))
                  
                  Spacer(modifier = Modifier.height(6.dp))

                  // Grid Days
                  val tempCal = Calendar.getInstance()
                  tempCal.set(Calendar.DAY_OF_MONTH, 1)
                  val startDay = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sun to 7 = Sat
                  val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                  
                  val itemsList = mutableListOf<Int?>()
                  for (i in 1 until startDay) {
                    itemsList.add(null)
                  }
                  for (i in 1..maxDays) {
                    itemsList.add(i)
                  }

                  val rowsCount = (itemsList.size + 6) / 7
                  for (row in 0 until rowsCount) {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      for (col in 0 until 7) {
                        val index = row * 7 + col
                        val dayNum = itemsList.getOrNull(index)
                        Box(
                          modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                          contentAlignment = Alignment.Center
                        ) {
                          if (dayNum != null) {
                            // Search if there is a log on this date
                            val logForDay = state.calendarLogs.firstOrNull { log ->
                              val logCal = Calendar.getInstance().apply { timeInMillis = log.created_at }
                              logCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                              logCal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) &&
                              logCal.get(Calendar.DAY_OF_MONTH) == dayNum
                            }
                            
                            val isSelected = selectedCalendarLog != null && 
                              Calendar.getInstance().apply { timeInMillis = selectedCalendarLog!!.created_at }.get(Calendar.DAY_OF_MONTH) == dayNum

                            Column(
                              modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                  if (isSelected) SoftSkyBlue.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .clickable {
                                  if (logForDay != null) {
                                    selectedCalendarLog = logForDay
                                    val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                                    selectedCalendarDateStr = sdf.format(Date(logForDay.created_at))
                                  } else {
                                    // Generate fallback selected title for empty index
                                    selectedCalendarLog = null
                                    selectedCalendarDateStr = "Day $dayNum, ${sdfMonth.format(calNow.time)}"
                                  }
                                }
                                .padding(4.dp),
                              horizontalAlignment = Alignment.CenterHorizontally,
                              verticalArrangement = Arrangement.Center
                            ) {
                              Text(
                                text = dayNum.toString(),
                                fontFamily = PoppinsFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (logForDay != null) QuietCharcoal else SoftSlate
                              )
                              
                              if (logForDay != null) {
                                val dotColor = when (logForDay.emotion.lowercase()) {
                                  "happy" -> SageGreen
                                  "calm" -> SoftSkyBlue
                                  "neutral" -> SoftSlate
                                  "sad" -> SoftLavender
                                  "stressed" -> Color(0xFFFFB74D) // Soft Orange
                                  "anxious" -> Color(0xFFEF5350) // Soft Red
                                  else -> SageGreen
                                }
                                Box(
                                  modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
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

              // Calendar Detail Card (Displays on Calendar day touch)
              Spacer(modifier = Modifier.height(12.dp))
              AnimatedVisibility(
                visible = selectedCalendarDateStr.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
              ) {
                Card(
                  shape = RoundedCornerShape(20.dp),
                  colors = CardDefaults.cardColors(containerColor = PureWhite),
                  border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.08f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                      text = selectedCalendarDateStr,
                      fontFamily = PoppinsFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = QuietCharcoal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val activeLog = selectedCalendarLog
                    if (activeLog != null) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = activeLog.mood, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = "Feeling: ${activeLog.emotion}",
                          fontFamily = PoppinsFamily,
                          fontWeight = FontWeight.Medium,
                          fontSize = 13.sp,
                          color = QuietCharcoal
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                          modifier = Modifier
                            .background(SkyGlow, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                          Text(
                            text = "Score: ${activeLog.score}",
                            fontFamily = PoppinsFamily,
                            fontSize = 11.sp,
                            color = QuietCharcoal
                          )
                        }
                      }
                      
                      if (!activeLog.notes.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                          text = "Notes:",
                          fontFamily = PoppinsFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 11.sp,
                          color = SoftSlate
                        )
                        Text(
                          text = activeLog.notes,
                          fontFamily = NotoSansDevanagariFamily,
                          fontSize = 12.sp,
                          color = QuietCharcoal,
                          lineHeight = 16.sp
                        )
                      }
                      
                      // Custom AI reflection simulation
                      Spacer(modifier = Modifier.height(12.dp))
                      HorizontalDivider(color = SoftSlate.copy(alpha = 0.1f))
                      Spacer(modifier = Modifier.height(10.dp))
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🪄", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = "Companion Reflection",
                          fontFamily = PoppinsFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp,
                          color = SageGreen
                        )
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = when (activeLog.emotion.lowercase()) {
                          "happy" -> "A beautiful sunshine record! Let's hold this cozy light for when the weather shifts."
                          "calm" -> "Resting back inside yourself builds such clean strength. This tranquil space is yours."
                          "sad" -> "Your tears are warm rain holding no threat, cleaning dust from your emotional windows."
                          "stressed" -> "Work deadlines are heavy but temporary. You are much bigger than this assignment scale."
                          "anxious" -> "Your racing heart is simply a storm. Remember, you have safe, deep anchors inside."
                          else -> "Your notes reveal deep, courageous introspection. We are walking together in peace."
                        },
                        fontFamily = NotoSansDevanagariFamily,
                        fontSize = 12.sp,
                        color = QuietCharcoal.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                      )
                    } else {
                      Text(
                        text = "No emotional check-ins completed on this date. Tap any marked date to relive your feelings.",
                        fontFamily = NotoSansDevanagariFamily,
                        fontSize = 12.sp,
                        color = SoftSlate,
                        lineHeight = 16.sp
                      )
                    }
                  }
                }
              }
            }

            // SECTION 3: EMOTIONAL WEATHER HISTORY
            item {
              Text(
                text = "Emotional Weather Timeline",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = QuietCharcoal
              )
              Spacer(modifier = Modifier.height(10.dp))
              
              if (state.weatherHistory.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  state.weatherHistory.take(4).forEach { trace ->
                    val dateStr = SimpleDateFormat("EEE, h:mm a", Locale.getDefault()).format(Date(trace.generated_at))
                    val emoji = when (trace.weather) {
                      "Sunny Mind" -> "☀️"
                      "Flourishing" -> "✨"
                      "Recovery Mode" -> "🌈"
                      "Cloudy Day" -> "☁️"
                      "Emotional Rain" -> "⛈️"
                      else -> "🌤️"
                    }
                    Card(
                      shape = RoundedCornerShape(16.dp),
                      colors = CardDefaults.cardColors(containerColor = PureWhite),
                      border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.08f)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(38.dp)
                            .background(SkyGlow, CircleShape),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(text = emoji, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                          Text(
                            text = trace.weather,
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = QuietCharcoal
                          )
                          Text(
                            text = dateStr,
                            fontFamily = PoppinsFamily,
                            fontSize = 11.sp,
                            color = SoftSlate
                          )
                        }
                      }
                    }
                  }
                }
              } else {
                Card(
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = PureWhite),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Begin checkins to write your atmospheric weather trends.",
                    fontFamily = NotoSansDevanagariFamily,
                    color = SoftSlate,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                  )
                }
              }
            }

            // SECTION 4 & 7: AI REFLECTIONS & WEEKLY REPORT
            item {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✨", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "AI Companion Insights",
                  fontFamily = PoppinsFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                  color = QuietCharcoal
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = SageGlow.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, SageGreen.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(20.dp)) {
                  Text(
                    text = "Weekly Summary",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = QuietCharcoal
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = state.insights.weekly_summary,
                    fontFamily = NotoSansDevanagariFamily,
                    fontSize = 13.sp,
                    color = QuietCharcoal,
                    lineHeight = 18.sp
                  )
                  
                  Spacer(modifier = Modifier.height(14.dp))
                  Text(
                    text = "AI Observations:",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SageGreen
                  )
                  
                  state.insights.insights.forEach { insight ->
                    Row(
                      modifier = Modifier.padding(vertical = 4.dp),
                      verticalAlignment = Alignment.Top
                    ) {
                      Text(text = "💡 ", fontSize = 13.sp)
                      Text(
                        text = insight,
                        fontFamily = NotoSansDevanagariFamily,
                        fontSize = 12.sp,
                        color = QuietCharcoal,
                        lineHeight = 16.sp
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))
                  Text(
                    text = "Achievements:",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SageGreen
                  )
                  state.insights.achievements.forEach { badge ->
                    Row(
                      modifier = Modifier.padding(vertical = 3.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(text = "🌸 ", fontSize = 12.sp)
                      Text(
                        text = badge,
                        fontFamily = NotoSansDevanagariFamily,
                        fontSize = 12.sp,
                        color = QuietCharcoal
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))
                  Text(
                    text = "Cozy Encouragement:",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SageGreen
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = state.insights.personalized_encouragement,
                    fontFamily = NotoSansDevanagariFamily,
                    fontSize = 13.sp,
                    color = QuietCharcoal,
                    lineHeight = 18.sp
                  )
                }
              }
            }

            // SECTION 5: PATTERN DISCOVERY
            item {
              Text(
                text = "Pattern Discovery",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = QuietCharcoal
              )
              Spacer(modifier = Modifier.height(10.dp))
              
              val items = listOf(
                "Most Common Emotion" to (state.insights.most_common_emotion to "✨"),
                "Your Best Day of Week" to (state.insights.best_day_of_week to "📅"),
                "Most Energetic Time" to (state.insights.most_positive_time to "⏰"),
                "Triggers Spotted" to (state.insights.stress_triggers to "🌊"),
                "Supportive Elements" to (state.insights.mood_improvement_factors to "🍃")
              )

              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { (title, detailPair) ->
                  val (detail, icon) = detailPair
                  Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(16.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(text = icon, fontSize = 20.sp)
                      Spacer(modifier = Modifier.width(14.dp))
                      Column {
                        Text(
                          text = title,
                          fontFamily = PoppinsFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 11.sp,
                          color = SoftSlate
                        )
                        Text(
                          text = detail,
                          fontFamily = NotoSansDevanagariFamily,
                          fontWeight = FontWeight.Medium,
                          fontSize = 13.sp,
                          color = QuietCharcoal
                        )
                      }
                    }
                  }
                }
              }
            }

            // SECTION 6: COMPANION GROWTH IMPACT
            item {
              Text(
                text = "Companion Growth Impact",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = QuietCharcoal
              )
              Spacer(modifier = Modifier.height(10.dp))
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, SoftSlate.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(20.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(48.dp)
                        .background(LavenderGlow, CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = "👑", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                      Text(
                        text = "${state.companion?.companion_name ?: "Mochi"}'s Affinity",
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = QuietCharcoal
                      )
                      Text(
                        text = "Level 3 - Safe Haven Companion",
                        fontFamily = PoppinsFamily,
                        fontSize = 12.sp,
                        color = SoftLavender
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(16.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "Growth Progress",
                      fontFamily = PoppinsFamily,
                      fontSize = 12.sp,
                      color = QuietCharcoal
                    )
                    Text(
                      text = "${state.logs.size * 20} / 200 XP",
                      fontFamily = PoppinsFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = QuietCharcoal
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  
                  val progressFraction = (state.logs.size * 20f / 200f).coerceIn(0f, 1f)
                  LinearProgressIndicator(
                    progress = progressFraction,
                    color = SoftLavender,
                    trackColor = SoftSlate.copy(alpha = 0.15f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(8.dp)
                      .clip(CircleShape)
                  )
                  
                  Spacer(modifier = Modifier.height(12.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "Mood Logs Logged:",
                      fontFamily = NotoSansDevanagariFamily,
                      fontSize = 12.sp,
                      color = SoftSlate
                    )
                    Text(
                      text = "${state.logs.size} logs",
                      fontFamily = PoppinsFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = QuietCharcoal
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
