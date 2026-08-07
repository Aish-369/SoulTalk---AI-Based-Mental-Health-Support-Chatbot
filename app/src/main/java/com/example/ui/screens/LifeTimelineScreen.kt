package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserEntity
import com.example.data.repository.CompanionRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class TimelineEventType {
  FIRST_ENTRY, JOURNAL_MOMENT, CHAT_BREAKTHROUGH,
  BREATHING_SESSION, VOICE_REFLECTION, EMOTIONAL_RECOVERY, ACHIEVEMENT
}

data class TimelineEvent(
  val id: Int, val eventType: TimelineEventType, val date: Long,
  val emotion: String, val emotionIcon: String, val title: String,
  val description: String, val aiReflection: String, val emotionalWeather: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTimelineScreen(
  repository: CompanionRepository,
  onNavigateBack: () -> Unit
) {
  var user by remember { mutableStateOf<UserEntity?>(null) }
  var timelineEvents by remember { mutableStateOf<List<TimelineEvent>>(emptyList()) }
  var storyMode by remember { mutableStateOf(false) }
  var selectedEvent by remember { mutableStateOf<TimelineEvent?>(null) }
  var companionReaction by remember { mutableStateOf<CompanionReaction?>(null) }
  var showGrowthSummary by remember { mutableStateOf(false) }
  var growthSummary by remember { mutableStateOf("") }
  
  LaunchedEffect(Unit) {
    repository.userFlow.collect { user = it }
    timelineEvents = generateTimelineEvents()
  }
  
  val listState = rememberLazyListState()
  val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
  
  LaunchedEffect(firstVisibleItemIndex) {
    if (timelineEvents.isNotEmpty() && firstVisibleItemIndex < timelineEvents.size) {
      companionReaction = getCompanionReactionForEvent(timelineEvents[firstVisibleItemIndex])
      delay(2000)
      companionReaction = null
    }
  }
  
  LaunchedEffect(listState) {
    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
      .collect { lastIndex ->
        if (lastIndex != null && lastIndex >= timelineEvents.size - 1 && !showGrowthSummary) {
          delay(500)
          growthSummary = generateGrowthSummary(timelineEvents)
          showGrowthSummary = true
        }
      }
  }
  
  Scaffold(
    containerColor = CalmingBackground,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Journal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = QuietCharcoal
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, "Back", tint = SageGreen, modifier = Modifier.size(24.dp))
          }
        },
        actions = {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(end = 12.dp)) {
            Icon(Icons.Default.BookmarksOutlined, contentDescription = null, tint = SageGreen, modifier = Modifier.size(20.dp))
            Switch(
              checked = storyMode,
              onCheckedChange = { storyMode = it },
              modifier = Modifier.scale(0.8f),
              colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = SageGreen,
                uncheckedThumbColor = PureWhite,
                uncheckedTrackColor = SoftSlate.copy(alpha = 0.4f)
              )
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PureWhite,
          scrolledContainerColor = PureWhite
        ),
        modifier = Modifier.shadow(2.dp)
      )
    }
  ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize()) {
      ParallaxBackground(scrollOffset = with(LocalDensity.current) { listState.firstVisibleItemScrollOffset.toFloat() })
      
      LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { TimelineHeader(user) }
        item { EmotionalWeatherOverlay(events = timelineEvents, storyMode = storyMode) }
        items(timelineEvents) { event ->
          TimelineEventCard(event = event, storyMode = storyMode, onClick = { selectedEvent = event }, onLongPress = {
            // Save as memory
          })
        }
        if (showGrowthSummary) { item { GrowthSummaryCard(summary = growthSummary, onClose = { showGrowthSummary = false }) } }
        item { Spacer(modifier = Modifier.height(80.dp)) }
      }
      
      companionReaction?.let { CompanionReactionOverlay(reaction = it, onDismiss = { companionReaction = null }) }
      selectedEvent?.let { EventDetailModal(event = it, onDismiss = { selectedEvent = null }) }
    }
  }
}

@Composable
fun TimelineHeader(user: UserEntity?) {
  val companionName = user?.companion_name ?: "Wolfie"
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          "Your Journey",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = QuietCharcoal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "Every moment shaped you.",
          style = MaterialTheme.typography.bodyMedium,
          color = SageGreen,
          fontWeight = FontWeight.Medium
        )
      }
      Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = SoftLavender.copy(alpha = 0.2f)
      ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
          Text("🐺", fontSize = 28.sp)
        }
      }
    }
  }
}

@Composable
fun ParallaxBackground(scrollOffset: Float) {
  val parallaxOffset by animateFloatAsState(targetValue = scrollOffset * 0.1f, animationSpec = tween(100), label = "parallax")
  Box(modifier = Modifier.fillMaxSize().offset(y = (-parallaxOffset).dp)) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(CalmingBackground, SageGlow.copy(alpha = 0.3f), CalmingBackground))))
    repeat(8) { index ->
      val particleOffset by rememberInfiniteTransition(label = "particle_$index").animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(6000 + index * 500, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "particle_$index")
      val angle = (index * 45).toDouble()
      val radius = 150.dp
      Box(modifier = Modifier.offset(x = (radius.value * kotlin.math.cos(kotlin.math.Math.toRadians(angle))).dp, y = (radius.value * kotlin.math.sin(kotlin.math.Math.toRadians(angle)) + parallaxOffset).dp).size(6.dp).clip(CircleShape).background(SageGreen.copy(alpha = 0.2f)))
    }
  }
}

@Composable
fun EmotionalWeatherOverlay(events: List<TimelineEvent>, storyMode: Boolean) {
  Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = PureWhite), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
    Column(modifier = Modifier.padding(20.dp)) {
      Text("Emotional Weather Journey", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = QuietCharcoal)
      Spacer(modifier = Modifier.height(16.dp))
      if (events.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          events.take(5).forEachIndexed { index, event ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
              Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = SageGreen.copy(alpha = 0.3f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("☀️", style = MaterialTheme.typography.titleMedium) }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(event.date)), style = MaterialTheme.typography.bodySmall, color = QuietCharcoal.copy(alpha = 0.6f))
            }
          }
        }
      } else {
        Text("Your emotional journey will appear here as you create moments.", style = MaterialTheme.typography.bodyMedium, color = QuietCharcoal.copy(alpha = 0.6f), textAlign = TextAlign.Center)
      }
    }
  }
}

@Composable
fun TimelineEventCard(event: TimelineEvent, storyMode: Boolean, onClick: () -> Unit, onLongPress: () -> Unit) {
  val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
  val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
  val eventIcon = when (event.eventType) {
    TimelineEventType.FIRST_ENTRY -> "🌱"
    TimelineEventType.JOURNAL_MOMENT -> "📔"
    TimelineEventType.CHAT_BREAKTHROUGH -> "💬"
    TimelineEventType.BREATHING_SESSION -> "🌬"
    TimelineEventType.VOICE_REFLECTION -> "🎙"
    TimelineEventType.EMOTIONAL_RECOVERY -> "🌈"
    TimelineEventType.ACHIEVEMENT -> "🏆"
  }
  val eventColor = when (event.eventType) {
    TimelineEventType.FIRST_ENTRY -> SageGreen
    TimelineEventType.JOURNAL_MOMENT -> SoftSkyBlue
    TimelineEventType.CHAT_BREAKTHROUGH -> SoftLavender
    TimelineEventType.BREATHING_SESSION -> Color(0xFF81C784)
    TimelineEventType.VOICE_REFLECTION -> Color(0xFFFFB74D)
    TimelineEventType.EMOTIONAL_RECOVERY -> Color(0xFFB39DDB)
    TimelineEventType.ACHIEVEMENT -> Color(0xFFFFD54F)
  }
  
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() }) },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, eventColor.copy(alpha = 0.2f))
  ) {
    Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
        Surface(
          modifier = Modifier.size(48.dp),
          shape = CircleShape,
          color = eventColor.copy(alpha = 0.15f),
          border = BorderStroke(1.5.dp, eventColor.copy(alpha = 0.4f))
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(eventIcon, style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp)
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.width(2.dp).height(36.dp).background(eventColor.copy(alpha = 0.25f)))
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(
            dateFormat.format(Date(event.date)),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = eventColor
          )
          Text(
            timeFormat.format(Date(event.date)),
            style = MaterialTheme.typography.labelSmall,
            color = QuietCharcoal.copy(alpha = 0.5f)
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(event.emotionIcon, fontSize = 18.sp)
          Text(
            event.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = QuietCharcoal
          )
        }
        Text(
          if (storyMode) event.aiReflection else event.description,
          style = MaterialTheme.typography.bodySmall,
          color = QuietCharcoal,
          lineHeight = 20.sp
        )
        Surface(shape = RoundedCornerShape(10.dp), color = eventColor.copy(alpha = 0.12f)) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("☀️", fontSize = 12.sp)
            Text(
              event.emotionalWeather,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = QuietCharcoal
            )
          }
        }
      }
    }
  }
}

@Composable
fun EventDetailModal(event: TimelineEvent, onDismiss: () -> Unit) {
  val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
  Box(modifier = Modifier.fillMaxSize().background(QuietCharcoal.copy(alpha = 0.5f)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
    Card(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = PureWhite)) {
      Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Moment Details", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = QuietCharcoal)
          IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close", tint = QuietCharcoal) }
        }
        Text(dateFormat.format(Date(event.date)), style = MaterialTheme.typography.bodyLarge, color = QuietCharcoal.copy(alpha = 0.7f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(event.emotionIcon, style = MaterialTheme.typography.displayLarge, fontSize = 48.sp)
          Column {
            Text(event.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = QuietCharcoal)
            Text(event.emotion, style = MaterialTheme.typography.titleMedium, color = QuietCharcoal.copy(alpha = 0.7f))
          }
        }
        Text(event.description, style = MaterialTheme.typography.bodyLarge, color = QuietCharcoal, lineHeight = 24.sp)
        Divider(color = QuietCharcoal.copy(alpha = 0.1f))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("💭 AI Reflection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = QuietCharcoal)
          Text(event.aiReflection, style = MaterialTheme.typography.bodyLarge, color = QuietCharcoal, lineHeight = 24.sp)
        }
        Surface(shape = RoundedCornerShape(12.dp), color = SageGlow) {
          Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("☀️", style = MaterialTheme.typography.titleMedium)
            Text(event.emotionalWeather, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = QuietCharcoal)
          }
        }
      }
    }
  }
}

@Composable
fun GrowthSummaryCard(summary: String, onClose: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = SageGreen.copy(alpha = 0.08f)),
    border = BorderStroke(1.5.dp, SageGreen.copy(alpha = 0.4f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
      Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = SageGreen.copy(alpha = 0.2f)
          ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
              Text("🌟", fontSize = 20.sp)
            }
          }
          Text(
            "Your Growth Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = QuietCharcoal
          )
        }
        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, "Close", tint = SageGreen, modifier = Modifier.size(20.dp))
        }
      }
      Text(
        summary,
        style = MaterialTheme.typography.bodyMedium,
        color = QuietCharcoal,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp
      )
      Surface(shape = RoundedCornerShape(16.dp), color = SoftLavender.copy(alpha = 0.15f)) {
        Row(
          modifier = Modifier.padding(18.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("🐺", fontSize = 24.sp)
          Text(
            "I'm so proud of how far you've come on this journey.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = QuietCharcoal,
            lineHeight = 20.sp
          )
        }
      }
    }
  }
}

@Composable
fun CompanionReactionOverlay(reaction: CompanionReaction, onDismiss: () -> Unit) {
  val scale by animateFloatAsState(targetValue = 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "reaction_scale")
  val alpha by animateFloatAsState(targetValue = 0f, animationSpec = tween(1500, delayMillis = 500), label = "reaction_alpha")
  if (alpha > 0.01f) {
    Box(modifier = Modifier.fillMaxSize().background(QuietCharcoal.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
      Box(modifier = Modifier.scale(scale).alpha(alpha), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = PureWhite)) {
          Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(when (reaction) { CompanionReaction.SMILE -> "😊"; CompanionReaction.SUPPORTIVE -> "🤗"; CompanionReaction.PROUD -> "🥰"; CompanionReaction.REFLECTIVE -> "🤔"; CompanionReaction.CELEBRATORY -> "🎉" }, style = MaterialTheme.typography.displayLarge, fontSize = 64.sp)
            Text(when (reaction) { CompanionReaction.SMILE -> "Such a happy moment!"; CompanionReaction.SUPPORTIVE -> "I'm here with you through this."; CompanionReaction.PROUD -> "I'm so proud of your growth!"; CompanionReaction.REFLECTIVE -> "This moment shaped who you are."; CompanionReaction.CELEBRATORY -> "You've come so far!" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = QuietCharcoal)
          }
        }
      }
    }
  }
}

fun generateTimelineEvents(): List<TimelineEvent> {
  val now = System.currentTimeMillis()
  val dayInMillis = 24 * 60 * 60 * 1000L
  return listOf(
    TimelineEvent(1, TimelineEventType.FIRST_ENTRY, now - 30 * dayInMillis, "Hopeful", "🌱", "First Entry", "You started your emotional wellness journey today.", "This was the beginning of something beautiful. You took the first step towards understanding yourself better.", "Sunny Mind"),
    TimelineEvent(2, TimelineEventType.JOURNAL_MOMENT, now - 25 * dayInMillis, "Anxious", "😟", "Anxiety Moment", "You were worried about your final year project.", "Even in moments of worry, you showed courage by expressing your feelings. That vulnerability is strength.", "Cloudy Day"),
    TimelineEvent(3, TimelineEventType.BREATHING_SESSION, now - 20 * dayInMillis, "Calm", "😌", "Breathing Session", "Completed a 10-minute breathing exercise.", "You found peace in the present moment. This practice became a cornerstone of your emotional toolkit.", "Sunny Mind"),
    TimelineEvent(4, TimelineEventType.CHAT_BREAKTHROUGH, now - 15 * dayInMillis, "Relieved", "😊", "Chat Breakthrough", "Had a meaningful conversation about your stress.", "Opening up helped release the weight you were carrying. Connection is healing.", "Recovery Phase"),
    TimelineEvent(5, TimelineEventType.VOICE_REFLECTION, now - 10 * dayInMillis, "Reflective", "🤔", "Voice Reflection", "Recorded your thoughts about the past week.", "Hearing your own voice helped you gain new perspective. Your wisdom is growing.", "Cloudy Day"),
    TimelineEvent(6, TimelineEventType.EMOTIONAL_RECOVERY, now - 5 * dayInMillis, "Hopeful", "🌈", "Emotional Recovery", "You felt more balanced after journaling.", "This was a turning point. You learned that emotions pass and clarity returns.", "Recovery Phase"),
    TimelineEvent(7, TimelineEventType.ACHIEVEMENT, now, "Proud", "🏆", "Achievement Unlocked", "Reached 1000 XP milestone!", "Your consistency has paid off. This achievement represents your commitment to emotional growth.", "Flourishing Period")
  )
}

fun getCompanionReactionForEvent(event: TimelineEvent): CompanionReaction {
  return when (event.emotion.lowercase()) {
    "happy", "relieved", "proud", "hopeful" -> CompanionReaction.SMILE
    "anxious", "worried", "stressed" -> CompanionReaction.SUPPORTIVE
    "calm", "peaceful" -> CompanionReaction.PROUD
    "reflective", "thoughtful" -> CompanionReaction.REFLECTIVE
    else -> CompanionReaction.CELEBRATORY
  }
}

fun generateGrowthSummary(events: List<TimelineEvent>): String {
  return "You have moved from frequent stress states to more balanced emotional patterns. Your journey shows remarkable resilience - from anxious moments about projects to finding peace through breathing exercises. Each step, whether journaling or voice reflection, has contributed to your emotional toolkit. You've learned that vulnerability is strength and that emotions pass. The companion has witnessed your growth from hopeful beginnings to flourishing periods. Keep nurturing this beautiful relationship with yourself."
}
