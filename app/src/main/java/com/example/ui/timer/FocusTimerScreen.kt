package com.example.ui.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.DeepForestBg
import com.example.ui.theme.EmeraldCardGlass
import com.example.ui.theme.EmeraldElevatedGlass
import com.example.ui.theme.LiquidGlassShapeType
import com.example.ui.theme.MintBright
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MutedSageText
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.SubtleGreenBorder
import com.example.ui.theme.liquidGlassEffect
import com.example.util.SocialSharePreviewModal
import com.example.util.SocialShareStats
import com.example.viewmodel.QuizUiState
import com.example.viewmodel.RegainViewModel

private val SUBJECT_TAGS = listOf("Engineering", "Coding", "Exam Prep", "Mathematics", "Physics")
private val TIMER_PRESETS = listOf(25, 50, 90)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusTimerScreen(
    viewModel: RegainViewModel
) {
    val fastState by viewModel.fastState.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val celebrationTrigger by viewModel.streakCelebrationTrigger.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showShareModal by remember { mutableStateOf(false) }

    // Real-Time App/Tab Switch Detection using Android LifecycleEventObserver
    DisposableEffect(lifecycleOwner, fastState.isStudyModeActive) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && fastState.isStudyModeActive) {
                viewModel.onAppBackgroundedOrTabSwitched("Lifecycle ON_PAUSE / App Switch")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val totalSec = (fastState.totalDurationMinutes * 60f).coerceAtLeast(1f)
    val progressTarget = if (fastState.isStudyModeActive) {
        (remainingSeconds.toFloat() / totalSec).coerceIn(0f, 1f)
    } else {
        1f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 500),
        label = "timerProgress"
    )

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formattedTime = "%02d:%02d".format(mins, secs)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fast State Sub-Millisecond Status Pill (Liquid Glass Capsule)
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            modifier = Modifier.liquidGlassEffect(
                shapeType = LiquidGlassShapeType.PILL,
                accentTint = MintBright,
                isNested = false
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Fast State Sync",
                    tint = MintBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "fast_state (<0.2ms) • Shorts Blocked: ${fastState.shortsBlockedCount} • Distractions: ${fastState.totalDistractionsCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftMintText
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        com.example.ui.components.CompactStreakGoalBanner(
            consecutiveStreakDays = fastState.consecutiveStreakDays,
            todayFocusedMinutes = fastState.todayFocusedMinutes,
            dailyFocusGoalMinutes = fastState.dailyFocusGoalMinutes,
            isTodayGoalMet = fastState.isTodayGoalMet,
            celebrationTrigger = celebrationTrigger,
            onCelebrateClick = { viewModel.triggerStreakCelebration() },
            onShareClick = { showShareModal = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Liquid Glass Circular Optical Pomodoro / Deep Study Timer Lens
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(256.dp)
                .liquidGlassEffect(
                    shapeType = LiquidGlassShapeType.CIRCLE,
                    accentTint = if (fastState.isStudyModeActive) MintBright else AmberWarning,
                    isNested = false
                )
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 13.dp.toPx()
                drawArc(
                    color = Color.White.copy(alpha = 0.10f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(MintPrimary, MintBright, Color(0xFF38BDF8), MintBright, MintPrimary)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (fastState.isStudyModeActive) "DEEP FOCUS ACTIVE" else "LIQUID GLASS FOCUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintBright,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge,
                    color = SoftMintText,
                    modifier = Modifier.testTag("timer_countdown_text")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier.liquidGlassEffect(
                        shapeType = LiquidGlassShapeType.PILL,
                        accentTint = MintBright,
                        isNested = true
                    )
                ) {
                    Text(
                        text = fastState.activeSubject,
                        style = MaterialTheme.typography.labelLarge,
                        color = SoftMintText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Timer Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!fastState.isStudyModeActive) {
                Button(
                    onClick = {
                        viewModel.startFocusSession(
                            durationMinutes = fastState.totalDurationMinutes,
                            subjectTag = fastState.activeSubject,
                            strictMode = fastState.isStrictMode
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("start_focus_timer_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintBright,
                        contentColor = Color(0xFF060B1E)
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Focus")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start ${fastState.totalDurationMinutes}m Focus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                if (fastState.isStrictMode) {
                    Button(
                        onClick = { viewModel.startStrictModeUnlockQuiz() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("unlock_strict_quiz_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberWarning,
                            contentColor = Color(0xFF1F1300)
                        )
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = "Pass AI Quiz to Unlock")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pass 3-Question AI Quiz to Stop",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.stopFocusSessionEarly() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("stop_focus_timer_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralAlert,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Focus")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "End Focus Session",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Preset Durations & Custom Slider Card (Liquid Glass Container)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassEffect(
                    shapeType = LiquidGlassShapeType.ROUNDED,
                    cornerRadius = 24.dp,
                    accentTint = MintBright
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pomodoro & Deep Study Presets",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftMintText
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TIMER_PRESETS.forEach { preset ->
                        val selected = fastState.totalDurationMinutes == preset
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.updateTimerPresetMinutes(preset) },
                            enabled = !fastState.isStudyModeActive,
                            label = {
                                Text(
                                    text = "${preset}m",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preset_${preset}m_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintBright,
                                selectedLabelColor = Color(0xFF060B1E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Custom Duration: ${fastState.totalDurationMinutes} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSageText
                )
                Slider(
                    value = fastState.totalDurationMinutes.toFloat(),
                    onValueChange = { viewModel.updateTimerPresetMinutes(it.toInt()) },
                    valueRange = 5f..120f,
                    steps = 22,
                    enabled = !fastState.isStudyModeActive,
                    colors = SliderDefaults.colors(
                        thumbColor = MintBright,
                        activeTrackColor = MintPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Active Study Subject",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftMintText
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SUBJECT_TAGS.forEach { tag ->
                        val isSelected = fastState.activeSubject == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSubjectTag(tag) },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintPrimary,
                                selectedLabelColor = SoftMintText
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Strict Mode, Shorts Blocker & Tab/App Switch Detection Card (Liquid Glass Container)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassEffect(
                    shapeType = LiquidGlassShapeType.ROUNDED,
                    cornerRadius = 24.dp,
                    accentTint = AmberWarning
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Strict Mode",
                            tint = if (fastState.isStrictMode) AmberWarning else MintBright
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Strict Mode (AI Quiz Lock)",
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftMintText
                            )
                            Text(
                                text = "Prevents canceling timer or editing rules unless you pass a 3-question Gemini quiz on ${fastState.activeSubject}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSageText
                            )
                        }
                    }
                    Switch(
                        checked = fastState.isStrictMode,
                        onCheckedChange = { viewModel.toggleStrictMode(it) },
                        modifier = Modifier.testTag("strict_mode_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1F1300),
                            checkedTrackColor = AmberWarning
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Block YouTube Shorts",
                            tint = MintBright
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "YouTube Shorts Blocker",
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftMintText
                            )
                            Text(
                                text = "Intercepts /shorts/, #shorts, and vertical feeds automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSageText
                            )
                        }
                    }
                    Switch(
                        checked = fastState.blockShorts,
                        onCheckedChange = { viewModel.toggleBlockShorts(it) },
                        modifier = Modifier.testTag("block_shorts_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF060B1E),
                            checkedTrackColor = MintBright
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        if (!fastState.isStudyModeActive) {
                            viewModel.startFocusSession(
                                durationMinutes = fastState.totalDurationMinutes,
                                subjectTag = fastState.activeSubject,
                                strictMode = fastState.isStrictMode
                            )
                        }
                        viewModel.onAppBackgroundedOrTabSwitched("Simulated Tab / App Blur Event")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulate_tab_switch_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Test Tab Switch Detection",
                        tint = AmberWarning
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Tab/App Switch Distraction Alert",
                        color = SoftMintText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    // Strict Mode 3-Question AI Quiz Dialog
    if (quizState !is QuizUiState.Idle) {
        StrictModeQuizDialog(
            subject = fastState.activeSubject,
            quizState = quizState,
            onSelectOption = { qIdx, optIdx -> viewModel.selectQuizAnswer(qIdx, optIdx) },
            onSubmit = { viewModel.submitStrictModeQuiz() },
            onRetry = { viewModel.startStrictModeUnlockQuiz() },
            onDismiss = { viewModel.dismissQuizModal() }
        )
    }

    if (showShareModal) {
        val totalMins = 265 + fastState.todayFocusedMinutes
        SocialSharePreviewModal(
            stats = SocialShareStats(
                studentName = viewModel.currentUserName,
                activeSubject = fastState.activeSubject,
                consecutiveStreakDays = fastState.consecutiveStreakDays,
                bestStreakDays = fastState.bestStreakDays,
                todayFocusedMinutes = fastState.todayFocusedMinutes,
                dailyFocusGoalMinutes = fastState.dailyFocusGoalMinutes,
                totalFocusedHoursFormatted = "%.1f hrs".format(totalMins / 60f),
                shortsBlockedCount = fastState.shortsBlockedCount,
                cloudSessionsCount = 5,
                dailyTrendMinutes = listOf(45, 60, 35, 80, 50, 75, fastState.todayFocusedMinutes.coerceAtLeast(25))
            ),
            onDismiss = { showShareModal = false }
        )
    }
}

@Composable
private fun StrictModeQuizDialog(
    subject: String,
    quizState: QuizUiState,
    onSelectOption: (Int, Int) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EmeraldCardGlass,
        titleContentColor = SoftMintText,
        textContentColor = MutedSageText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = MintBright)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Strict Mode Unlock: $subject Quiz")
            }
        },
        text = {
            when (quizState) {
                is QuizUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MintBright)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Gemini 3.8 Flash is generating a 3-question quiz on $subject...",
                            textAlign = TextAlign.Center,
                            color = SoftMintText
                        )
                    }
                }
                is QuizUiState.Error -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = quizState.message,
                            color = CoralAlert,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is QuizUiState.Passed -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Passed",
                            tint = MintBright,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Strict Mode Unlocked! You demonstrated solid mastery of $subject.",
                            color = SoftMintText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is QuizUiState.Active -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Score at least 2/3 to unlock Strict Mode early.",
                            style = MaterialTheme.typography.labelLarge,
                            color = MintBright
                        )
                        if (quizState.submitted) {
                            Text(
                                text = "You scored ${quizState.score}/${quizState.questions.size}. Review explanations below or generate a new quiz!",
                                color = CoralAlert,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        quizState.questions.forEachIndexed { qIndex, q ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DeepForestBg)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Q${qIndex + 1}. ${q.question}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SoftMintText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                q.options.forEachIndexed { optIndex, optText ->
                                    val isChosen = quizState.selectedOptions[qIndex] == optIndex
                                    val bg = when {
                                        quizState.submitted && optIndex == q.correctIndex -> MintPrimary.copy(alpha = 0.35f)
                                        quizState.submitted && isChosen -> CoralAlert.copy(alpha = 0.35f)
                                        isChosen -> EmeraldElevatedGlass
                                        else -> Color.Transparent
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .clickable { onSelectOption(qIndex, optIndex) }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${('A' + optIndex)}. $optText",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SoftMintText
                                        )
                                    }
                                }
                                if (quizState.submitted && q.explanation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Explanation: ${q.explanation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSageText
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        },
        confirmButton = {
            when (quizState) {
                is QuizUiState.Active -> {
                    if (!quizState.submitted) {
                        Button(
                            onClick = onSubmit,
                            enabled = quizState.selectedOptions.size == quizState.questions.size,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintBright,
                                contentColor = Color(0xFF060B1E)
                            )
                        ) {
                            Text("Submit Answers")
                        }
                    } else {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintBright,
                                contentColor = Color(0xFF060B1E)
                            )
                        ) {
                            Text("Try New Quiz")
                        }
                    }
                }
                is QuizUiState.Error -> {
                    Button(onClick = onRetry) {
                        Text("Retry AI Quiz")
                    }
                }
                else -> {
                    Button(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Studying", color = MutedSageText)
            }
        }
    )
}
