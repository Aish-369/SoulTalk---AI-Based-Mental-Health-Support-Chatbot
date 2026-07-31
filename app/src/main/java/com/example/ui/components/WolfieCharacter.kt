package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalmingBackground
import com.example.ui.theme.NotoSansDevanagariFamily
import com.example.ui.theme.PoppinsFamily
import com.example.ui.theme.PremiumDesignSystem
import com.example.ui.theme.QuietCharcoal
import com.example.ui.theme.SageGlow
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftSlate
import com.example.ui.theme.WolfieEyeWhite
import com.example.ui.theme.WolfieGray
import com.example.ui.theme.WolfieNoseBlack
import kotlinx.coroutines.delay

enum class WolfieEmotion {
    LISTENING,
    THINKING,
    HAPPY,
    CELEBRATING,
    MEDITATING,
    SLEEPING,
    TYPING,
    SUPPORTIVE
}

/**
 * Reusable Wolfie Character Component
 * Displays Wolfie with different emotional states throughout the app.
 * Supports smooth pose transitions and contextual animations.
 */
@Composable
fun WolfieCharacter(
    emotion: WolfieEmotion = WolfieEmotion.LISTENING,
    size: WolfieSize = WolfieSize.MEDIUM,
    isAnimating: Boolean = true,
    modifier: Modifier = Modifier
) {
    val wolfieScale = when (size) {
        WolfieSize.SMALL -> 0.6f
        WolfieSize.MEDIUM -> 1f
        WolfieSize.LARGE -> 1.4f
    }

    var offsetY by remember { mutableStateOf(0f) }
    val animationOffset = remember { Animatable(0f) }

    // Breathing/bobbing animation for listening and supportive states
    LaunchedEffect(emotion) {
        if (emotion == WolfieEmotion.LISTENING || emotion == WolfieEmotion.SUPPORTIVE) {
            animationOffset.animateTo(
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 2000
                        0f at 0 using FastOutSlowInEasing
                        8f at 1000 using FastOutSlowInEasing
                        0f at 2000
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            animationOffset.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size((150 * wolfieScale).dp)
            .offset(y = animationOffset.value.dp),
        contentAlignment = Alignment.Center
    ) {
        when (emotion) {
            WolfieEmotion.LISTENING -> WolfieListening(wolfieScale)
            WolfieEmotion.THINKING -> WolfieThinking(wolfieScale)
            WolfieEmotion.HAPPY -> WolfieHappy(wolfieScale)
            WolfieEmotion.CELEBRATING -> WolfieCelebrating(wolfieScale)
            WolfieEmotion.MEDITATING -> WolfieMeditating(wolfieScale)
            WolfieEmotion.SLEEPING -> WolfieSleeping(wolfieScale)
            WolfieEmotion.TYPING -> WolfieTyping(wolfieScale)
            WolfieEmotion.SUPPORTIVE -> WolfieSupport(wolfieScale)
        }
    }
}

enum class WolfieSize {
    SMALL, MEDIUM, LARGE
}

// Wolfie Listening Pose - Warm, present, actively engaged
@Composable
private fun WolfieListening(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Head
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((40 * scale).dp))
        ) {
            // Eyes - Open and warm
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (20 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                // Left eye
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.Center)
                            .offset(x = (2 * scale).dp, y = (1 * scale).dp)
                    )
                }
                // Right eye
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.Center)
                            .offset(x = (2 * scale).dp, y = (1 * scale).dp)
                    )
                }
            }

            // Nose
            Box(
                modifier = Modifier
                    .size((10 * scale).dp)
                    .background(WolfieNoseBlack, CircleShape)
                    .align(Alignment.Center)
                    .offset(y = (5 * scale).dp)
            )

            // Smile
            Box(
                modifier = Modifier
                    .size((20 * scale).dp, (8 * scale).dp)
                    .background(
                        WolfieNoseBlack,
                        RoundedCornerShape(bottomStart = (10 * scale).dp, bottomEnd = (10 * scale).dp)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = -(8 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        // Body
        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Thinking Pose - Thoughtful hand on chin
@Composable
private fun WolfieThinking(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Head tilted slightly
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((40 * scale).dp))
                .rotate(5f)
        ) {
            // Thoughtful eyes - looking upward
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (20 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.TopCenter)
                            .offset(x = (1 * scale).dp, y = (1 * scale).dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.TopCenter)
                            .offset(x = (1 * scale).dp, y = (1 * scale).dp)
                    )
                }
            }

            // Pensive mouth - straight line
            Box(
                modifier = Modifier
                    .size((12 * scale).dp, (3 * scale).dp)
                    .background(WolfieNoseBlack)
                    .align(Alignment.BottomCenter)
                    .offset(y = -(12 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Happy Pose - Big smile, warm expression
@Composable
private fun WolfieHappy(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((40 * scale).dp))
        ) {
            // Happy eyes - crescent shape
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (18 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((14 * scale).dp, (8 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape(bottomEnd = (7 * scale).dp, bottomStart = (7 * scale).dp))
                )
                Box(
                    modifier = Modifier
                        .size((14 * scale).dp, (8 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape(bottomEnd = (7 * scale).dp, bottomStart = (7 * scale).dp))
                )
            }

            // Big smile
            Box(
                modifier = Modifier
                    .size((28 * scale).dp, (12 * scale).dp)
                    .background(
                        WolfieNoseBlack,
                        RoundedCornerShape(bottomStart = (14 * scale).dp, bottomEnd = (14 * scale).dp)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = -(8 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Celebrating Pose - Arms up, joyful
@Composable
private fun WolfieCelebrating(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((40 * scale).dp))
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (18 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((14 * scale).dp, (8 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape(bottomEnd = (7 * scale).dp, bottomStart = (7 * scale).dp))
                )
                Box(
                    modifier = Modifier
                        .size((14 * scale).dp, (8 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape(bottomEnd = (7 * scale).dp, bottomStart = (7 * scale).dp))
                )
            }

            Box(
                modifier = Modifier
                    .size((28 * scale).dp, (12 * scale).dp)
                    .background(
                        WolfieNoseBlack,
                        RoundedCornerShape(bottomStart = (14 * scale).dp, bottomEnd = (14 * scale).dp)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = -(8 * scale).dp)
            )
        }

        // Arms celebrating
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((20 * scale).dp),
            horizontalArrangement = Arrangement.spacedBy((10 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size((12 * scale).dp, (30 * scale).dp)
                    .background(WolfieGray, RoundedCornerShape((6 * scale).dp))
                    .rotate(-30f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size((12 * scale).dp, (30 * scale).dp)
                    .background(WolfieGray, RoundedCornerShape((6 * scale).dp))
                    .rotate(30f)
            )
        }

        Spacer(modifier = Modifier.height((4 * scale).dp))

        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Meditating Pose - Peaceful, closed eyes
@Composable
private fun WolfieMeditating(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(SageGlow, RoundedCornerShape((40 * scale).dp))
        ) {
            // Closed peaceful eyes
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (20 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp, (4 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape((2 * scale).dp))
                )
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp, (4 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape((2 * scale).dp))
                )
            }

            // Peaceful smile
            Box(
                modifier = Modifier
                    .size((20 * scale).dp, (8 * scale).dp)
                    .background(
                        WolfieNoseBlack,
                        RoundedCornerShape(bottomStart = (10 * scale).dp, bottomEnd = (10 * scale).dp)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = -(8 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(SageGlow, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Sleeping Pose - Peaceful, z's floating
@Composable
private fun WolfieSleeping(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((40 * scale).dp))
                .rotate(-15f)
        ) {
            // Sleeping closed eyes
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (20 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp, (4 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape((2 * scale).dp))
                )
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp, (4 * scale).dp)
                        .background(WolfieNoseBlack, RoundedCornerShape((2 * scale).dp))
                )
            }

            // Peaceful sleeping smile
            Box(
                modifier = Modifier
                    .size((18 * scale).dp, (6 * scale).dp)
                    .background(
                        WolfieNoseBlack,
                        RoundedCornerShape(bottomStart = (9 * scale).dp, bottomEnd = (9 * scale).dp)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = -(10 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Typing Pose - Focused, engaged
@Composable
private fun WolfieTyping(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((40 * scale).dp))
        ) {
            // Focused eyes - looking down
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (20 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.BottomCenter)
                            .offset(x = (2 * scale).dp, y = -(2 * scale).dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.BottomCenter)
                            .offset(x = (2 * scale).dp, y = -(2 * scale).dp)
                    )
                }
            }

            // Focused mouth
            Box(
                modifier = Modifier
                    .size((10 * scale).dp, (3 * scale).dp)
                    .background(WolfieNoseBlack)
                    .align(Alignment.BottomCenter)
                    .offset(y = -(12 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(WolfieGray, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Support Pose - Warm and encouraging
@Composable
private fun WolfieSupport(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .background(SoftLavender, RoundedCornerShape((40 * scale).dp))
        ) {
            // Warm supportive eyes
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (20 * scale).dp),
                horizontalArrangement = Arrangement.spacedBy((15 * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.Center)
                            .offset(x = (2 * scale).dp, y = (1 * scale).dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size((12 * scale).dp)
                        .background(WolfieEyeWhite, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * scale).dp)
                            .background(WolfieNoseBlack, CircleShape)
                            .align(Alignment.Center)
                            .offset(x = (2 * scale).dp, y = (1 * scale).dp)
                    )
                }
            }

            // Warm smile
            Box(
                modifier = Modifier
                    .size((20 * scale).dp, (8 * scale).dp)
                    .background(
                        WolfieNoseBlack,
                        RoundedCornerShape(bottomStart = (10 * scale).dp, bottomEnd = (10 * scale).dp)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = -(8 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        Box(
            modifier = Modifier
                .size((70 * scale).dp)
                .background(SoftLavender, RoundedCornerShape((35 * scale).dp))
        )
    }
}

// Wolfie Message Bubble with Emotion
@Composable
fun WolfieMessageBubble(
    message: String,
    emotion: WolfieEmotion = WolfieEmotion.SUPPORTIVE,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PremiumDesignSystem.Spacing.lg),
        horizontalAlignment = Alignment.Start
    ) {
        // Wolfie with message
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(PremiumDesignSystem.Spacing.md)
        ) {
            WolfieCharacter(
                emotion = emotion,
                size = WolfieSize.SMALL
            )

            // Message bubble
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = SageGlow,
                        shape = RoundedCornerShape(
                            topStart = PremiumDesignSystem.CornerRadius.large,
                            topEnd = PremiumDesignSystem.CornerRadius.large,
                            bottomEnd = PremiumDesignSystem.CornerRadius.large
                        )
                    )
                    .padding(PremiumDesignSystem.Spacing.md)
            ) {
                Text(
                    text = message,
                    style = PremiumDesignSystem.TextStyles.bodyMedium,
                    fontFamily = NotoSansDevanagariFamily
                )
            }
        }
    }
}
