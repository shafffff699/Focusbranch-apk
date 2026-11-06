package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DeepForestBg
import com.example.ui.theme.EmeraldCardGlass
import com.example.ui.theme.EmeraldElevatedGlass
import com.example.ui.theme.LiquidGlassShapeType
import com.example.ui.theme.MintBright
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MutedSageText
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.SubtleGreenBorder
import com.example.ui.theme.liquidGlassEffect
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val DAILY_GOAL_OPTIONS = listOf(30, 60, 90, 120)

private data class CelebrationParticle(
    val angleRad: Float,
    val speedFactor: Float,
    val radiusDp: Float,
    val color: Color,
    val spinSpeed: Float,
    val isRect: Boolean
)

@Composable
fun DailyStreakDashboardCard(
    consecutiveStreakDays: Int,
    bestStreakDays: Int,
    todayFocusedMinutes: Int,
    dailyFocusGoalMinutes: Int,
    isTodayGoalMet: Boolean,
    celebrationTrigger: Int,
    onSelectDailyGoal: (Int) -> Unit,
    onLogQuickFocusMinutes: (Int) -> Unit,
    onTriggerCelebration: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val safeGoal = dailyFocusGoalMinutes.coerceAtLeast(10)
    val rawProgress = (todayFocusedMinutes.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "dailyGoalProgress"
    )

    // Celebratory Burst Progress (0f -> 1f over 1800ms whenever celebrationTrigger changes or goal is met)
    val burstAnim = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(1f) }

    LaunchedEffect(celebrationTrigger, isTodayGoalMet) {
        if (celebrationTrigger > 0 || isTodayGoalMet) {
            badgeScale.snapTo(1.28f)
            burstAnim.snapTo(0.01f)
            badgeScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            burstAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
            )
            burstAnim.snapTo(0f)
        }
    }

    // Continuous flame aura pulse
    val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )

    val particles = remember {
        val palette = listOf(
            MintBright,
            AmberWarning,
            Color(0xFFFBBF24),
            SageAccent,
            Color(0xFF38BDF8)
        )
        val rng = Random(2026)
        List(44) { i ->
            CelebrationParticle(
                angleRad = ((i * (360f / 44f)) + rng.nextFloat() * 12f) * (Math.PI.toFloat() / 180f),
                speedFactor = 0.45f + rng.nextFloat() * 0.75f,
                radiusDp = 3.5f + rng.nextFloat() * 4.5f,
                color = palette[i % palette.size],
                spinSpeed = 120f + rng.nextFloat() * 240f,
                isRect = i % 2 == 0
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.ROUNDED,
                cornerRadius = 26.dp,
                accentTint = if (isTodayGoalMet) AmberWarning else MintBright,
                isNested = false
            )
            .testTag("streak_counter_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Celebratory Particle Confetti & Glowing Ember Canvas Overlay
            if (burstAnim.value > 0f && burstAnim.value < 1f) {
                val t = burstAnim.value
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .testTag("streak_celebration_canvas")
                ) {
                    val originX = size.width * 0.22f
                    val originY = size.height * 0.28f
                    val maxRadius = size.width * 0.75f
                    val alpha = (1f - t).coerceIn(0f, 1f)

                    // Expanding celebratory shockwave ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AmberWarning.copy(alpha = 0.35f * alpha),
                                MintBright.copy(alpha = 0.2f * alpha),
                                Color.Transparent
                            ),
                            center = Offset(originX, originY),
                            radius = (maxRadius * t).coerceAtLeast(10f)
                        ),
                        center = Offset(originX, originY),
                        radius = maxRadius * t
                    )

                    particles.forEach { p ->
                        val distance = maxRadius * p.speedFactor * t
                        val gravityDrop = 140f * t * t
                        val px = originX + cos(p.angleRad) * distance
                        val py = originY + sin(p.angleRad) * distance + gravityDrop
                        val rPx = p.radiusDp.dp.toPx()

                        if (px in 0f..size.width && py in 0f..size.height) {
                            if (p.isRect) {
                                rotate(degrees = p.spinSpeed * t, pivot = Offset(px, py)) {
                                    drawRoundRect(
                                        color = p.color.copy(alpha = alpha),
                                        topLeft = Offset(px - rPx, py - rPx * 0.6f),
                                        size = Size(rPx * 2f, rPx * 1.2f),
                                        cornerRadius = CornerRadius(3f, 3f)
                                    )
                                }
                            } else {
                                drawCircle(
                                    color = p.color.copy(alpha = alpha),
                                    radius = rPx,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                // Header Row: Flame Badge + Consecutive Streak Count + Celebrate Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Animated Flame Halo Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .scale(badgeScale.value)
                                .clickable { onTriggerCelebration() }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val r = (size.minDimension / 2f) * haloPulse
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = if (isTodayGoalMet) {
                                            listOf(
                                                AmberWarning.copy(alpha = 0.45f),
                                                MintBright.copy(alpha = 0.20f),
                                                Color.Transparent
                                            )
                                        } else {
                                            listOf(
                                                MintPrimary.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        }
                                    ),
                                    radius = r
                                )
                                drawCircle(
                                    color = if (isTodayGoalMet) AmberWarning else MintBright,
                                    radius = size.minDimension * 0.38f,
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak Flame",
                                tint = if (isTodayGoalMet) AmberWarning else MintBright,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$consecutiveStreakDays Day Streak",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SoftMintText,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("streak_days_value_text")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = AmberWarning.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = AmberWarning,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Best: ${bestStreakDays}d",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AmberWarning
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTodayGoalMet) {
                                    "🎉 Daily focus goal met! Consecutive streak extended!"
                                } else {
                                    "${(safeGoal - todayFocusedMinutes).coerceAtLeast(0)}m more today to extend your streak!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isTodayGoalMet) MintBright else MutedSageText
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onTriggerCelebration,
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("celebrate_streak_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isTodayGoalMet) AmberWarning else MintBright
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Celebrate Streak",
                                tint = if (isTodayGoalMet) AmberWarning else MintBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Celebrate",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftMintText
                            )
                        }
                        if (onShareClick != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onShareClick,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MintPrimary.copy(alpha = 0.25f))
                                    .border(1.dp, MintBright.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .testTag("streak_card_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Streak Progress",
                                    tint = MintBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Celebratory Goal Achieved Banner
                AnimatedVisibility(visible = isTodayGoalMet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        AmberWarning.copy(alpha = 0.22f),
                                        MintPrimary.copy(alpha = 0.25f)
                                    )
                                )
                            )
                            .border(1.dp, AmberWarning.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Milestone Unlocked! You hit $todayFocusedMinutes/${safeGoal}m today!",
                                style = MaterialTheme.typography.labelLarge,
                                color = SoftMintText
                            )
                        }
                        Text(
                            text = "🔥 +1 DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberWarning,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Daily Goal Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Focus Goal: $todayFocusedMinutes / $safeGoal mins",
                        style = MaterialTheme.typography.labelLarge,
                        color = SoftMintText
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isTodayGoalMet) AmberWarning else MintBright
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DeepForestBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isTodayGoalMet) {
                                        listOf(MintPrimary, MintBright, AmberWarning)
                                    } else {
                                        listOf(MintPrimary, MintBright)
                                    }
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7-Day Consecutive Goal Timeline Row
                Text(
                    text = "7-Day Consecutive Daily Goal Chain",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSageText
                )
                Spacer(modifier = Modifier.height(8.dp))

                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEachIndexed { index, label ->
                        val isToday = index == days.lastIndex
                        val met = if (isToday) isTodayGoalMet else true
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isToday && met -> AmberWarning
                                    met -> MintPrimary.copy(alpha = 0.35f)
                                    else -> DeepForestBg
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(
                                        width = if (isToday) 1.5.dp else 1.dp,
                                        color = when {
                                            isToday && met -> AmberWarning
                                            met -> MintBright
                                            else -> SubtleGreenBorder
                                        },
                                        shape = CircleShape
                                    )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (met) {
                                        Icon(
                                            imageVector = if (isToday) Icons.Default.LocalFireDepartment else Icons.Default.Check,
                                            contentDescription = "$label Goal Met",
                                            tint = if (isToday) Color(0xFF1F1300) else MintBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${(animatedProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedSageText
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) SoftMintText else MutedSageText,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Daily Focus Goal Target Chips + Quick Goal Completion Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DAILY_GOAL_OPTIONS.forEach { goalOpt ->
                            val selected = dailyFocusGoalMinutes == goalOpt
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectDailyGoal(goalOpt) },
                                label = {
                                    Text(
                                        text = "${goalOpt}m",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintPrimary,
                                    selectedLabelColor = SoftMintText
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { onLogQuickFocusMinutes(25) },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("log_streak_progress_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintBright,
                            contentColor = Color(0xFF060B1E)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 0.dp
                        )
                    ) {
                        Text(
                            text = "+25m Goal",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactStreakGoalBanner(
    consecutiveStreakDays: Int,
    todayFocusedMinutes: Int,
    dailyFocusGoalMinutes: Int,
    isTodayGoalMet: Boolean,
    celebrationTrigger: Int,
    onCelebrateClick: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val safeGoal = dailyFocusGoalMinutes.coerceAtLeast(10)
    val progress by animateFloatAsState(
        targetValue = (todayFocusedMinutes.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "compactStreakProgress"
    )

    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(celebrationTrigger, isTodayGoalMet) {
        if (celebrationTrigger > 0 || isTodayGoalMet) {
            scaleAnim.snapTo(1.12f)
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.ROUNDED,
                cornerRadius = 20.dp,
                accentTint = if (isTodayGoalMet) AmberWarning else MintBright,
                isNested = false
            )
            .clickable { onCelebrateClick() }
            .testTag("compact_streak_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = if (isTodayGoalMet) AmberWarning.copy(alpha = 0.25f) else MintPrimary.copy(alpha = 0.25f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = if (isTodayGoalMet) AmberWarning else MintBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 $consecutiveStreakDays Day Focus Streak",
                            style = MaterialTheme.typography.labelLarge,
                            color = SoftMintText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTodayGoalMet) "🎉 Goal Met ($todayFocusedMinutes/${safeGoal}m)" else "$todayFocusedMinutes/${safeGoal}m today",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTodayGoalMet) AmberWarning else MintBright
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(DeepForestBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = if (isTodayGoalMet) {
                                            listOf(MintBright, AmberWarning)
                                        } else {
                                            listOf(MintPrimary, MintBright)
                                        }
                                    )
                                )
                        )
                    }
                }
            }
            if (onShareClick != null) {
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MintPrimary.copy(alpha = 0.28f))
                        .border(1.dp, MintBright.copy(alpha = 0.5f), CircleShape)
                        .testTag("compact_streak_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Progress Socials",
                        tint = MintBright,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
