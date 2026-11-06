package com.example.ui.filter

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.StudyClassifierRepository
import com.example.data.ai.VideoClassificationDecision
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
import com.example.viewmodel.RegainViewModel
import com.example.viewmodel.VideoEvalState

@Composable
fun StudyFilterScreen(
    viewModel: RegainViewModel
) {
    val fastState by viewModel.fastState.collectAsStateWithLifecycle()
    val evalState by viewModel.videoEvalState.collectAsStateWithLifecycle()
    val cachedVideos by viewModel.cachedVideos.collectAsStateWithLifecycle()
    val whitelistItems by viewModel.whitelistItems.collectAsStateWithLifecycle()
    val studyNotes by viewModel.studyNotes.collectAsStateWithLifecycle()

    var customUrlInput by remember { mutableStateOf("") }
    var customTitleInput by remember { mutableStateOf("") }
    var customChannelInput by remember { mutableStateOf("") }
    var isCacheDrawerExpanded by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Header Banner (Liquid Glass Container)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "AI Study Tube",
                            tint = MintBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart YouTube Study Filter & Shorts Blocker",
                            style = MaterialTheme.typography.titleLarge,
                            color = SoftMintText
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pipeline: 1. Shorts URL Blocker -> 2. Whitelist (<1ms) -> 3. Local Room SQLite Cache (<2ms) -> 4. Gemini 3.8 Flash JSON Schema -> 5. Distraction-Free Player.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSageText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Evaluation Result / Distraction-Free Player Section
            when (val currentEval = evalState) {
                is VideoEvalState.Evaluating -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MintBright, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldElevatedGlass)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = MintBright,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Inspecting: ${currentEval.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftMintText
                                )
                                Text(
                                    text = currentEval.stepMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSageText
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
                is VideoEvalState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CoralAlert, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Classification Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = CoralAlert
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentEval.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftMintText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
                is VideoEvalState.Result -> {
                    val decision = currentEval.decision
                    if (decision.isStudyRelated && !decision.isShortsBlocked) {
                        DistractionFreeStudyPlayerCard(
                            decision = decision,
                            studyNotes = studyNotes,
                            onNotesChange = { viewModel.updateStudyNotes(it) },
                            onClosePlayer = { viewModel.resetVideoEvaluation() }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
                else -> {}
            }

            // Custom YouTube URL / Title + Channel Input Card (Liquid Glass Container)
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
                        text = "Paste YouTube URL, Shorts Link, or Video Title",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftMintText
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("YouTube URL or /shorts/ Link") },
                        placeholder = { Text("https://youtube.com/watch?v=... or /shorts/...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintBright,
                            unfocusedBorderColor = SubtleGreenBorder,
                            focusedTextColor = SoftMintText,
                            unfocusedTextColor = SoftMintText
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customTitleInput,
                            onValueChange = { customTitleInput = it },
                            label = { Text("Video Title / Topic") },
                            placeholder = { Text("e.g. Fourier Transform") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("youtube_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintBright,
                                unfocusedBorderColor = SubtleGreenBorder,
                                focusedTextColor = SoftMintText,
                                unfocusedTextColor = SoftMintText
                            )
                        )
                        OutlinedTextField(
                            value = customChannelInput,
                            onValueChange = { customChannelInput = it },
                            label = { Text("Channel Name") },
                            placeholder = { Text("e.g. 3Blue1Brown") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("youtube_channel_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintBright,
                                unfocusedBorderColor = SubtleGreenBorder,
                                focusedTextColor = SoftMintText,
                                unfocusedTextColor = SoftMintText
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val query = customUrlInput.ifBlank { customTitleInput }
                            if (query.isNotBlank()) {
                                viewModel.evaluateYoutubeVideo(
                                    urlOrQuery = customUrlInput.ifBlank { "https://youtube.com/watch?v=custom" },
                                    title = customTitleInput.ifBlank { customUrlInput },
                                    channelName = customChannelInput.ifBlank { "YouTube Channel" }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("classify_video_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintBright,
                            contentColor = Color(0xFF060B1E)
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Evaluate Video")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Run AI Study Classifier Pipeline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8 Realistic Preset Educational & Entertainment Test Videos
            Text(
                text = "8 Preset Test Videos (Tap to Test Pipeline & Cache)",
                style = MaterialTheme.typography.titleLarge,
                color = SoftMintText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Test lectures, CAD tutorials, gaming montages, music videos, and YouTube Shorts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSageText
            )
            Spacer(modifier = Modifier.height(10.dp))

            StudyClassifierRepository.PRESET_TEST_VIDEOS.forEach { preset ->
                val isCached = cachedVideos.any { it.videoId == preset.videoId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .border(1.dp, SubtleGreenBorder, RoundedCornerShape(18.dp))
                        .clickable {
                            viewModel.evaluateYoutubeVideo(
                                urlOrQuery = preset.url,
                                title = preset.title,
                                channelName = preset.channelName
                            )
                        }
                        .testTag("preset_video_${preset.videoId}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (preset.isShortsUrl) CoralAlert.copy(alpha = 0.2f) else MintPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (preset.isShortsUrl) Icons.Default.Block else Icons.Default.PlayArrow,
                                    contentDescription = preset.title,
                                    tint = if (preset.isShortsUrl) CoralAlert else MintBright
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftMintText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${preset.channelName} • ${preset.durationLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSageText
                            )
                            if (isCached) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚡ Cached in Room SQLite (<2ms)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintBright
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Database Cache Inspector Drawer at the Bottom
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MintBright.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldElevatedGlass)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCacheDrawerExpanded = !isCacheDrawerExpanded }
                            .testTag("toggle_cache_inspector_drawer"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Database Cache Inspector",
                                tint = MintBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Live Database Cache Inspector",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftMintText
                                )
                                Text(
                                    text = "Room video_cache (${cachedVideos.size}) • whitelist (${whitelistItems.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintBright
                                )
                            }
                        }
                        IconButton(onClick = { isCacheDrawerExpanded = !isCacheDrawerExpanded }) {
                            Icon(
                                imageVector = if (isCacheDrawerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Inspector",
                                tint = SoftMintText
                            )
                        }
                    }

                    AnimatedVisibility(visible = isCacheDrawerExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "video_cache Table (<2ms Instant Lookup)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SoftMintText
                                )
                                if (cachedVideos.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { viewModel.clearAllVideoCache() },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = "Clear Cache",
                                            tint = CoralAlert,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear", style = MaterialTheme.typography.labelSmall, color = SoftMintText)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (cachedVideos.isEmpty()) {
                                Text(
                                    text = "No videos cached yet. Tap any preset video above to populate video_cache!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSageText
                                )
                            } else {
                                cachedVideos.take(6).forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DeepForestBg)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = entry.videoTitle,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = SoftMintText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${entry.channelName} • ${entry.category}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedSageText
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (entry.isStudyRelated) MintPrimary.copy(alpha = 0.25f) else CoralAlert.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = if (entry.isStudyRelated) "STUDY" else "DISTRACTION",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (entry.isStudyRelated) MintBright else CoralAlert,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-Screen App Block / YouTube Shorts Blocked Overlay when Distraction or Short is detected
    val currentResult = (evalState as? VideoEvalState.Result)?.decision
    if (currentResult != null && (currentResult.isShortsBlocked || !currentResult.isStudyRelated)) {
        FullScreenBlockOverlayDialog(
            decision = currentResult,
            shortsBlockedCount = fastState.shortsBlockedCount,
            onDismiss = { viewModel.resetVideoEvaluation() }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun DistractionFreeStudyPlayerCard(
    decision: VideoClassificationDecision,
    studyNotes: String,
    onNotesChange: (String) -> Unit,
    onClosePlayer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MintBright, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cache / Source Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = MintPrimary.copy(alpha = 0.25f),
                modifier = Modifier.testTag("cache_source_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MintBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = decision.pipelineSource,
                        style = MaterialTheme.typography.labelSmall,
                        color = MintBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Approved Study Video",
                    tint = MintBright
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = decision.videoTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftMintText
                    )
                    Text(
                        text = "${decision.channelName} • ${decision.category} (${(decision.confidence * 100).toInt()}% match)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSageText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clean Embedded Distraction-Free YouTube Iframe Player (No Shorts, No Comments, No Sidebar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(1.dp, SubtleGreenBorder, RoundedCornerShape(16.dp))
            ) {
                val safeVideoId = decision.videoId.takeIf { it.length in 6..15 && !it.contains("_") } ?: "cdZnhQjJu4I"
                val embedHtml = remember(safeVideoId, decision.videoTitle) {
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                      body { margin: 0; background: #04130E; color: #ECFDF5; font-family: sans-serif; display: flex; flex-direction: column; height: 100vh; overflow: hidden; }
                      .topbar { background: #0A241C; padding: 8px 12px; font-size: 12px; color: #34D399; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #235C4B; }
                      iframe { width: 100%; flex: 1; border: none; }
                    </style>
                    </head>
                    <body>
                      <div class="topbar">
                        <span>🛡️ Regain Distraction-Free Study Player</span>
                        <span>Comments & Shorts Stripped</span>
                      </div>
                      <iframe src="https://www.youtube-nocookie.com/embed/$safeVideoId?rel=0&modestbranding=1&iv_load_policy=3" allowfullscreen></iframe>
                    </body>
                    </html>
                    """.trimIndent()
                }

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL(
                                "https://www.youtube-nocookie.com",
                                embedHtml,
                                "text/html",
                                "utf-8",
                                null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "AI Verification: ${decision.reason}",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSageText
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = studyNotes,
                onValueChange = onNotesChange,
                label = { Text("Lecture Scratchpad & Key Takeaways") },
                placeholder = { Text("Jot down formulas, timestamps, or key concepts while watching...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintBright,
                    unfocusedBorderColor = SubtleGreenBorder,
                    focusedTextColor = SoftMintText,
                    unfocusedTextColor = SoftMintText
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClosePlayer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close Study Player", color = SoftMintText)
            }
        }
    }
}

@Composable
private fun FullScreenBlockOverlayDialog(
    decision: VideoClassificationDecision,
    shortsBlockedCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepForestBg.copy(alpha = 0.97f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .border(2.dp, CoralAlert, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CoralAlert.copy(alpha = 0.2f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (decision.isShortsBlocked) Icons.Default.Block else Icons.Default.Shield,
                                contentDescription = "Blocked",
                                tint = CoralAlert,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = EmeraldElevatedGlass
                    ) {
                        Text(
                            text = decision.pipelineSource,
                            style = MaterialTheme.typography.labelSmall,
                            color = MintBright,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (decision.isShortsBlocked) {
                            "YouTube Shorts Blocked — Stay Focused!"
                        } else {
                            "Distraction Blocked by Regain AI"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = SoftMintText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"${decision.videoTitle}\" (${decision.channelName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberWarning,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepForestBg)
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Category: ${decision.category}",
                                style = MaterialTheme.typography.labelLarge,
                                color = CoralAlert
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = decision.reason,
                                style = MaterialTheme.typography.bodyLarge,
                                color = SoftMintText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Total Shorts & Distractions Intercepted: $shortsBlockedCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSageText
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("dismiss_block_overlay_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintBright,
                            contentColor = Color(0xFF060B1E)
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Return to Study Sanctuary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
