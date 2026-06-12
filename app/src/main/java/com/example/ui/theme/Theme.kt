package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SoulTalkColorScheme = lightColorScheme(
  primary = SoftLavender,
  onPrimary = QuietCharcoal,
  primaryContainer = LavenderGlow,
  onPrimaryContainer = QuietCharcoal,
  secondary = SageGreen,
  onSecondary = PureWhite,
  secondaryContainer = SageGlow,
  onSecondaryContainer = QuietCharcoal,
  tertiary = SoftSkyBlue,
  onTertiary = QuietCharcoal,
  tertiaryContainer = SkyGlow,
  onTertiaryContainer = QuietCharcoal,
  background = CalmingBackground,
  onBackground = QuietCharcoal,
  surface = PureWhite,
  onSurface = QuietCharcoal,
  surfaceVariant = CalmingBackground,
  onSurfaceVariant = QuietCharcoal,
  outline = SoftSlate
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic system overlays to preserve our custom premium pastel identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Always use our calming SoulTalk color scheme to stay consistent with the designer guidelines
  val colorScheme = SoulTalkColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
