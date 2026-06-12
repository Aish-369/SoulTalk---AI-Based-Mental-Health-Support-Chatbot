package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

// Configure Google Font Provider for downloading fonts safely at runtime
val fontProvider = GoogleFont.Provider(
  providerAuthority = "com.google.android.gms.fonts",
  providerPackage = "com.google.android.gms",
  certificates = R.array.com_google_android_gms_fonts_certs
)

// Poppins for elegant display, headers, and UI controls
val PoppinsFamily = FontFamily(
  Font(
    googleFont = GoogleFont("Poppins"),
    fontProvider = fontProvider,
    weight = FontWeight.Normal
  ),
  Font(
    googleFont = GoogleFont("Poppins"),
    fontProvider = fontProvider,
    weight = FontWeight.Medium
  ),
  Font(
    googleFont = GoogleFont("Poppins"),
    fontProvider = fontProvider,
    weight = FontWeight.SemiBold
  ),
  Font(
    googleFont = GoogleFont("Poppins"),
    fontProvider = fontProvider,
    weight = FontWeight.Bold
  )
)

// Noto Sans Devanagari for warm, organic, body and reflective text with local language support
val NotoSansDevanagariFamily = FontFamily(
  Font(
    googleFont = GoogleFont("Noto Sans Devanagari"),
    fontProvider = fontProvider,
    weight = FontWeight.Normal
  ),
  Font(
    googleFont = GoogleFont("Noto Sans Devanagari"),
    fontProvider = fontProvider,
    weight = FontWeight.Medium
  ),
  Font(
    googleFont = GoogleFont("Noto Sans Devanagari"),
    fontProvider = fontProvider,
    weight = FontWeight.Bold
  )
)

// Premium typography mappings for SoulTalk
val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = PoppinsFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    letterSpacing = (-0.5).sp
  ),
  headlineMedium = TextStyle(
    fontFamily = PoppinsFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    letterSpacing = 0.sp
  ),
  titleLarge = TextStyle(
    fontFamily = PoppinsFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    letterSpacing = 0.15.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = NotoSansDevanagariFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = NotoSansDevanagariFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
  ),
  labelLarge = TextStyle(
    fontFamily = PoppinsFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    letterSpacing = 1.25.sp
  )
)

