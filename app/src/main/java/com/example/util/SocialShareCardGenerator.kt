package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.theme.AmberWarning
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
import java.io.File
import java.io.FileOutputStream

enum class SocialCardFormat(
    val label: String,
    val subtitle: String,
    val widthPx: Int,
    val heightPx: Int,
    val previewAspect: Float
) {
    STORY_9_16(
        label = "9:16 Story / Status",
        subtitle = "Instagram Story & WhatsApp Status",
        widthPx = 1080,
        heightPx = 1920,
        previewAspect = 9f / 16f
    ),
    PORTRAIT_4_5(
        label = "4:5 Feed Card",
        subtitle = "Instagram Feed & Facebook Post",
        widthPx = 1080,
        heightPx = 1350,
        previewAspect = 4f / 5f
    ),
    LANDSCAPE_16_9(
        label = "16:9 Wide Card",
        subtitle = "X (Twitter) & LinkedIn Post",
        widthPx = 1280,
        heightPx = 720,
        previewAspect = 16f / 9f
    )
}

enum class SocialPlatform(
    val displayName: String,
    val targetPackage: String?,
    val badgeColor: Color
) {
    INSTAGRAM("Instagram", "com.instagram.android", Color(0xFFE1306C)),
    WHATSAPP("WhatsApp Status", "com.whatsapp", Color(0xFF25D366)),
    TWITTER_X("X (Twitter)", "com.twitter.android", Color(0xFF38BDF8)),
    FACEBOOK("Facebook", "com.facebook.katana", Color(0xFF1877F2)),
    SYSTEM_MORE("Share Anywhere", null, MintBright)
}

data class SocialShareStats(
    val studentName: String,
    val activeSubject: String,
    val consecutiveStreakDays: Int,
    val bestStreakDays: Int,
    val todayFocusedMinutes: Int,
    val dailyFocusGoalMinutes: Int,
    val totalFocusedHoursFormatted: String,
    val shortsBlockedCount: Int,
    val cloudSessionsCount: Int,
    val dailyTrendMinutes: List<Int>
) {
    fun buildShareCaption(): String {
        val goalPct = ((todayFocusedMinutes.toFloat() / dailyFocusGoalMinutes.coerceAtLeast(1)) * 100).toInt()
        return "🔥 ${consecutiveStreakDays}-Day Study Streak on Regain Focus!\n" +
            "📚 Subject: $activeSubject\n" +
            "⏱️ Today's Focus: ${todayFocusedMinutes}/${dailyFocusGoalMinutes}m ($goalPct%) • Total: $totalFocusedHoursFormatted\n" +
            "🛡️ YouTube Shorts & Distractions Blocked: $shortsBlockedCount\n" +
            "#RegainFocus #StudyStreak #DeepWork #$activeSubject"
    }
}

object SocialShareCardGenerator {

    /**
     * Generates a high-resolution visual summary card Bitmap using the native Android Canvas API.
     */
    fun renderShareCardBitmap(
        stats: SocialShareStats,
        format: SocialCardFormat
    ): Bitmap {
        val w = format.widthPx
        val h = format.heightPx
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Cosmic Obsidian to Indigo Gradient Background
        paint.shader = LinearGradient(
            0f,
            0f,
            w.toFloat(),
            h.toFloat(),
            intArrayOf(
                AndroidColor.parseColor("#050816"),
                AndroidColor.parseColor("#0E1738"),
                AndroidColor.parseColor("#070B1E")
            ),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // 2. Ambient Glowing Decorative Prismatic Orbs
        val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        orbPaint.color = AndroidColor.parseColor("#2E38BDF8")
        canvas.drawCircle(w * 0.85f, h * 0.15f, w * 0.34f, orbPaint)
        orbPaint.color = AndroidColor.parseColor("#28A855F7")
        canvas.drawCircle(w * 0.15f, h * 0.82f, w * 0.30f, orbPaint)

        // 3. Outer Liquid Glass Card Frame
        val pad = w * 0.055f
        val cardRect = RectF(pad, pad, w - pad, h - pad)
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#D8111A3A")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardBgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            shader = LinearGradient(
                cardRect.left,
                cardRect.top,
                cardRect.right,
                cardRect.bottom,
                AndroidColor.parseColor("#F8FAFC"),
                AndroidColor.parseColor("#38BDF8"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, borderPaint)

        val isLandscape = format == SocialCardFormat.LANDSCAPE_16_9
        val innerLeft = cardRect.left + w * 0.05f
        val innerRight = cardRect.right - w * 0.05f
        var cursorY = cardRect.top + (if (isLandscape) h * 0.08f else h * 0.055f)

        // 4. Top Brand Header + Student Subject Pill
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#38BDF8")
            textSize = if (isLandscape) 26f else 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        canvas.drawText("🛡️ REGAIN FOCUS • LIQUID GLASS SANCTUARY", innerLeft, cursorY, titlePaint)

        cursorY += if (isLandscape) 42f else 54f
        val studentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#F8FAFC")
            textSize = if (isLandscape) 36f else 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("${stats.studentName} • ${stats.activeSubject}", innerLeft, cursorY, studentPaint)

        // 5. Hero Consecutive Streak Banner Box
        cursorY += if (isLandscape) 32f else 48f
        val heroHeight = if (isLandscape) h * 0.30f else h * 0.22f
        val heroRect = RectF(innerLeft, cursorY, innerRight, cursorY + heroHeight)

        val heroFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                heroRect.left,
                heroRect.top,
                heroRect.right,
                heroRect.bottom,
                AndroidColor.parseColor("#233263"),
                AndroidColor.parseColor("#162044"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(heroRect, 36f, 36f, heroFill)

        val heroStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = AndroidColor.parseColor("#38BDF8")
        }
        canvas.drawRoundRect(heroRect, 36f, 36f, heroStroke)

        // Circular Streak Ring inside Hero Box
        val ringCenterY = heroRect.centerY()
        val ringRadius = heroHeight * 0.33f
        val ringCenterX = heroRect.left + ringRadius + 44f

        val ringTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 16f
            color = AndroidColor.parseColor("#0B1126")
        }
        canvas.drawCircle(ringCenterX, ringCenterY, ringRadius, ringTrackPaint)

        val goalRatio = (stats.todayFocusedMinutes.toFloat() / stats.dailyFocusGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
        val ringArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 16f
            strokeCap = Paint.Cap.ROUND
            color = AndroidColor.parseColor("#38BDF8")
        }
        val arcBounds = RectF(
            ringCenterX - ringRadius,
            ringCenterY - ringRadius,
            ringCenterX + ringRadius,
            ringCenterY + ringRadius
        )
        canvas.drawArc(arcBounds, -90f, 360f * goalRatio.coerceAtLeast(0.15f), false, ringArcPaint)

        val ringEmojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = ringRadius * 0.85f
            color = AndroidColor.parseColor("#F8FAFC")
        }
        canvas.drawText("🔥", ringCenterX, ringCenterY + ringRadius * 0.30f, ringEmojiPaint)

        // Hero Text Right of Ring
        val heroTextX = ringCenterX + ringRadius + 42f
        val streakHeadlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#F8FAFC")
            textSize = if (isLandscape) 48f else 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            "${stats.consecutiveStreakDays} DAY STREAK",
            heroTextX,
            heroRect.top + heroHeight * 0.44f,
            streakHeadlinePaint
        )

        val streakSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#FBBF24")
            textSize = if (isLandscape) 26f else 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            "Personal Best: ${stats.bestStreakDays} Days • Daily Goal ${(goalRatio * 100).toInt()}%",
            heroTextX,
            heroRect.top + heroHeight * 0.68f,
            streakSubPaint
        )

        val goalDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#CBD5E1")
            textSize = if (isLandscape) 22f else 28f
        }
        canvas.drawText(
            "Today's Focus: ${stats.todayFocusedMinutes} / ${stats.dailyFocusGoalMinutes} mins completed",
            heroTextX,
            heroRect.top + heroHeight * 0.86f,
            goalDetailPaint
        )

        cursorY = heroRect.bottom + (if (isLandscape) 26f else 42f)

        // 6. 4-Tile Session Stats Grid (2x2 or 4x1 in Landscape)
        val statItems = listOf(
            Triple("TOTAL FOCUSED", stats.totalFocusedHoursFormatted, "Deep Work Logged"),
            Triple("TODAY'S GOAL", "${stats.todayFocusedMinutes}/${stats.dailyFocusGoalMinutes}m", "Daily Target"),
            Triple("SHORTS BLOCKED", "${stats.shortsBlockedCount}", "Zero Doomscrolling"),
            Triple("STUDY SESSIONS", "${stats.cloudSessionsCount + 6}", "Completed Blocks")
        )

        val gap = 22f
        if (isLandscape) {
            val tileW = (innerRight - innerLeft - gap * 3f) / 4f
            val tileH = h * 0.22f
            statItems.forEachIndexed { idx, (label, value, sub) ->
                val left = innerLeft + idx * (tileW + gap)
                val rect = RectF(left, cursorY, left + tileW, cursorY + tileH)
                drawStatTileOnCanvas(canvas, rect, label, value, sub, compact = true)
            }
            cursorY += tileH + 26f
        } else {
            val tileW = (innerRight - innerLeft - gap) / 2f
            val tileH = if (format == SocialCardFormat.STORY_9_16) h * 0.125f else h * 0.145f
            statItems.forEachIndexed { idx, (label, value, sub) ->
                val col = idx % 2
                val row = idx / 2
                val left = innerLeft + col * (tileW + gap)
                val top = cursorY + row * (tileH + gap)
                val rect = RectF(left, top, left + tileW, top + tileH)
                drawStatTileOnCanvas(canvas, rect, label, value, sub, compact = false)
            }
            cursorY += (tileH * 2f) + gap + 42f
        }

        // 7. 7-Day Focus Trend Bar Chart (in Portrait & Story formats)
        if (!isLandscape) {
            val chartBottom = cardRect.bottom - 95f
            val chartTop = cursorY.coerceAtMost(chartBottom - 160f)
            if (chartBottom - chartTop >= 140f) {
                val chartRect = RectF(innerLeft, chartTop, innerRight, chartBottom)
                val chartBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.parseColor("#0D1530")
                }
                canvas.drawRoundRect(chartRect, 28f, 28f, chartBg)

                val chartHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.parseColor("#93C5FD")
                    textSize = 26f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(
                    "7-DAY FOCUS CONSISTENCY (MINUTES)",
                    chartRect.left + 28f,
                    chartRect.top + 42f,
                    chartHeaderPaint
                )

                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today")
                val values = stats.dailyTrendMinutes.takeIf { it.size == 7 }
                    ?: listOf(45, 60, 35, 80, 50, 75, stats.todayFocusedMinutes)
                val maxVal = (values.maxOrNull() ?: 100).coerceAtLeast(80).toFloat()

                val barAreaTop = chartRect.top + 70f
                val barAreaBottom = chartRect.bottom - 48f
                val maxBarH = (barAreaBottom - barAreaTop).coerceAtLeast(40f)
                val slotW = (chartRect.width() - 56f) / 7f

                val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.parseColor("#F8FAFC")
                    textSize = 22f
                    textAlign = Paint.Align.CENTER
                }

                values.forEachIndexed { i, v ->
                    val ratio = (v.toFloat() / maxVal).coerceIn(0.12f, 1f)
                    val bH = maxBarH * ratio
                    val cx = chartRect.left + 28f + slotW * (i + 0.5f)
                    val bW = slotW * 0.50f
                    val bRect = RectF(cx - bW / 2f, barAreaBottom - bH, cx + bW / 2f, barAreaBottom)

                    barPaint.shader = LinearGradient(
                        bRect.left,
                        bRect.top,
                        bRect.left,
                        bRect.bottom,
                        if (i == 6) AndroidColor.parseColor("#E879F9") else AndroidColor.parseColor("#38BDF8"),
                        AndroidColor.parseColor("#6366F1"),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawRoundRect(bRect, 12f, 12f, barPaint)
                    canvas.drawText(days[i], cx, chartRect.bottom - 16f, labelPaint)
                }
            }
        }

        // 8. Footer Watermark
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#93C5FD")
            textSize = if (isLandscape) 22f else 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            "Verified by Regain Focus AI • Zero YouTube Shorts • Collaborative Study",
            w / 2f,
            cardRect.bottom - 32f,
            footerPaint
        )

        return bitmap
    }

    private fun drawStatTileOnCanvas(
        canvas: Canvas,
        rect: RectF,
        label: String,
        value: String,
        subLabel: String,
        compact: Boolean
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#0D1530")
        }
        canvas.drawRoundRect(rect, 26f, 26f, bgPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = AndroidColor.parseColor("#3B82F6")
        }
        canvas.drawRoundRect(rect, 26f, 26f, strokePaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#38BDF8")
            textSize = if (compact) 20f else 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(label, rect.left + 26f, rect.top + (if (compact) 36f else 46f), labelPaint)

        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#F8FAFC")
            textSize = if (compact) 38f else 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(value, rect.left + 26f, rect.top + (if (compact) 86f else 112f), valuePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#CBD5E1")
            textSize = if (compact) 18f else 24f
        }
        canvas.drawText(subLabel, rect.left + 26f, rect.bottom - (if (compact) 20f else 28f), subPaint)
    }

    /**
     * Renders the Canvas card to a PNG in cacheDir and shares it to Instagram, WhatsApp, X/Twitter, Facebook, or the system chooser.
     */
    fun shareProgressCardToSocial(
        context: Context,
        stats: SocialShareStats,
        format: SocialCardFormat,
        platform: SocialPlatform
    ) {
        try {
            val bitmap = renderShareCardBitmap(stats, format)
            val sharedDir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val imageFile = File(sharedDir, "regain_focus_${format.name.lowercase()}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            val caption = stats.buildShareCaption()

            val baseIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, caption)
                putExtra(Intent.EXTRA_TITLE, "Regain Focus Study Streak Card")
                clipData = ClipData.newRawUri("Regain Focus Streak Card", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val targetPkg = platform.targetPackage
            if (targetPkg != null) {
                val targetedIntent = Intent(baseIntent).apply {
                    setPackage(targetPkg)
                }
                if (targetedIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(targetedIntent)
                    return
                }
            }

            val chooser = Intent.createChooser(
                baseIntent,
                "Share Study Streak to ${platform.displayName}"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Prepared study streak card for ${platform.displayName}!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialShareDashboardCard(
    stats: SocialShareStats,
    onOpenShareStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.ROUNDED,
                cornerRadius = 24.dp,
                accentTint = MintBright
            )
            .testTag("social_share_dashboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = MintPrimary.copy(alpha = 0.25f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Progress to Socials",
                                tint = MintBright
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Share Progress to Socials (Canvas Card)",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftMintText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate a visual streak & session stats image for Instagram, WhatsApp Status, X & Facebook.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSageText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Social Destination Pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SocialQuickPill(
                    label = "Instagram Story",
                    accentColor = SocialPlatform.INSTAGRAM.badgeColor,
                    testTag = "quick_share_instagram",
                    onClick = {
                        SocialShareCardGenerator.shareProgressCardToSocial(
                            context = context,
                            stats = stats,
                            format = SocialCardFormat.STORY_9_16,
                            platform = SocialPlatform.INSTAGRAM
                        )
                    }
                )
                SocialQuickPill(
                    label = "WhatsApp Status",
                    accentColor = SocialPlatform.WHATSAPP.badgeColor,
                    testTag = "quick_share_whatsapp",
                    onClick = {
                        SocialShareCardGenerator.shareProgressCardToSocial(
                            context = context,
                            stats = stats,
                            format = SocialCardFormat.STORY_9_16,
                            platform = SocialPlatform.WHATSAPP
                        )
                    }
                )
                SocialQuickPill(
                    label = "X (Twitter)",
                    accentColor = SocialPlatform.TWITTER_X.badgeColor,
                    testTag = "quick_share_twitter",
                    onClick = {
                        SocialShareCardGenerator.shareProgressCardToSocial(
                            context = context,
                            stats = stats,
                            format = SocialCardFormat.LANDSCAPE_16_9,
                            platform = SocialPlatform.TWITTER_X
                        )
                    }
                )
                SocialQuickPill(
                    label = "Facebook",
                    accentColor = SocialPlatform.FACEBOOK.badgeColor,
                    testTag = "quick_share_facebook",
                    onClick = {
                        SocialShareCardGenerator.shareProgressCardToSocial(
                            context = context,
                            stats = stats,
                            format = SocialCardFormat.PORTRAIT_4_5,
                            platform = SocialPlatform.FACEBOOK
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenShareStudio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_social_card_studio_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBright,
                    contentColor = Color(0xFF060B1E)
                )
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Preview & Customize Canvas Share Card",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SocialQuickPill(
    label: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        modifier = Modifier
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.PILL,
                accentTint = accentColor,
                isNested = true
            )
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SoftMintText,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialSharePreviewModal(
    stats: SocialShareStats,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(SocialCardFormat.PORTRAIT_4_5) }

    val renderedBitmap = remember(stats, selectedFormat) {
        SocialShareCardGenerator.renderShareCardBitmap(stats, selectedFormat)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EmeraldCardGlass,
        titleContentColor = SoftMintText,
        textContentColor = MutedSageText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = MintBright)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Study Streak Card",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rendered in real time via Android Canvas API (${selectedFormat.widthPx}×${selectedFormat.heightPx} PNG):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSageText
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Format Selector Chips (9:16 Story, 4:5 Feed, 16:9 X/Twitter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SocialCardFormat.entries.forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFormat = fmt },
                            label = {
                                Text(
                                    text = fmt.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("format_chip_${fmt.name.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintBright,
                                selectedLabelColor = Color(0xFF060B1E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Rendered Canvas Bitmap Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 280.dp)
                        .aspectRatio(selectedFormat.previewAspect)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DeepForestBg)
                        .border(1.dp, SubtleGreenBorder, RoundedCornerShape(18.dp))
                        .testTag("canvas_card_bitmap_preview"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = renderedBitmap.asImageBitmap(),
                        contentDescription = "Generated Study Streak Card Preview",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "One-Tap Share to Social Platforms",
                    style = MaterialTheme.typography.labelLarge,
                    color = SoftMintText
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SocialPlatform.entries.forEach { platform ->
                        OutlinedButton(
                            onClick = {
                                SocialShareCardGenerator.shareProgressCardToSocial(
                                    context = context,
                                    stats = stats,
                                    format = selectedFormat,
                                    platform = platform
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, platform.badgeColor),
                            modifier = Modifier.testTag("modal_share_${platform.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = when (platform) {
                                    SocialPlatform.INSTAGRAM -> Icons.Default.PhotoCamera
                                    SocialPlatform.WHATSAPP -> Icons.Default.Sms
                                    SocialPlatform.TWITTER_X -> Icons.Default.Public
                                    SocialPlatform.FACEBOOK -> Icons.Default.Share
                                    SocialPlatform.SYSTEM_MORE -> Icons.Default.IosShare
                                },
                                contentDescription = platform.displayName,
                                tint = platform.badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = platform.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftMintText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("RegainStreakCaption", stats.buildShareCaption())
                        )
                        Toast.makeText(context, "Social caption copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Caption",
                        tint = MintBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Streak Caption & Hashtags", color = SoftMintText)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SocialShareCardGenerator.shareProgressCardToSocial(
                        context = context,
                        stats = stats,
                        format = selectedFormat,
                        platform = SocialPlatform.SYSTEM_MORE
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBright,
                    contentColor = Color(0xFF060B1E)
                ),
                modifier = Modifier.testTag("confirm_share_card_button")
            ) {
                Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Image Card", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MutedSageText)
            }
        }
    )
}
