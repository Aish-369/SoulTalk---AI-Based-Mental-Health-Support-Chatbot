package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserEntity
import com.example.data.database.CompanionProgressEntity
import com.example.data.repository.CompanionRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  repository: CompanionRepository,
  onNavigateBack: () -> Unit,
  onLogout: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var user by remember { mutableStateOf<UserEntity?>(null) }
  var progress by remember { mutableStateOf<CompanionProgressEntity?>(null) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  
  LaunchedEffect(Unit) {
    repository.userFlow.collect { user = it }
    repository.progressFlow.collect { progress = it }
  }
  
  Scaffold(
    containerColor = CalmingBackground,
    topBar = {
      TopAppBar(
        title = {
          Text(
            "Settings",
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
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PureWhite,
          scrolledContainerColor = PureWhite
        ),
        modifier = Modifier.shadow(2.dp)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      SettingsProfileHeader(user, progress)
      NotificationsControl()
      AIControlCenter()
      PrivacyDataControl(onDeleteAccount = { showDeleteDialog = true })
      SecuritySettings(onLogout = onLogout)
      VoiceSettings()
      LanguageSettings()
      DataManagement()
      CompanionSettings(user)
    }
    
    if (showDeleteDialog) {
      TrustfulConfirmationDialog(
        title = "Remove your account",
        message = "Your data will be removed from SoulTalk.",
        onConfirm = { scope.launch { onLogout() } },
        onDismiss = { showDeleteDialog = false }
      )
    }
  }
}

@Composable
fun SettingsProfileHeader(user: UserEntity?, progress: CompanionProgressEntity?) {
  val userName = user?.name ?: "User"
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite)
  ) {
    Row(
      modifier = Modifier.padding(20.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        modifier = Modifier.size(60.dp),
        shape = RoundedCornerShape(12.dp),
        color = SageGlow
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(userName.firstOrNull()?.uppercase() ?: "U", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = SageGreen)
        }
      }
      Column {
        Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QuietCharcoal)
        Text("Level ${progress?.level ?: 1} • ${user?.companion_name ?: "Mochi"}", style = MaterialTheme.typography.bodyMedium, color = QuietCharcoal.copy(alpha = 0.7f))
      }
    }
  }
}

@Composable
fun NotificationsControl() {
  var moodReminders by remember { mutableStateOf(true) }
  var journalReminders by remember { mutableStateOf(true) }
  
  SettingSection(title = "🔔 Notifications") {
    SettingToggle("Mood reminders", moodReminders) { moodReminders = it }
    SettingToggle("Journal reminders", journalReminders) { journalReminders = it }
  }
}

@Composable
fun AIControlCenter() {
  var aiMemoryEnabled by remember { mutableStateOf(true) }
  var personality by remember { mutableStateOf("Gentle Friend") }
  
  SettingSection(title = "🧠 AI Control") {
    SettingToggle("AI memory enabled", aiMemoryEnabled) { aiMemoryEnabled = it }
    SettingRow("AI personality", personality)
  }
}

@Composable
fun PrivacyDataControl(onDeleteAccount: () -> Unit) {
  SettingSection(title = "🔐 Privacy & Data") {
    SettingAction("View my data") { }
    SettingAction("Export my data") { }
    SettingAction("Remove my account", onDeleteAccount, Color(0xFFEF5350))
  }
}

@Composable
fun SecuritySettings(onLogout: () -> Unit) {
  var biometricEnabled by remember { mutableStateOf(false) }
  
  SettingSection(title = "🔒 Security") {
    SettingToggle("Biometric login", biometricEnabled) { biometricEnabled = it }
    SettingAction("Logout", onLogout, Color(0xFFEF5350))
  }
}

@Composable
fun VoiceSettings() {
  var voiceEnabled by remember { mutableStateOf(true) }
  
  SettingSection(title = "🎙 Voice") {
    SettingToggle("Voice companion", voiceEnabled) { voiceEnabled = it }
  }
}

@Composable
fun LanguageSettings() {
  var language by remember { mutableStateOf("English") }
  
  SettingSection(title = "🌐 Language") {
    SettingRow("Language", language)
  }
}

@Composable
fun DataManagement() {
  SettingSection(title = "💾 Data") {
    SettingRow("Storage used", "24.5 MB")
    SettingAction("Clear cache") { }
  }
}

@Composable
fun CompanionSettings(user: UserEntity?) {
  SettingSection(title = "🤖 Companion") {
    SettingRow("Companion type", user?.companion_type ?: "mochi_cat")
    SettingRow("Companion name", user?.companion_name ?: "Mochi")
  }
}

@Composable
fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = QuietCharcoal
      )
      content()
    }
  }
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = QuietCharcoal, fontWeight = FontWeight.Medium)
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = PureWhite,
        checkedTrackColor = SageGreen,
        uncheckedThumbColor = PureWhite,
        uncheckedTrackColor = SoftSlate.copy(alpha = 0.4f)
      )
    )
  }
}

@Composable
fun SettingRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = QuietCharcoal.copy(alpha = 0.7f))
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = QuietCharcoal)
  }
}

@Composable
fun SettingAction(label: String, onClick: () -> Unit, color: Color = QuietCharcoal) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = color.copy(alpha = 0.4f))
  }
}

@Composable
fun TrustfulConfirmationDialog(
  title: String,
  message: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
    text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text("Confirm", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun ExportDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Export your data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
    text = { Text("Your data will be exported as a JSON file.", style = MaterialTheme.typography.bodyMedium) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text("Export", fontWeight = FontWeight.Bold) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
