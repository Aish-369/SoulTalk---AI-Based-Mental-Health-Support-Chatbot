package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.ripple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.CompanionProgressEntity
import com.example.data.database.UserEntity
import com.example.data.repository.CompanionRepository
import com.example.ui.theme.*
import com.example.ui.theme.WolfieVoiceLines
import com.example.ui.components.WolfieCharacter
import com.example.ui.components.WolfieEmotion
import com.example.ui.components.WolfieSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Theme data classes
data class CompanionTheme(
  val id: String,
  val name: String,
  val emoji: String,
  val primaryColor: Color,
  val secondaryColor: Color,
  val backgroundColor: Color,
  val description: String
)

val companionThemes = listOf(
  CompanionTheme(
    id = "cloud_garden",
    name = "Cloud Garden",
    emoji = "☁️",
    primaryColor = Color(0xFFE3F2FD),
    secondaryColor = Color(0xFF90CAF9),
    backgroundColor = Color(0xFFF5F9FF),
    description = "Soft clouds floating in peaceful skies"
  ),
  CompanionTheme(
    id = "blossom_meadow",
    name = "Blossom Meadow",
    emoji = "🌸",
    primaryColor = Color(0xFFFCE4EC),
    secondaryColor = Color(0xFFF48FB1),
    backgroundColor = Color(0xFFFFF5F8),
    description = "Gentle petals dancing in the breeze"
  ),
  CompanionTheme(
    id = "forest_retreat",
    name = "Forest Retreat",
    emoji = "🌲",
    primaryColor = Color(0xFFE8F5E9),
    secondaryColor = Color(0xFF81C784),
    backgroundColor = Color(0xFFF1F8F3),
    description = "Ancient trees whispering wisdom"
  ),
  CompanionTheme(
    id = "moonlight_haven",
    name = "Moonlight Haven",
    emoji = "🌙",
    primaryColor = Color(0xFFEDE7F6),
    secondaryColor = Color(0xFF9575CD),
    backgroundColor = Color(0xFFF8F6FC),
    description = "Silver light guiding your dreams"
  ),
  CompanionTheme(
    id = "cozy_cabin",
    name = "Cozy Cabin",
    emoji = "🏡",
    primaryColor = Color(0xFFFFF3E0),
    secondaryColor = Color(0xFFFFB74D),
    backgroundColor = Color(0xFFFFFAF0),
    description = "Warm hearth and comforting embrace"
  )
)

// Evolution stages
data class EvolutionStage(
  val level: Int,
  val name: String,
  val description: String,
  val scale: Float,
  val accessories: List<String>
)

val evolutionStages = listOf(
  EvolutionStage(1, "Tiny Companion", "Just beginning our journey together", 0.6f, emptyList()),
  EvolutionStage(2, "Growing Companion", "Learning and developing every day", 0.8f, listOf("small_scarf")),
  EvolutionStage(3, "Healthy Companion", "Strong and thriving with purpose", 1.0f, listOf("scarf", "glasses")),
  EvolutionStage(4, "Thriving Companion", "Radiating warmth and wisdom", 1.2f, listOf("scarf", "hat", "backpack")),
  EvolutionStage(5, "Soul Guardian", "A beacon of light and guidance", 1.4f, listOf("scarf", "hat", "wings", "crown"))
)

// Memory data class
data class CompanionMemory(
  val id: Int,
  val title: String,
  val description: String,
  val icon: String,
  val date: Long,
  val category: String
)

// Achievement data class
data class Achievement(
  val id: String,
  val title: String,
  val description: String,
  val icon: String,
  val unlocked: Boolean,
  val unlockedAt: Long?,
  val progress: Int,
  val maxProgress: Int
)

// Customization items
data class CustomizationItem(
  val id: String,
  val name: String,
  val icon: String,
  val category: String,
  val unlocked: Boolean,
  val unlockLevel: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionHomeScreen(
  repository: CompanionRepository,
  onNavigateToChat: () -> Unit,
  onNavigateToBreathing: () -> Unit,
  onNavigateToMood: () -> Unit,
  onNavigateToJournal: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  
  // State
  var user by remember { mutableStateOf<UserEntity?>(null) }
  var progress by remember { mutableStateOf<CompanionProgressEntity?>(null) }
  var selectedTheme by remember { mutableStateOf(companionThemes[0]) }
  var showLevelUpCelebration by remember { mutableStateOf(false) }
  var showThemeSelector by remember { mutableStateOf(false) }
  var showCustomization by remember { mutableStateOf(false) }
  var companionMessage by remember { mutableStateOf("Welcome back. I'm so glad to see you. What's on your mind today?") }
  var companionReaction by remember { mutableStateOf<CompanionReaction?>(null) }
  
  // Load data
  LaunchedEffect(Unit) {
    repository.userFlow.collect { userData ->
      user = userData
    }
  }
  
  LaunchedEffect(Unit) {
    repository.progressFlow.collect { progressData ->
      val oldLevel = progress?.level ?: 1
      progress = progressData
      val newLevel = progressData?.level ?: 1
      if (newLevel > oldLevel) {
        showLevelUpCelebration = true
        companionMessage = "Look how far we've come together! We've reached level $newLevel!"
      }
    }
  }
  
  // Update companion dialogue based on time and context
  LaunchedEffect(Unit) {
    while (true) {
      delay(30000)
      updateCompanionDialogue(progress, user)?.let {
        companionMessage = it
      }
    }
  }
  
  // Animation states
  val infiniteTransition = rememberInfiniteTransition(label = "breathing")
  val breathingScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = EaseInOutCubic),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathing"
  )
  
  val floatOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 10f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "floating"
  )
  
  // Calculate XP progress
  val currentLevel = progress?.level ?: 1
  val currentXp = progress?.xp ?: 0
  val xpNeededForNextLevel = currentLevel * 50
  val xpProgress = if (xpNeededForNextLevel > 0) currentXp.toFloat() / xpNeededForNextLevel else 0f
  val evolutionStage = evolutionStages.find { it.level == currentLevel } ?: evolutionStages[0]
  
  Scaffold(
    containerColor = CalmingBackground,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "SoulTalk",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = QuietCharcoal
          )
        },
        navigationIcon = {
          IconButton(onClick = { showThemeSelector = true }) {
            Icon(Icons.Default.Palette, "Change Theme", tint = SageGreen)
          }
        },
        actions = {
          IconButton(onClick = { showCustomization = true }) {
            Icon(Icons.Default.Settings, "Customize", tint = SageGreen)
          }
          IconButton(onClick = onNavigateToChat) {
            Icon(Icons.Default.Chat, "Chat", tint = SageGreen)
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
    Box(modifier = Modifier.fillMaxSize().background(CalmingBackground)) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Header with companion info
        item {
          CompanionHeader(
            user = user,
            progress = progress,
            selectedTheme = selectedTheme,
            xpProgress = xpProgress,
            xpNeededForNextLevel = xpNeededForNextLevel
          )
        }
        
        // 3D Companion Display
        item {
          InteractiveCompanion(
            evolutionStage = evolutionStage,
            selectedTheme = selectedTheme,
            breathingScale = breathingScale,
            floatOffset = floatOffset,
            onReaction = { reaction ->
              companionReaction = reaction
              scope.launch {
                delay(2000)
                companionReaction = null
              }
            },
            onLongPress = {
              companionMessage = getEncouragingMessage(progress, user)
            }
          )
        }
        
        // Companion Message
        item {
          CompanionDialogueCard(
            message = companionMessage,
            selectedTheme = selectedTheme
          )
        }
        
        // Companion Status Card
        item {
          CompanionStatusCard(
            progress = progress,
            evolutionStage = evolutionStage,
            selectedTheme = selectedTheme
          )
        }
        
        // Quick Actions
        item {
          QuickActionsRow(
            onNavigateToBreathing = onNavigateToBreathing,
            onNavigateToMood = onNavigateToMood,
            onNavigateToJournal = onNavigateToJournal,
            selectedTheme = selectedTheme
          )
        }
        
        // Memories Timeline
        item {
          MemoriesTimeline(
            selectedTheme = selectedTheme,
            currentLevel = currentLevel
          )
        }
        
        // Achievements
        item {
          AchievementsSection(
            selectedTheme = selectedTheme,
            currentLevel = currentLevel,
            currentXp = currentXp
          )
        }
      }
      
      // Level Up Celebration Overlay
      if (showLevelUpCelebration) {
        LevelUpCelebration(
          evolutionStage = evolutionStage,
          selectedTheme = selectedTheme,
          onDismiss = { showLevelUpCelebration = false }
        )
      }
      
      // Theme Selector Bottom Sheet
      if (showThemeSelector) {
        ThemeSelectorBottomSheet(
          currentTheme = selectedTheme,
          themes = companionThemes,
          onThemeSelected = { theme ->
            selectedTheme = theme
            showThemeSelector = false
          },
          onDismiss = { showThemeSelector = false }
        )
      }
      
      // Customization Bottom Sheet
      if (showCustomization) {
        CustomizationBottomSheet(
          currentLevel = currentLevel,
          onDismiss = { showCustomization = false }
        )
      }
      
      // Companion Reaction Overlay
      companionReaction?.let { reaction ->
        CompanionReactionOverlay(
          reaction = reaction,
          selectedTheme = selectedTheme,
          onDismiss = { companionReaction = null }
        )
      }
    }
  }
}

@Composable
fun CompanionHeader(
  user: UserEntity?,
  progress: CompanionProgressEntity?,
  selectedTheme: CompanionTheme,
  xpProgress: Float,
  xpNeededForNextLevel: Int
) {
  val companionName = user?.companion_name ?: "Mochi"
  val currentLevel = progress?.level ?: 1
  val currentXp = progress?.xp ?: 0
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = companionName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = QuietCharcoal
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Level $currentLevel • ${evolutionStages.find { it.level == currentLevel }?.description ?: "Growing"}",
            style = MaterialTheme.typography.labelMedium,
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
            Text(text = "✨", fontSize = 28.sp)
          }
        }
      }
      
      Spacer(modifier = Modifier.height(20.dp))
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "$currentXp XP", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
          Text(text = "of $xpNeededForNextLevel", style = MaterialTheme.typography.labelSmall, color = SoftSlate)
        }
        Text(
          text = "${(xpProgress * 100).toInt()}%",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = SageGreen
        )
      }
      
      Spacer(modifier = Modifier.height(12.dp))
      
      LinearProgressIndicator(
        progress = xpProgress.coerceIn(0f, 1f),
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = SageGreen,
        trackColor = SurfaceVariantLight
      )
    }
  }
}

enum class CompanionReaction {
  SMILE,
  JUMP,
  WAG_TAIL,
  WAVE,
  HEART,
  SUPPORTIVE,
  PROUD,
  REFLECTIVE,
  CELEBRATORY
}

@Composable
fun InteractiveCompanion(
  evolutionStage: EvolutionStage,
  selectedTheme: CompanionTheme,
  breathingScale: Float,
  floatOffset: Float,
  onReaction: (CompanionReaction) -> Unit,
  onLongPress: () -> Unit
) {
  var tapCount by remember { mutableStateOf(0) }
  var lastTapTime by remember { mutableStateOf(0L) }
  var currentEmotion by remember { mutableStateOf(WolfieEmotion.LISTENING) }
  
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(340.dp)
      .clickable {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 500) {
          tapCount++
          if (tapCount >= 2) {
            onReaction(CompanionReaction.CELEBRATORY)
            currentEmotion = WolfieEmotion.CELEBRATING
            tapCount = 0
          }
        } else {
          tapCount = 1
          onReaction(CompanionReaction.SMILE)
          currentEmotion = WolfieEmotion.HAPPY
        }
        lastTapTime = now
      },
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = Brush.verticalGradient(
        colors = listOf(SoftLavender.copy(alpha = 0.15f), SoftSkyBlue.copy(alpha = 0.1f))
      ).let { PureWhite }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(SoftLavender.copy(alpha = 0.12f), SoftSkyBlue.copy(alpha = 0.08f))
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      // Animated background glow
      repeat(3) { index ->
        val glowAlpha by rememberInfiniteTransition(label = "glow_$index").animateFloat(
          initialValue = 0.1f,
          targetValue = 0.3f,
          animationSpec = infiniteRepeatable(
            animation = tween(3000 + index * 500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
          ),
          label = "glow_$index"
        )
        Box(
          modifier = Modifier
            .size(280.dp - (index * 40.dp))
            .clip(CircleShape)
            .background(SageGreen.copy(alpha = glowAlpha * 0.3f))
        )
      }
      
      // Wolfie Character with breathing animation
      Box(
        modifier = Modifier
          .scale(breathingScale)
          .offset(y = floatOffset.dp)
          .zIndex(1f),
        contentAlignment = Alignment.Center
      ) {
        WolfieCharacter(
          emotion = currentEmotion,
          size = WolfieSize.LARGE,
          modifier = Modifier.size(200.dp)
        )
      }
      
      // Gesture handler
      Box(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectTapGestures(
              onLongPress = {
                onLongPress()
                currentEmotion = WolfieEmotion.SUPPORTIVE
              }
            )
          }
      )
      
      // Bottom interaction hint
      Text(
        text = "Tap to interact • Long press for support",
        style = MaterialTheme.typography.labelSmall,
        color = QuietCharcoal.copy(alpha = 0.5f),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(16.dp)
      )
    }
  }
}

@Composable
fun CompanionDialogueCard(
  message: String,
  selectedTheme: CompanionTheme
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SoftPeach.copy(alpha = 0.4f)),
    border = BorderStroke(1.dp, SoftPeach.copy(alpha = 0.6f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
          modifier = Modifier.size(32.dp),
          shape = CircleShape,
          color = WarningOrange.copy(alpha = 0.2f)
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = "💬", fontSize = 16.sp)
          }
        }
        Text(
          text = "From Wolfie",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = QuietCharcoal,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp
      )
    }
  }
}

@Composable
fun CompanionStatusCard(
  progress: CompanionProgressEntity?,
  evolutionStage: EvolutionStage,
  selectedTheme: CompanionTheme
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Text(
        text = "Companion Status",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = QuietCharcoal
      )
      
      Spacer(modifier = Modifier.height(16.dp))
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        StatusItem(
          icon = "😊",
          label = "Mood",
          value = "Happy",
          selectedTheme = selectedTheme
        )
        StatusItem(
          icon = "💚",
          label = "Friendship",
          value = getFriendshipLevel(progress?.level ?: 1),
          selectedTheme = selectedTheme
        )
        StatusItem(
          icon = "🌱",
          label = "Stage",
          value = evolutionStage.name,
          selectedTheme = selectedTheme
        )
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = selectedTheme.primaryColor.copy(alpha = 0.3f)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "🎯",
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            text = "Today's Activity: Breathing Session Completed",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = QuietCharcoal
          )
        }
      }
    }
  }
}

@Composable
fun StatusItem(
  icon: String,
  label: String,
  value: String,
  selectedTheme: CompanionTheme
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = icon,
      style = MaterialTheme.typography.headlineMedium
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = QuietCharcoal.copy(alpha = 0.7f)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = QuietCharcoal
    )
  }
}

@Composable
fun QuickActionsRow(
  onNavigateToBreathing: () -> Unit,
  onNavigateToMood: () -> Unit,
  onNavigateToJournal: () -> Unit,
  selectedTheme: CompanionTheme
) {
  Column {
    Text(
      text = "Quick Actions",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      color = QuietCharcoal,
      modifier = Modifier.padding(horizontal = 4.dp)
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      QuickActionButton(
        icon = Icons.Default.Air,
        label = "Breathe",
        color = SoftSkyBlue,
        onClick = onNavigateToBreathing,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.Face,
        label = "Mood",
        color = SoftLavender,
        onClick = onNavigateToMood,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.Edit,
        label = "Journal",
        color = SageGreen,
        onClick = onNavigateToJournal,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun QuickActionButton(
  icon: ImageVector,
  label: String,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .aspectRatio(1f)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(),
        onClick = onClick
      ),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
    border = BorderStroke(1.5.dp, color.copy(alpha = 0.3f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = color,
        modifier = Modifier.size(36.dp)
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = QuietCharcoal,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun MemoriesTimeline(
  selectedTheme: CompanionTheme,
  currentLevel: Int
) {
  val memories = remember {
    generateMemories(currentLevel)
  }
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "📸",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Companion Memories",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      memories.forEachIndexed { index, memory ->
        MemoryCard(
          memory = memory,
          selectedTheme = selectedTheme,
          isLast = index == memories.size - 1
        )
        if (index < memories.size - 1) {
          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }
  }
}

@Composable
fun MemoryCard(
  memory: CompanionMemory,
  selectedTheme: CompanionTheme,
  isLast: Boolean
) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
  
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Timeline dot
    Box(
      modifier = Modifier
        .size(12.dp)
        .clip(CircleShape)
        .background(selectedTheme.secondaryColor)
    )
    
    // Timeline line
    if (!isLast) {
      Box(
        modifier = Modifier
          .width(2.dp)
          .fillMaxHeight()
          .background(selectedTheme.secondaryColor.copy(alpha = 0.3f))
      )
    }
    
    // Memory content
    Card(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = selectedTheme.primaryColor.copy(alpha = 0.3f)
      )
    ) {
      Column(
        modifier = Modifier.padding(12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = memory.icon,
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            text = memory.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = QuietCharcoal
          )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
          text = memory.description,
          style = MaterialTheme.typography.bodySmall,
          color = QuietCharcoal.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
          text = dateFormat.format(Date(memory.date)),
          style = MaterialTheme.typography.bodySmall,
          color = QuietCharcoal.copy(alpha = 0.6f)
        )
      }
    }
  }
}

@Composable
fun AchievementsSection(
  selectedTheme: CompanionTheme,
  currentLevel: Int,
  currentXp: Int
) {
  val achievements = remember {
    generateAchievements(currentLevel, currentXp)
  }
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "🏆",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Achievements",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(achievements.size) { index ->
          AchievementBadge(
            achievement = achievements[index],
            selectedTheme = selectedTheme
          )
        }
      }
    }
  }
}

@Composable
fun AchievementBadge(
  achievement: Achievement,
  selectedTheme: CompanionTheme
) {
  Card(
    modifier = Modifier.width(140.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (achievement.unlocked) {
        selectedTheme.secondaryColor.copy(alpha = 0.3f)
      } else {
        QuietCharcoal.copy(alpha = 0.05f)
      }
    ),
    border = if (achievement.unlocked) {
      androidx.compose.foundation.BorderStroke(
        2.dp,
        selectedTheme.secondaryColor
      )
    } else null
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = if (achievement.unlocked) achievement.icon else "🔒",
        style = MaterialTheme.typography.headlineMedium
      )
      Text(
        text = achievement.title,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = QuietCharcoal,
        textAlign = TextAlign.Center
      )
      if (!achievement.unlocked && achievement.maxProgress > 1) {
        LinearProgressIndicator(
          progress = { achievement.progress.toFloat() / achievement.maxProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = selectedTheme.secondaryColor,
          trackColor = QuietCharcoal.copy(alpha = 0.1f)
        )
      }
    }
  }
}

@Composable
fun LevelUpCelebration(
  evolutionStage: EvolutionStage,
  selectedTheme: CompanionTheme,
  onDismiss: () -> Unit
) {
  val scale by animateFloatAsState(
    targetValue = 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "celebration_scale"
  )
  
  val alpha by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(500),
    label = "celebration_alpha"
  )
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            selectedTheme.primaryColor,
            selectedTheme.secondaryColor
          )
        ).copy(alpha = 0.95f)
      )
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(24.dp),
      modifier = Modifier.scale(scale)
    ) {
      // Animated stars
      repeat(8) { index ->
        val starRotation by rememberInfiniteTransition(label = "star_$index").animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(
            animation = tween(3000 + index * 200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "star_$index"
        )
        
        val angle = (index * 45).toDouble()
        val radius = 120.dp
        
        Box(
          modifier = Modifier
            .offset(
              x = (radius.value * kotlin.math.cos(kotlin.math.Math.toRadians(angle))).dp,
              y = (radius.value * kotlin.math.sin(kotlin.math.Math.toRadians(angle))).dp
            )
        ) {
          Text(
            text = "⭐",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 32.sp
          )
        }
      }
      
      // Main celebration content
      Text(
        text = "🎉",
        style = MaterialTheme.typography.displayLarge,
        fontSize = 80.sp
      )
      
      Text(
        text = "LEVEL UP!",
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        color = PureWhite
      )
      
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = PureWhite
        )
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = evolutionStage.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = QuietCharcoal
          )
          
          Text(
            text = evolutionStage.description,
            style = MaterialTheme.typography.bodyLarge,
            color = QuietCharcoal.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
          )
          
          if (evolutionStage.accessories.isNotEmpty()) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              evolutionStage.accessories.forEach { accessory ->
                Text(
                  text = when (accessory) {
                    "scarf" -> "🧣"
                    "hat" -> "🎩"
                    "glasses" -> "👓"
                    "backpack" -> "🎒"
                    "wings" -> "🪽"
                    "crown" -> "👑"
                    else -> "✨"
                  },
                  style = MaterialTheme.typography.titleLarge
                )
              }
            }
          }
        }
      }
      
      Text(
        text = "Look how far we've come together!",
        style = MaterialTheme.typography.titleLarge,
        color = PureWhite,
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(32.dp))
      
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = PureWhite,
          contentColor = selectedTheme.secondaryColor
        )
      ) {
        Text(
          text = "Continue Journey",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorBottomSheet(
  currentTheme: CompanionTheme,
  themes: List<CompanionTheme>,
  onThemeSelected: (CompanionTheme) -> Unit,
  onDismiss: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(QuietCharcoal.copy(alpha = 0.5f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.BottomCenter
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = PureWhite
      )
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(
          text = "Choose Your Environment",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = QuietCharcoal
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        themes.forEach { theme ->
          ThemeOption(
            theme = theme,
            isSelected = theme.id == currentTheme.id,
            onClick = { onThemeSelected(theme) }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "Close",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
fun ThemeOption(
  theme: CompanionTheme,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        theme.primaryColor
      } else {
        PureWhite
      }
    ),
    border = if (isSelected) {
      androidx.compose.foundation.BorderStroke(2.dp, theme.secondaryColor)
    } else {
      androidx.compose.foundation.BorderStroke(1.dp, QuietCharcoal.copy(alpha = 0.1f))
    }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = theme.emoji,
          style = MaterialTheme.typography.headlineMedium
        )
        Column {
          Text(
            text = theme.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) QuietCharcoal else QuietCharcoal
          )
          Text(
            text = theme.description,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) QuietCharcoal.copy(alpha = 0.8f) else QuietCharcoal.copy(alpha = 0.6f)
          )
        }
      }
      
      if (isSelected) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Selected",
          tint = theme.secondaryColor
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationBottomSheet(
  currentLevel: Int,
  onDismiss: () -> Unit
) {
  val customizationItems = remember {
    generateCustomizationItems(currentLevel)
  }
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(QuietCharcoal.copy(alpha = 0.5f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.BottomCenter
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(500.dp)
        .padding(16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = PureWhite
      )
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(
          text = "Customize Companion",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = QuietCharcoal
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
          text = "Unlock items by leveling up your companion!",
          style = MaterialTheme.typography.bodyMedium,
          color = QuietCharcoal.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(customizationItems.size) { index ->
            CustomizationItemCard(
              item = customizationItems[index],
              currentLevel = currentLevel
            )
          }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "Close",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
fun CustomizationItemCard(
  item: CustomizationItem,
  currentLevel: Int
) {
  val isUnlocked = item.unlocked || currentLevel >= item.unlockLevel
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isUnlocked) {
        SageGlow
      } else {
        QuietCharcoal.copy(alpha = 0.05f)
      }
    ),
    border = if (isUnlocked) {
      androidx.compose.foundation.BorderStroke(1.dp, SageGreen)
    } else null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isUnlocked) item.icon else "🔒",
          style = MaterialTheme.typography.headlineMedium
        )
        Column {
          Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = QuietCharcoal
          )
          Text(
            text = if (isUnlocked) "Unlocked" else "Unlocks at Level ${item.unlockLevel}",
            style = MaterialTheme.typography.bodySmall,
            color = if (isUnlocked) {
              SageGreen
            } else {
              QuietCharcoal.copy(alpha = 0.5f)
            }
          )
        }
      }
      
      if (isUnlocked) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Unlocked",
          tint = SageGreen
        )
      }
    }
  }
}

@Composable
fun CompanionReactionOverlay(
  reaction: CompanionReaction,
  selectedTheme: CompanionTheme,
  onDismiss: () -> Unit
) {
  val scale by animateFloatAsState(
    targetValue = 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "reaction_scale"
  )
  
  val alpha by animateFloatAsState(
    targetValue = 0f,
    animationSpec = tween(1500, delayMillis = 500),
    label = "reaction_alpha"
  )
  
  if (alpha > 0.01f) {
    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .scale(scale)
          .alpha(alpha),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = selectedTheme.primaryColor.copy(alpha = 0.9f)
          )
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = when (reaction) {
                CompanionReaction.SMILE -> "😊"
                CompanionReaction.JUMP -> "🎉"
                CompanionReaction.WAG_TAIL -> "💨"
                CompanionReaction.WAVE -> "👋"
                CompanionReaction.HEART -> "❤️"
              },
              style = MaterialTheme.typography.displayLarge,
              fontSize = 64.sp
            )
            
            Text(
              text = when (reaction) {
                CompanionReaction.SMILE -> "So happy to see you!"
                CompanionReaction.JUMP -> "Yay! Let's play!"
                CompanionReaction.WAG_TAIL -> "I love spending time with you!"
                CompanionReaction.WAVE -> "Hello there, friend!"
                CompanionReaction.HEART -> "You're the best!"
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = QuietCharcoal
            )
          }
        }
      }
    }
  }
}

// Helper functions

fun getFriendshipLevel(level: Int): String {
  return when (level) {
    1 -> "New Friend"
    2 -> "Growing Friend"
    3 -> "Close Friend"
    4 -> "Best Friend"
    5, 6 -> "Soul Companion"
    else -> "Guardian Spirit"
  }
}

fun generateMemories(currentLevel: Int): List<CompanionMemory> {
  val baseMemories = listOf(
    CompanionMemory(
      id = 1,
      title = "First Journal Entry",
      description = "You wrote your first journal entry together",
      icon = "📝",
      date = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
      category = "journal"
    ),
    CompanionMemory(
      id = 2,
      title = "First Breathing Session",
      description = "Completed your first breathing exercise",
      icon = "🧘",
      date = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L,
      category = "breathing"
    ),
    CompanionMemory(
      id = 3,
      title = "7 Day Streak",
      description = "7 days of consistent growth together",
      icon = "🔥",
      date = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L,
      category = "streak"
    ),
    CompanionMemory(
      id = 4,
      title = "Emotional Breakthrough",
      description = "A meaningful moment of clarity",
      icon = "💡",
      date = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L,
      category = "growth"
    )
  )
  
  return if (currentLevel >= 3) {
    baseMemories + CompanionMemory(
      id = 5,
      title = "Level 3 Reached",
      description = "Your companion became a Healthy Companion",
      icon = "🌟",
      date = System.currentTimeMillis(),
      category = "level"
    )
  } else {
    baseMemories
  }
}

fun generateAchievements(currentLevel: Int, currentXp: Int): List<Achievement> {
  return listOf(
    Achievement(
      id = "first_reflection",
      title = "First Reflection",
      description = "Complete your first mood log",
      icon = "🌱",
      unlocked = true,
      unlockedAt = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
      progress = 1,
      maxProgress = 1
    ),
    Achievement(
      id = "seven_day_streak",
      title = "7 Day Check-In",
      description = "Check in for 7 consecutive days",
      icon = "🌸",
      unlocked = currentLevel >= 2,
      unlockedAt = if (currentLevel >= 2) System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L else null,
      progress = minOf(currentLevel, 7),
      maxProgress = 7
    ),
    Achievement(
      id = "thirty_moods",
      title = "30 Mood Logs",
      description = "Log your mood 30 times",
      icon = "🌟",
      unlocked = currentXp >= 300,
      unlockedAt = if (currentXp >= 300) System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L else null,
      progress = minOf(currentXp / 10, 30),
      maxProgress = 30
    ),
    Achievement(
      id = "first_voice",
      title = "First Voice Reflection",
      description = "Complete your first voice journal",
      icon = "🫶",
      unlocked = currentLevel >= 3,
      unlockedAt = if (currentLevel >= 3) System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L else null,
      progress = if (currentLevel >= 3) 1 else 0,
      maxProgress = 1
    ),
    Achievement(
      id = "thousand_xp",
      title = "1000 XP Reached",
      description = "Earn 1000 total XP",
      icon = "🏆",
      unlocked = currentXp >= 1000,
      unlockedAt = if (currentXp >= 1000) System.currentTimeMillis() else null,
      progress = currentXp,
      maxProgress = 1000
    )
  )
}

fun generateCustomizationItems(currentLevel: Int): List<CustomizationItem> {
  return listOf(
    CustomizationItem(
      id = "scarf",
      name = "Cozy Scarf",
      icon = "🧣",
      category = "accessory",
      unlocked = currentLevel >= 2,
      unlockLevel = 2
    ),
    CustomizationItem(
      id = "glasses",
      name = "Smart Glasses",
      icon = "👓",
      category = "accessory",
      unlocked = currentLevel >= 3,
      unlockLevel = 3
    ),
    CustomizationItem(
      id = "hat",
      name = "Adventurer Hat",
      icon = "🎩",
      category = "accessory",
      unlocked = currentLevel >= 4,
      unlockLevel = 4
    ),
    CustomizationItem(
      id = "backpack",
      name = "Journey Backpack",
      icon = "🎒",
      category = "accessory",
      unlocked = currentLevel >= 4,
      unlockLevel = 4
    ),
    CustomizationItem(
      id = "wings",
      name = "Spirit Wings",
      icon = "🪽",
      category = "accessory",
      unlocked = currentLevel >= 5,
      unlockLevel = 5
    ),
    CustomizationItem(
      id = "crown",
      name = "Guardian Crown",
      icon = "👑",
      category = "accessory",
      unlocked = currentLevel >= 5,
      unlockLevel = 5
    ),
    CustomizationItem(
      id = "bow_tie",
      name = "Dapper Bow Tie",
      icon = "🎀",
      category = "accessory",
      unlocked = currentLevel >= 3,
      unlockLevel = 3
    ),
    CustomizationItem(
      id = "nature_effects",
      name = "Nature Aura",
      icon = "🌿",
      category = "effect",
      unlocked = currentLevel >= 2,
      unlockLevel = 2
    )
  )
}

fun updateCompanionDialogue(
  progress: CompanionProgressEntity?,
  user: UserEntity?
  ): String? {
  val level = progress?.level ?: 1
  val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
  
  // Use Wolfie's personality-driven voice lines
  val timeBasedGreeting = when (hour) {
    in 5..11 -> WolfieVoiceLines.morningGreetings.random()
    in 17..20 -> WolfieVoiceLines.eveningGreetings.random()
    else -> "I'm here with you, always."
  }
  
  val levelBasedMessage = when {
    level >= 5 -> "You've become such a wise soul. I'm honored to be your guardian."
    level >= 3 -> "I've noticed how much you've grown. Your emotional awareness is beautiful."
    level >= 2 -> "We're building something special together. Keep growing."
    else -> "Every moment of reflection matters. You're already becoming more aware."
  }
  
  val activityBasedMessages = listOf(
    "I see how you're taking care of yourself. That takes real courage.",
    "Your willingness to feel deeply shows real strength.",
    "The work you're doing here is important. I believe in you.",
    "You're showing up for yourself. That matters more than you know.",
    "Remember to be kind to yourself today. You deserve that compassion."
  )
  
  return timeBasedGreeting + " " + levelBasedMessage + " " + activityBasedMessages.random()
  }

fun getEncouragingMessage(
  progress: CompanionProgressEntity?,
  user: UserEntity?
): String {
  val level = progress?.level ?: 1
  val companionName = user?.companion_name ?: "Mochi"
  
  val messages = listOf(
    "I'm proud of how consistent you've been lately.",
    "Your emotional growth is inspiring to witness.",
    "You're doing better than you realize. Keep going!",
    "Every challenge you face makes you stronger.",
    "I cherish every moment we spend together.",
    "Your resilience amazes me every day.",
    "Remember: progress, not perfection. You're doing great.",
    "I believe in you, now and always.",
    "Your journey is unique and beautiful.",
    "Together, we can weather any storm."
  )
  
  return messages.random()
}
