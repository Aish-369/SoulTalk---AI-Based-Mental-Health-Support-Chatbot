package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PremiumDesignSystem {

    // Material 3 Rounded Corner Sizes
    object CornerRadius {
        val small = 8.dp
        val medium = 12.dp
        val large = 16.dp
        val extraLarge = 24.dp
        val full = 32.dp
    }

    // Elevation and Shadow Styles
    object Elevation {
        val subtle = 2.dp
        val medium = 4.dp
        val prominent = 8.dp
        val floating = 12.dp
    }

    // Spacing Scale
    object Spacing {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
        val xxxl = 32.dp
    }

    // Premium Typography Styles
    object TextStyles {
        val heroTitle: TextStyle
            @Composable get() = TextStyle(
                fontFamily = com.example.ui.theme.PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.5).sp,
                color = QuietCharcoal
            )

        val screenTitle: TextStyle
            @Composable get() = TextStyle(
                fontFamily = com.example.ui.theme.PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
                color = QuietCharcoal
            )

        val subtitle: TextStyle
            @Composable get() = TextStyle(
                fontFamily = com.example.ui.theme.NotoSansDevanagariFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
                color = SoftSlate
            )

        val bodyLarge: TextStyle
            @Composable get() = TextStyle(
                fontFamily = com.example.ui.theme.NotoSansDevanagariFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
                color = QuietCharcoal
            )

        val bodyMedium: TextStyle
            @Composable get() = TextStyle(
                fontFamily = com.example.ui.theme.NotoSansDevanagariFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                color = SoftSlate
            )

        val label: TextStyle
            @Composable get() = TextStyle(
                fontFamily = com.example.ui.theme.PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
                color = QuietCharcoal
            )

        val caption: TextStyle
            @Composable get() = TextStyle(
                fontFamily = com.example.ui.theme.NotoSansDevanagariFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
                color = SoftSlate
            )
    }
}

// Premium Rounded Button Component
@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SoftLavender,
    textColor: Color = QuietCharcoal,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PremiumDesignSystem.Spacing.lg),
        shape = RoundedCornerShape(PremiumDesignSystem.CornerRadius.large),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = DisabledGray
        ),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Text(text, style = PremiumDesignSystem.TextStyles.label)
    }
}

// Premium Card Component with Soft Shadow
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PureWhite,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(PremiumDesignSystem.CornerRadius.large)
            )
            .border(
                width = 1.dp,
                color = DividerLight,
                shape = RoundedCornerShape(PremiumDesignSystem.CornerRadius.large)
            )
    ) {
        content()
    }
}

// Premium Input Field Container
@Composable
fun PremiumInputContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SurfaceVariantLight,
                shape = RoundedCornerShape(PremiumDesignSystem.CornerRadius.medium)
            )
            .border(
                width = 1.dp,
                color = DividerLight,
                shape = RoundedCornerShape(PremiumDesignSystem.CornerRadius.medium)
            )
            .padding(PremiumDesignSystem.Spacing.md)
    ) {
        content()
    }
}

// Gradient Background Helper
@Composable
fun PremiumGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = CalmingBackground
            )
    ) {
        content()
    }
}

// Divider Component
@Composable
fun PremiumDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerLight
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = color)
            .padding(vertical = 0.5.dp)
    )
}
