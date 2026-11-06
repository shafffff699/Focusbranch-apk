package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Geometric shape types supported by the dashersw/liquid-glass-js optical system.
 */
enum class LiquidGlassShapeType(val label: String) {
    ROUNDED("Rounded Rect"),
    PILL("Capsule Pill"),
    CIRCLE("Optical Circle")
}

/**
 * Live optical refraction and lighting state modeled on `window.glassControls`
 * from `https://github.com/dashersw/liquid-glass-js`.
 */
@Stable
class LiquidGlassControlsState(
    initialEdgeIntensity: Float = 0.045f,
    initialRimIntensity: Float = 0.135f,
    initialBlurRadius: Float = 9.0f,
    initialTintOpacity: Float = 0.20f,
    initialChromaticDispersion: Float = 0.68f
) {
    var edgeIntensity by mutableFloatStateOf(initialEdgeIntensity)
    var rimIntensity by mutableFloatStateOf(initialRimIntensity)
    var blurRadius by mutableFloatStateOf(initialBlurRadius)
    var tintOpacity by mutableFloatStateOf(initialTintOpacity)
    var chromaticDispersion by mutableFloatStateOf(initialChromaticDispersion)
    var activePresetName by mutableStateOf("dashersw Default")

    fun applyCrystalDefault() {
        edgeIntensity = 0.045f
        rimIntensity = 0.135f
        blurRadius = 9.0f
        tintOpacity = 0.20f
        chromaticDispersion = 0.68f
        activePresetName = "dashersw Default"
    }

    fun applyHyperPrismatic() {
        edgeIntensity = 0.092f
        rimIntensity = 0.195f
        blurRadius = 5.0f
        tintOpacity = 0.14f
        chromaticDispersion = 0.96f
        activePresetName = "Hyper Prismatic"
    }

    fun applyDeepFrosted() {
        edgeIntensity = 0.026f
        rimIntensity = 0.095f
        blurRadius = 14.5f
        tintOpacity = 0.34f
        chromaticDispersion = 0.35f
        activePresetName = "Frosted Vision"
    }
}

val LocalLiquidGlassControls = compositionLocalOf { LiquidGlassControlsState() }

/**
 * High-contrast Cosmic Aurora & Prismatic Light Backdrop designed specifically to maximize
 * the visual refraction, edge bending, and chromatic dispersion of crystal-clear Liquid Glass.
 */
@Composable
fun LiquidGlassAnimatedBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquidGlassScene")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbPhase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = (Math.PI / 2f).toFloat(),
        targetValue = (2.5f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 21000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbPhase2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050816),
                        Color(0xFF0A1128),
                        Color(0xFF111332),
                        Color(0xFF060814)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Fine silver-blue optical refraction grid (makes glass edge lensing stand out sharply)
            val gridSpacing = 52.dp.toPx()
            val gridColor = Color(0x1693C5FD)
            var x = 0f
            while (x < w) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            var y = 0f
            while (y < h) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // Orb 1: Electric Sapphire-Cyan Optical Sphere
            val orb1Center = Offset(
                x = w * (0.24f + 0.24f * cos(phase1)),
                y = h * (0.20f + 0.14f * sin(phase1))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x7038BDF8),
                        Color(0x383B82F6),
                        Color.Transparent
                    ),
                    center = orb1Center,
                    radius = w * 0.62f
                ),
                center = orb1Center,
                radius = w * 0.62f
            )

            // Orb 2: Iridescent Violet-Magenta Prismatic Sphere
            val orb2Center = Offset(
                x = w * (0.78f + 0.18f * sin(phase2)),
                y = h * (0.44f + 0.18f * cos(phase2))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x68A855F7),
                        Color(0x34EC4899),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = w * 0.58f
                ),
                center = orb2Center,
                radius = w * 0.58f
            )

            // Orb 3: Deep Indigo-Royal Caustic Sphere
            val orb3Center = Offset(
                x = w * (0.32f + 0.26f * sin(phase1 * 0.7f)),
                y = h * (0.76f + 0.13f * cos(phase2 * 0.8f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x606366F1),
                        Color(0x2E38BDF8),
                        Color.Transparent
                    ),
                    center = orb3Center,
                    radius = w * 0.64f
                ),
                center = orb3Center,
                radius = w * 0.64f
            )

            // Orb 4: Warm Sunset Coral-Gold Caustic Accent
            val orb4Center = Offset(
                x = w * (0.68f + 0.20f * cos(phase2)),
                y = h * (0.15f + 0.10f * sin(phase1))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x48F43F5E),
                        Color(0x24FBBF24),
                        Color.Transparent
                    ),
                    center = orb4Center,
                    radius = w * 0.44f
                ),
                center = orb4Center,
                radius = w * 0.44f
            )
        }

        content()
    }
}

/**
 * Core Modifier that renders the `dashersw/liquid-glass-js` multi-pass optical refraction,
 * crystal-clear silver-white frosted glass body, chromatic edge dispersion (cyan/magenta/violet),
 * and high-gloss specular rim lighting.
 */
@Composable
fun Modifier.liquidGlassEffect(
    shapeType: LiquidGlassShapeType = LiquidGlassShapeType.ROUNDED,
    cornerRadius: Dp = 24.dp,
    accentTint: Color = MintBright,
    isNested: Boolean = false,
    interactiveHighlightOffset: Offset? = null
): Modifier {
    val controls = LocalLiquidGlassControls.current
    val effectiveCornerRadius = when (shapeType) {
        LiquidGlassShapeType.ROUNDED -> cornerRadius
        LiquidGlassShapeType.PILL -> 50.dp
        LiquidGlassShapeType.CIRCLE -> 500.dp
    }
    val clipShape: Shape = when (shapeType) {
        LiquidGlassShapeType.ROUNDED -> RoundedCornerShape(cornerRadius)
        LiquidGlassShapeType.PILL -> RoundedCornerShape(percent = 50)
        LiquidGlassShapeType.CIRCLE -> CircleShape
    }

    val edgeStrength = (controls.edgeIntensity / 0.10f).coerceIn(0f, 1.25f)
    val rimStrength = (controls.rimIntensity / 0.20f).coerceIn(0f, 1.25f)
    val frostFactor = (controls.blurRadius / 15f).coerceIn(0.05f, 1f)
    val baseTintAlpha = if (isNested) {
        (controls.tintOpacity * 0.82f).coerceIn(0.08f, 0.72f)
    } else {
        controls.tintOpacity.coerceIn(0.08f, 0.72f)
    }
    val dispersion = controls.chromaticDispersion.coerceIn(0f, 1f)

    return this
        .clip(clipShape)
        .drawWithContent {
            val crPx = when (shapeType) {
                LiquidGlassShapeType.ROUNDED -> effectiveCornerRadius.toPx()
                LiquidGlassShapeType.PILL -> size.minDimension / 2f
                LiquidGlassShapeType.CIRCLE -> size.minDimension / 2f
            }
            val corner = CornerRadius(crPx, crPx)

            // 1. Crystal-Clear Frosted White & Ice-Sapphire Glass Body (achromatic optical lens)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (baseTintAlpha * 0.72f + frostFactor * 0.12f).coerceAtMost(0.48f)),
                        Color(0xFF1E294B).copy(alpha = (baseTintAlpha * 0.85f).coerceAtMost(0.52f)),
                        Color(0xFF38BDF8).copy(alpha = (baseTintAlpha * 0.32f + edgeStrength * 0.08f).coerceAtMost(0.35f)),
                        Color.White.copy(alpha = (baseTintAlpha * 0.48f).coerceAtMost(0.36f))
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = corner
            )

            // 2. Diagonal Liquid Specular Sheen (curved physical glass surface reflection)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.22f * rimStrength + frostFactor * 0.06f).coerceAtMost(0.42f)),
                        accentTint.copy(alpha = (0.10f * edgeStrength).coerceAtMost(0.24f)),
                        Color.Transparent,
                        Color(0xFFC084FC).copy(alpha = (0.08f * edgeStrength).coerceAtMost(0.20f)),
                        Color.White.copy(alpha = (0.12f * rimStrength).coerceAtMost(0.28f))
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = corner
            )

            // 3. Interactive Pointer / Caustic Light Lens
            val highlightCenter = interactiveHighlightOffset
                ?: Offset(size.width * 0.22f, size.height * 0.16f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.20f * rimStrength).coerceAtMost(0.38f)),
                        accentTint.copy(alpha = (0.11f * edgeStrength).coerceAtMost(0.25f)),
                        Color.Transparent
                    ),
                    center = highlightCenter,
                    radius = (size.maxDimension * 0.68f).coerceAtLeast(40f)
                ),
                center = highlightCenter,
                radius = (size.maxDimension * 0.68f).coerceAtLeast(40f)
            )

            // 4. Render child UI content inside the glass container
            drawContent()

            // 5. Optical Edge Refraction Bevel (`edgeIntensity` inner curved lens thickness band)
            val innerBevelStroke = (1.8f + edgeStrength * 6.0f).dp.toPx()
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.34f * edgeStrength).coerceAtMost(0.55f)),
                        Color(0xFF38BDF8).copy(alpha = (0.24f * edgeStrength).coerceAtMost(0.42f)),
                        Color.Transparent,
                        Color(0xFFE879F9).copy(alpha = (0.22f * edgeStrength).coerceAtMost(0.38f)),
                        Color.White.copy(alpha = (0.24f * edgeStrength).coerceAtMost(0.42f))
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                topLeft = Offset(innerBevelStroke / 2f, innerBevelStroke / 2f),
                size = Size(
                    (size.width - innerBevelStroke).coerceAtLeast(1f),
                    (size.height - innerBevelStroke).coerceAtLeast(1f)
                ),
                cornerRadius = CornerRadius(
                    (crPx - innerBevelStroke / 2f).coerceAtLeast(0f),
                    (crPx - innerBevelStroke / 2f).coerceAtLeast(0f)
                ),
                style = Stroke(width = innerBevelStroke)
            )

            // 6. Prismatic Chromatic Dispersion Fringe (Cyan / Rose-Magenta / Violet optical aberration)
            if (dispersion > 0.05f) {
                val fringeStroke = 1.35.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.55f * dispersion),
                            Color.Transparent,
                            Color(0xFFF472B6).copy(alpha = 0.48f * dispersion),
                            Color(0xFFC084FC).copy(alpha = 0.50f * dispersion)
                        ),
                        start = Offset(0f, size.height * 0.12f),
                        end = Offset(size.width, size.height * 0.88f)
                    ),
                    topLeft = Offset(fringeStroke * 1.7f, fringeStroke * 1.7f),
                    size = Size(
                        (size.width - fringeStroke * 3.4f).coerceAtLeast(1f),
                        (size.height - fringeStroke * 3.4f).coerceAtLeast(1f)
                    ),
                    cornerRadius = CornerRadius(
                        (crPx - fringeStroke * 1.7f).coerceAtLeast(0f),
                        (crPx - fringeStroke * 1.7f).coerceAtLeast(0f)
                    ),
                    style = Stroke(width = fringeStroke)
                )
            }

            // 7. Specular Rim Lighting (`rimIntensity` outer crisp white-silver curved glass border)
            val rimStroke = (1.3f + rimStrength * 1.6f).dp.toPx()
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.85f * rimStrength).coerceIn(0.22f, 0.95f)),
                        accentTint.copy(alpha = (0.58f * rimStrength).coerceIn(0.16f, 0.80f)),
                        Color.White.copy(alpha = (0.16f * rimStrength).coerceIn(0.06f, 0.30f)),
                        Color(0xFFC084FC).copy(alpha = (0.52f * rimStrength).coerceIn(0.14f, 0.75f)),
                        Color.White.copy(alpha = (0.72f * rimStrength).coerceIn(0.18f, 0.90f))
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = corner,
                style = Stroke(width = rimStroke)
            )
        }
}

/**
 * Primary Liquid Glass Card (`Container` in `dashersw/liquid-glass-js`) supporting
 * nested glass refraction and interactive touch caustic highlights.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    accentTint: Color = MintBright,
    isNested: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    var touchOffset by remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        touchOffset = offset
                        tryAwaitRelease()
                    }
                )
            }
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.ROUNDED,
                cornerRadius = cornerRadius,
                accentTint = accentTint,
                isNested = isNested,
                interactiveHighlightOffset = touchOffset
            )
            .padding(contentPadding),
        content = content
    )
}

/**
 * Liquid Glass Capsule / Pill (`shapeType = PILL` in `dashersw/liquid-glass-js`).
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    accentTint: Color = MintBright,
    isNested: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableMod = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(clickableMod)
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.PILL,
                accentTint = accentTint,
                isNested = isNested
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Liquid Glass Circular Optical Lens (`shapeType = CIRCLE` in `dashersw/liquid-glass-js`).
 */
@Composable
fun LiquidGlassCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    accentTint: Color = MintBright,
    isNested: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableMod = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(size)
            .then(clickableMod)
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.CIRCLE,
                accentTint = accentTint,
                isNested = isNested
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Interactive `window.glassControls` Live Shader Tuner Modal from `dashersw/liquid-glass-js`.
 * Lets the user adjust `edgeIntensity`, `rimIntensity`, `blurRadius`, `tintOpacity`, and
 * `chromaticDispersion` in real time across the entire app.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidGlassControlsDialog(
    controls: LiquidGlassControlsState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0B1126),
        titleContentColor = SoftMintText,
        textContentColor = MutedSageText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Liquid Glass Shader Controls",
                    tint = MintBright
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Liquid Glass UI Controls",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftMintText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "dashersw/liquid-glass-js • Crystal Optical Refraction",
                        style = MaterialTheme.typography.labelSmall,
                        color = MintBright
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live Interactive Glass Preview Sample inside the tuner
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    accentTint = MintBright,
                    isNested = true,
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassCircle(size = 42.dp, accentTint = CyanInfo) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SoftMintText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Live Optical Lens Preview",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftMintText,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Preset: ${controls.activePresetName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSageText
                                )
                            }
                        }
                        LiquidGlassPill(
                            accentTint = AmberWarning,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "PILL",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftMintText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optical Presets Row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = controls.activePresetName == "dashersw Default",
                        onClick = { controls.applyCrystalDefault() },
                        label = { Text("dashersw Default", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintPrimary,
                            selectedLabelColor = SoftMintText
                        )
                    )
                    FilterChip(
                        selected = controls.activePresetName == "Hyper Prismatic",
                        onClick = { controls.applyHyperPrismatic() },
                        label = { Text("Hyper Prismatic", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintPrimary,
                            selectedLabelColor = SoftMintText
                        )
                    )
                    FilterChip(
                        selected = controls.activePresetName == "Frosted Vision",
                        onClick = { controls.applyDeepFrosted() },
                        label = { Text("Frosted Vision", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintPrimary,
                            selectedLabelColor = SoftMintText
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassParameterSlider(
                    label = "edgeIntensity (Edge Refraction)",
                    valueText = "%.3f".format(controls.edgeIntensity),
                    value = controls.edgeIntensity,
                    valueRange = 0.005f..0.10f,
                    onValueChange = {
                        controls.edgeIntensity = it
                        controls.activePresetName = "Custom"
                    }
                )

                GlassParameterSlider(
                    label = "rimIntensity (Specular Rim Light)",
                    valueText = "%.3f".format(controls.rimIntensity),
                    value = controls.rimIntensity,
                    valueRange = 0.01f..0.20f,
                    onValueChange = {
                        controls.rimIntensity = it
                        controls.activePresetName = "Custom"
                    }
                )

                GlassParameterSlider(
                    label = "blurRadius (Frosted Depth)",
                    valueText = "%.1f px".format(controls.blurRadius),
                    value = controls.blurRadius,
                    valueRange = 1.0f..15.0f,
                    onValueChange = {
                        controls.blurRadius = it
                        controls.activePresetName = "Custom"
                    }
                )

                GlassParameterSlider(
                    label = "tintOpacity (Glass Body Alpha)",
                    valueText = "%.2f".format(controls.tintOpacity),
                    value = controls.tintOpacity,
                    valueRange = 0.08f..0.60f,
                    onValueChange = {
                        controls.tintOpacity = it
                        controls.activePresetName = "Custom"
                    }
                )

                GlassParameterSlider(
                    label = "chromaticDispersion (Prismatic Fringe)",
                    valueText = "%.2f".format(controls.chromaticDispersion),
                    value = controls.chromaticDispersion,
                    valueRange = 0.0f..1.0f,
                    onValueChange = {
                        controls.chromaticDispersion = it
                        controls.activePresetName = "Custom"
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBright,
                    contentColor = OnGlassPrimaryDark
                ),
                modifier = Modifier.testTag("close_glass_controls_button")
            ) {
                Text("Apply Glass Shader", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { controls.applyCrystalDefault() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MintBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", color = MintBright)
            }
        }
    )
}

@Composable
private fun GlassParameterSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SoftMintText
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall,
                color = MintBright,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MintBright,
                activeTrackColor = MintPrimary,
                inactiveTrackColor = SubtleGreenBorder
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}
