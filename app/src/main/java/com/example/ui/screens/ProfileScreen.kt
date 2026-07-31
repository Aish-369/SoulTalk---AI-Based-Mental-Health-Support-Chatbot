package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserEntity
import com.example.data.database.CompanionProgressEntity
import com.example.data.repository.CompanionRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// AI Tone options
enum class AITone(val displayName: String, val description: String) {
  GENTLE_FRIEND("Gentle Friend", "Warm, supportive, and caring responses"),
  CALM_LISTENER("Calm Listener", "Peaceful, patient, and understanding"),
  MOTIVATIONAL_COACH("Motivational Coach", "Encouraging, energetic, and goal-focused")
}

// Language options
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
  ENGLISH("en", "English", "English"),
  HINDI("hi", "Hindi", "हिंदी"),
  MARATHI("mr", "Marathi", "मराठी"),
  AUTO_DETECT("auto", "Auto-detect", "Auto")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  repository: CompanionRepository,
  onNavigateBack: () -> Unit,
  onLogout: () -> Unit
) {
  val scope = rememberCoroutineScope()
  
  // State
  var user by remember { mutableStateOf<UserEntity?>(null) }
  var progress by remember { mutableStateOf<CompanionProgressEntity?>(null) }
  var selectedAITone by remember { mutableStateOf(AITone.GENTLE_FRIEND) }
  var selectedLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
  var notificationsEnabled by remember { mutableStateOf(true) }
  var aiMemoryEnabled by remember { mutableStateOf(true) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  
  // Load data
  LaunchedEffect(Unit) {
    repository.userFlow.collect { userData ->
      user = userData
      selectedAITone = when (userData?.personality_type) {
        "Gentle Friend" -> AITone.GENTLE_FRIEND
        "Calm Listener" -> AITone.CALM_LISTENER
        "Motivational Coach" -> AITone.MOTIVATIONAL_COACH
        else -> AITone.GENTLE_FRIEND
      }
      selectedLanguage = when (userData?.language) {
        "hi" -> AppLanguage.HINDI
        "mr" -> AppLanguage.MARATHI
        "auto" -> AppLanguage.AUTO_DETECT
        else -> AppLanguage.ENGLISH
      }
    }
  }
  
  LaunchedEffect(Unit) {
    repository.progressFlow.collect { progressData ->
      progress = progressData
    }
  }
  
  Scaffold(
    containerColor = CalmingBackground,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Profile",
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
          IconButton(onClick = { /* Settings */ }) {
            Icon(Icons.Default.MoreVert, "More", tint = SageGreen, modifier = Modifier.size(24.dp))
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Profile Header
      item {
        ProfileHeader(user = user, progress = progress)
      }
      
      // Emotional Snapshot
      item {
        EmotionalSnapshot()
      }
      
      // AI Personality Settings
      item {
        AIPersonalitySettings(
          selectedTone = selectedAITone,
          onToneChanged = { selectedAITone = it }
        )
      }
      
      // Language Settings
      item {
        LanguageSettings(
          selectedLanguage = selectedLanguage,
          onLanguageChanged = { selectedLanguage = it }
        )
      }
      
      // Emotional Insights
      item {
        EmotionalInsights()
      }
      
      // Activity Stats
      item {
        ActivityStats()
      }
      
      // Companion Connection
      item {
        CompanionConnection(user = user, progress = progress)
      }
      
      // Privacy Control
      item {
        PrivacyControl(
          onDeleteData = { showDeleteConfirmation = true },
          onExportData = { showExportDialog = true },
          aiMemoryEnabled = aiMemoryEnabled,
          onAiMemoryToggle = { aiMemoryEnabled = it }
        )
      }
      
      // Account Settings
      item {
        AccountSettings(
          user = user,
          onLogout = onLogout
        )
      }
      
      // Bottom spacer
      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
      DeleteDataConfirmationDialog(
        onConfirm = {
          scope.launch {
            repository.clearCompanionData()
            showDeleteConfirmation = false
          }
        },
        onDismiss = { showDeleteConfirmation = false }
      )
    }
    
    // Export Dialog
    if (showExportDialog) {
      ExportDataDialog(
        onConfirm = {
          showExportDialog = false
          // Implement export functionality
        },
        onDismiss = { showExportDialog = false }
      )
    }
  }
}

@Composable
fun ProfileHeader(user: UserEntity?, progress: CompanionProgressEntity?) {
  val userName = user?.name ?: "User"
  val companionName = user?.companion_name ?: "Mochi"
  val companionType = user?.companion_type ?: "mochi_cat"
  val level = progress?.level ?: 1
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Surface(
        modifier = Modifier.size(96.dp),
        shape = CircleShape,
        color = SageGlow.copy(alpha = 0.4f)
      ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
          Text(
            text = userName.firstOrNull()?.uppercase() ?: "U",
            style = MaterialTheme.typography.displayMedium,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = SageGreen
          )
        }
      }
      
      Text(
        text = userName,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = QuietCharcoal
      )
      
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SoftLavender.copy(alpha = 0.12f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("🐺", fontSize = 16.sp)
          Text(
            text = companionName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = SoftLavender
          )
        }
      }
      
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SageGreen.copy(alpha = 0.12f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("🌈", fontSize = 16.sp)
          Text(
            text = "Level $level",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = SageGreen
          )
        }
      }
    }
  }
}

@Composable
fun EmotionalSnapshot() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "🌤",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Emotional Snapshot",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      // Trend visualization
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Mostly Calm Week",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = QuietCharcoal
        )
        
        // Simple trend bars
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TrendBar(label = "Calm", value = 0.7f, color = SageGreen)
          TrendBar(label = "Happy", value = 0.5f, color = SoftSkyBlue)
          TrendBar(label = "Stress", value = 0.2f, color = SoftLavender)
        }
        
        Text(
          text = "Small stress peaks shown gently",
          style = MaterialTheme.typography.bodySmall,
          color = QuietCharcoal.copy(alpha = 0.6f)
        )
      }
    }
  }
}

@Composable
fun TrendBar(label: String, value: Float, color: Color) {
  Column(
    modifier = Modifier.weight(1f),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = QuietCharcoal.copy(alpha = 0.7f)
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(QuietCharcoal.copy(alpha = 0.1f))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(value.coerceIn(0f, 1f))
          .fillMaxHeight()
          .clip(RoundedCornerShape(4.dp))
          .background(color)
      )
    }
  }
}

@Composable
fun AIPersonalitySettings(
  selectedTone: AITone,
  onToneChanged: (AITone) -> Unit
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
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "🤖",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "AI Personality Settings",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Text(
        text = "Choose how your AI companion responds to you",
        style = MaterialTheme.typography.bodyMedium,
        color = QuietCharcoal.copy(alpha = 0.7f)
      )
      
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AITone.values().forEach { tone ->
          ToneOption(
            tone = tone,
            isSelected = tone == selectedTone,
            onClick = { onToneChanged(tone) }
          )
        }
      }
    }
  }
}

@Composable
fun ToneOption(tone: AITone, isSelected: Boolean, onClick: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        SageGlow
      } else {
        QuietCharcoal.copy(alpha = 0.05f)
      }
    ),
    border = if (isSelected) {
      androidx.compose.foundation.BorderStroke(2.dp, SageGreen)
    } else null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = tone.displayName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
        Text(
          text = tone.description,
          style = MaterialTheme.typography.bodySmall,
          color = QuietCharcoal.copy(alpha = 0.6f)
        )
      }
      
      if (isSelected) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Selected",
          tint = SageGreen
        )
      }
    }
  }
}

@Composable
fun LanguageSettings(
  selectedLanguage: AppLanguage,
  onLanguageChanged: (AppLanguage) -> Unit
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
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "🌍",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Language Settings",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Text(
        text = "Select your preferred language",
        style = MaterialTheme.typography.bodyMedium,
        color = QuietCharcoal.copy(alpha = 0.7f)
      )
      
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AppLanguage.values().forEach { language ->
          LanguageOption(
            language = language,
            isSelected = language == selectedLanguage,
            onClick = { onLanguageChanged(language) }
          )
        }
      }
    }
  }
}

@Composable
fun LanguageOption(language: AppLanguage, isSelected: Boolean, onClick: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        SkyGlow
      } else {
        QuietCharcoal.copy(alpha = 0.05f)
      }
    ),
    border = if (isSelected) {
      androidx.compose.foundation.BorderStroke(2.dp, SoftSkyBlue)
    } else null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${language.nativeName} (${language.displayName})",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = QuietCharcoal
      )
      
      if (isSelected) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Selected",
          tint = SoftSkyBlue
        )
      }
    }
  }
}

@Composable
fun EmotionalInsights() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "💡",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Emotional Insights",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Top Emotion
        InsightRow(
          label = "Top Emotion",
          value = "Anxiety (but decreasing)",
          icon = "📉",
          color = SoftLavender
        )
        
        // Stability Score
        InsightRow(
          label = "Stability Score",
          value = "72%",
          icon = "📊",
          color = SageGreen
        )
        
        // Monthly Trend
        InsightRow(
          label = "Monthly Trend",
          value = "Improving",
          icon = "📈",
          color = SoftSkyBlue
        )
      }
    }
  }
}

@Composable
fun InsightRow(label: String, value: String, icon: String, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = QuietCharcoal.copy(alpha = 0.7f)
    )
    
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = color.copy(alpha = 0.2f)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = icon,
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          text = value,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
    }
  }
}

@Composable
fun ActivityStats() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "📊",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Activity Stats",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatCard(icon = "😊", label = "Mood Logs", value = "24", color = SageGreen, modifier = Modifier.weight(1f))
        StatCard(icon = "📝", label = "Journal Entries", value = "12", color = SoftSkyBlue, modifier = Modifier.weight(1f))
      }
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatCard(icon = "🎙", label = "Voice Sessions", value = "6", color = SoftLavender, modifier = Modifier.weight(1f))
        StatCard(icon = "🧘", label = "Breathing Sessions", value = "18", color = Color(0xFF81C784), modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
fun StatCard(icon: String, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = color.copy(alpha = 0.15f)
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = icon,
        style = MaterialTheme.typography.headlineMedium
      )
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = QuietCharcoal
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = QuietCharcoal.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun CompanionConnection(user: UserEntity?, progress: CompanionProgressEntity?) {
  val companionName = user?.companion_name ?: "Mochi"
  val level = progress?.level ?: 1
  val xp = progress?.xp ?: 0
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "💚",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Companion Connection",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ConnectionRow(label = "Companion Name", value = companionName)
        ConnectionRow(label = "Level", value = level.toString())
        ConnectionRow(label = "XP", value = "$xp XP")
        
        Divider(color = QuietCharcoal.copy(alpha = 0.1f))
        
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SageGlow
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "❤️",
              style = MaterialTheme.typography.titleLarge
            )
            Column {
              Text(
                text = "Bond Strength",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = QuietCharcoal
              )
              Text(
                text = "Strong Connection",
                style = MaterialTheme.typography.bodySmall,
                color = QuietCharcoal.copy(alpha = 0.7f)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ConnectionRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
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
fun PrivacyControl(
  onDeleteData: () -> Unit,
  onExportData: () -> Unit,
  aiMemoryEnabled: Boolean,
  onAiMemoryToggle: (Boolean) -> Unit
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
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "🔒",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Privacy Control",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Text(
        text = "Your data belongs to you. You have full control.",
        style = MaterialTheme.typography.bodyMedium,
        color = QuietCharcoal.copy(alpha = 0.7f)
      )
      
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        PrivacyOption(
          icon = Icons.Default.Delete,
          label = "Delete All Data",
          description = "Permanently remove all your data",
          onClick = onDeleteData,
          color = Color(0xFFEF5350)
        )
        
        PrivacyOption(
          icon = Icons.Default.Download,
          label = "Export Data",
          description = "Download your data as JSON",
          onClick = onExportData,
          color = QuietCharcoal
        )
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "AI Memory",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = QuietCharcoal
            )
            Text(
              text = "Allow AI to remember context",
              style = MaterialTheme.typography.bodySmall,
              color = QuietCharcoal.copy(alpha = 0.6f)
            )
          }
          Switch(
            checked = aiMemoryEnabled,
            onCheckedChange = onAiMemoryToggle,
            colors = SwitchDefaults.colors(
              checkedThumbColor = SageGreen,
              uncheckedThumbColor = SoftSlate
            )
          )
        }
      }
    }
  }
}

@Composable
fun PrivacyOption(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  description: String,
  onClick: () -> Unit,
  color: Color
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = color.copy(alpha = 0.1f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = color,
        modifier = Modifier.size(24.dp)
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = QuietCharcoal.copy(alpha = 0.6f)
        )
      }
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Navigate",
        tint = QuietCharcoal.copy(alpha = 0.4f)
      )
    }
  }
}

@Composable
fun AccountSettings(user: UserEntity?, onLogout: () -> Unit) {
  val email = user?.email ?: ""
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = PureWhite
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "👤",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = "Account Settings",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = QuietCharcoal
        )
      }
      
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AccountRow(
          icon = Icons.Default.Email,
          label = "Email",
          value = email
        )
        
        AccountRow(
          icon = Icons.Default.PhoneAndroid,
          label = "Google Account",
          value = "Linked"
        )
        
        Divider(color = QuietCharcoal.copy(alpha = 0.1f))
        
        Button(
          onClick = onLogout,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF5350).copy(alpha = 0.1f),
            contentColor = Color(0xFFEF5350)
          )
        ) {
          Text(
            text = "Logout",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
fun AccountRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = QuietCharcoal.copy(alpha = 0.6f),
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = QuietCharcoal.copy(alpha = 0.7f)
      )
    }
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = QuietCharcoal
    )
  }
}

@Composable
fun DeleteDataConfirmationDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Delete All Data?",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Text(
        text = "This will permanently delete all your data including mood logs, journal entries, chat history, and companion progress. This action cannot be undone.",
        style = MaterialTheme.typography.bodyMedium
      )
    },
    confirmButton = {
      TextButton(
        onClick = onConfirm
      ) {
        Text(
          text = "Delete",
          color = Color(0xFFEF5350),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun ExportDataDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Export Your Data",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Text(
        text = "Your data will be exported as a JSON file containing all your mood logs, journal entries, chat history, and companion progress.",
        style = MaterialTheme.typography.bodyMedium
      )
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(
          text = "Export",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
