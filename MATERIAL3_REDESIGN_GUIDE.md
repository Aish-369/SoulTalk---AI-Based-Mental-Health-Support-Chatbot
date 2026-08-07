# SoulTalk Material 3 Redesign Implementation Guide

## Objective
Transform SoulTalk into the highest-quality Jetpack Compose UI comparable to Google Material 3 showcase apps, Duolingo, Calm, and Headspace.

## Core Design System (Already Implemented)
- **Color Palette**: Premium warm neutrals, soft pastels, semantic colors
- **Typography**: Poppins (headlines) + Noto Sans Devanagari (body)
- **Spacing**: 4dp, 8dp, 16dp, 24dp, 32dp scale
- **Corners**: 24dp for cards, 26dp for buttons, 20dp for message bubbles
- **Shadows**: Soft elevation 2-4dp on cards, 6dp on floating elements

## Screen-by-Screen Redesign Checklist

### 1. SPLASH SCREEN ✅ 
**Current Status**: Well-designed, shows Wolfie Happy pose
**Key Elements**:
- Animated logo with breathing pulse ✅
- Floating ambient particles ✅
- Wolfie character (Happy pose) ✅
- Three-part tagline (Listen, Reflect, Grow) ✅
- "Enter Sanctuary" button with icons ✅

**Enhancements Needed**:
- Add Material 3 motion specs (300ms transitions)
- Refine button scale animation on tap (0.95x)

---

### 2. ONBOARDING SCREENS ✅
**Current Status**: Good foundation with Wolfie poses
**Key Elements**:
- Full-screen cards ✅
- Wolfie character with emotion-specific poses ✅
- Progress indicator (dots) ✅
- Next button with animation ✅

**Enhancements Needed**:
- Add Material 3 scrim effect (dimmed background) for focus
- Increase button elevation on hover

---

### 3. LOGIN/REGISTER SCREENS 🔄
**Current Status**: Functional, needs Material 3 refinement
**Key Elements**:
- Wolfie character in hero section (Listening pose needed)
- Card-based form layout
- Social login buttons
- Premium input fields

**Enhancements Needed**:
- [ ] Increase input field corners to 16dp
- [ ] Add focus state animation (scale 1.02, shadow elevation)
- [ ] Improve error states with ErrorRed color feedback
- [ ] Add Material 3 filled TextField instead of Outlined (more Material 3 style)
- [ ] Smooth transition between login/register modes
- [ ] Add input validation feedback badges

---

### 4. HOME SCREEN (Dashboard/Companion Home) 🔴
**Current Status**: Needs significant redesign
**Key Elements Missing**:
- [ ] Hero section with Wolfie (Happy pose) + greeting
- [ ] Today's mood large card selector (6 emotions)
- [ ] Quick action cards (Continue Chat, Start Journal, Quick Meditation)
- [ ] Weekly mood graph (clean line chart)
- [ ] Streak counter with celebration badge
- [ ] Floating action button for new message

**Specific Improvements**:
- [ ] Create hero gradient background (Cream to Ivory)
- [ ] Large Wolfie (200dp) with interactive breathing animation
- [ ] Emotion grid with 48sp emoji, haptic feedback on tap
- [ ] Stats cards with Material 3 styling (elevation 4dp)
- [ ] Floating action button positioned bottom-right with label

---

### 5. CHAT SCREEN ✅
**Current Status**: Well-designed with ChatGPT-quality
**Key Elements**:
- Wolfie character display (Listening pose) ✅
- Message bubbles with proper alignment ✅
- Input section with voice button ✅
- Quick action cards ✅

**Enhancements Needed**:
- [ ] Increase message bubble corners to 20dp
- [ ] Add subtle shadow to bubbles (2dp elevation)
- [ ] Typing indicator animation (3 bouncing dots)
- [ ] Smooth message slide-up animation
- [ ] Voice recording waveform animation

---

### 6. MOOD CHECK-IN SCREEN 🔴
**Current Status**: Functional, needs redesign
**Key Elements Missing**:
- [ ] Large Wolfie (Thinking pose) in center
- [ ] Large emotion emoji grid (48sp size)
- [ ] Intensity slider (0-10 scale)
- [ ] Optional "What triggered this?" text input
- [ ] Large save button at bottom

**Specific Improvements**:
- [ ] Make emotion emojis interactive with haptic feedback
- [ ] Add Material 3 slider styling
- [ ] Smooth animation on emotion selection
- [ ] Add success feedback animation after save

---

### 7. JOURNAL SCREEN 🔴
**Current Status**: Needs significant redesign
**Key Elements Missing**:
- [ ] "Your Reflections" header with Wolfie (Listening pose)
- [ ] Entry cards with date, mood emoji, preview text
- [ ] Floating button for new entry
- [ ] Entry detail view with full text

**Specific Improvements**:
- [ ] Card-based layout (24dp corners, 4dp elevation)
- [ ] Rich text rendering with formatting
- [ ] Swipe-to-delete with undo option
- [ ] Smooth entry detail animations

---

### 8. MEDITATION SCREEN 🔴
**Current Status**: Needs significant redesign
**Key Elements Missing**:
- [ ] Large Wolfie (Meditating pose) in hero
- [ ] Duration option cards (5/10/15/20 mins)
- [ ] Ambient sound grid (6 options with icons)
- [ ] Start button (large, prominent)
- [ ] During meditation: breathing circle animation

**Specific Improvements**:
- [ ] Breathing circle expands/contracts (4s cycle)
- [ ] "Inhale"/"Exhale" text animates with circle
- [ ] Gradient background animation matching breath rhythm
- [ ] Wolfie maintains Meditating pose throughout

---

### 9. BREATHING EXERCISE SCREEN 🔴
**Current Status**: Needs significant redesign
**Key Elements Missing**:
- [ ] Large Wolfie (Meditating pose)
- [ ] Animated breathing circle (expanding/contracting)
- [ ] "Inhale"/"Exhale" text indicators
- [ ] Large timer display
- [ ] Duration selector (2/4/5 mins)

**Specific Improvements**:
- [ ] Breathing animation follows 4-second cycle
- [ ] Circle scales from 80dp to 180dp
- [ ] Background gradient pulses with breathing
- [ ] Minimal stop button at bottom

---

### 10. PROFILE SCREEN 🔴
**Current Status**: Needs significant redesign
**Key Elements Missing**:
- [ ] Wolfie (Happy pose) + user name (editable)
- [ ] Statistics section with large numbers
- [ ] Total messages, streak, mood insights
- [ ] Achievement badges grid
- [ ] Mood trend sparkline graph

**Specific Improvements**:
- [ ] Apple Settings-quality design
- [ ] Statistics card with Material 3 styling
- [ ] Editable name with inline edit on tap
- [ ] Achievement badges with unlock animations
- [ ] Mood trend visualization

---

### 11. SETTINGS SCREEN 🔴
**Current Status**: Needs significant redesign
**Key Elements Missing**:
- [ ] Wolfie (Listening pose) in header
- [ ] Card-based sections (notifications, reminders, etc.)
- [ ] Toggle switches for settings
- [ ] Time pickers in bottom sheets
- [ ] Account section with logout/delete buttons

**Specific Improvements**:
- [ ] Consistent card styling (24dp corners)
- [ ] Material 3 switches with animation
- [ ] Bottom sheet for time pickers
- [ ] Destructive buttons in red (ErrorRed)

---

### 12. PREMIUM SCREEN (if exists) 🔴
**Current Status**: Likely not implemented
**Key Elements**:
- [ ] Large Wolfie (Celebrating pose) in hero
- [ ] Features grid (3 columns of cards)
- [ ] Current subscription status badge
- [ ] Upgrade/Renew button (prominent)

---

## Material 3 Implementation Standards

### Button Styling
```
Primary Button:
- Container: SoftLavender (primary)
- Text: PureWhite
- Corners: 26dp
- Padding: 16dp horizontal, 12dp vertical
- Elevation: 2dp default, 4dp hovered
- Scale on press: 0.95x (100ms)

Secondary Button:
- Container: PureWhite
- Border: 1dp SoftSlate.copy(alpha=0.15f)
- Text: QuietCharcoal
- Corners: 26dp
- Elevation: 1dp
```

### Card Styling
```
Standard Card:
- Corners: 24dp
- Elevation: 2-4dp
- Padding: 16dp
- Border: Optional 1dp divider (alpha=0.1f)

Message Bubble:
- Corners: 20dp
- Elevation: 2dp
- User: Right-aligned, SoftLavender container
- Companion: Left-aligned, SoftPeach container
- Padding: 12dp
```

### Input Field Styling
```
Outlined TextField:
- Corners: 16dp
- Focused Border: SageGreen, 2dp
- Unfocused Border: SoftSlate.copy(alpha=0.15f), 1dp
- Container: CalmingBackground
- Label: PoppinsFamily, 13sp, SemiBold
- Placeholder: SoftSlate.copy(alpha=0.6f)
```

### Animations (Material 3 Specs)
```
Screen Transition: 300ms (Fade + Slide)
Button Press: 100ms (Scale 0.95x)
Pose Change: 200ms (Crossfade)
Message Arrival: 300ms (SlideUp + Fade)
List Item: 150ms (SlideIn from bottom)
```

## Implementation Priority

**Phase 1 (Critical - Foundation)**:
1. HomeScreen redesign (hero, stats, quick actions)
2. MoodCheckInScreen (emotion grid, slider)
3. MeditationScreen (breathing animation, sound grid)

**Phase 2 (Core Experience)**:
4. ProfileScreen (user stats, achievements)
5. SettingsScreen (card-based sections)
6. JournalScreen (entry cards, detail view)

**Phase 3 (Polish)**:
7. BreathingExerciseScreen (circle animation)
8. PremiumScreen (if applicable)
9. Animations and micro-interactions across all screens
10. Edge case testing

## Files to Modify

**Critical**:
- `CompanionHomeScreen.kt` - Major redesign
- `FirstMoodCheckInScreen.kt` - New emotion grid
- `MoodTrackingHubScreen.kt` - Stats and charts

**Important**:
- `ProfileScreen.kt` - Apple Settings style
- `SettingsScreen.kt` - Card sections
- `LifeTimelineScreen.kt` (Journal) - Entry cards

**Polish**:
- `CompanionChatScreen.kt` - Refine bubbles
- `VoiceCompanionScreen.kt` - If needed

## Success Metrics

✅ Every screen feels cohesive with Material 3
✅ Wolfie appears meaningfully on every major screen
✅ Animations are smooth (60fps target)
✅ Typography hierarchy is clear
✅ Color palette creates calming, premium feel
✅ No UI elements feel generic or dated
✅ Touch feedback is immediate and delightful
