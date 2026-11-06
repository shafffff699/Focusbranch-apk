package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val RegainColorScheme = darkColorScheme(
    primary = MintBright,
    onPrimary = OnGlassPrimaryDark,
    primaryContainer = MintPrimary,
    onPrimaryContainer = SoftMintText,
    secondary = SageAccent,
    onSecondary = OnGlassPrimaryDark,
    secondaryContainer = EmeraldElevatedGlass,
    onSecondaryContainer = SoftMintText,
    tertiary = CyanInfo,
    onTertiary = OnGlassPrimaryDark,
    background = DeepForestBg,
    onBackground = SoftMintText,
    surface = EmeraldSurface,
    onSurface = SoftMintText,
    surfaceVariant = EmeraldCardGlass,
    onSurfaceVariant = MutedSageText,
    outline = SubtleGreenBorder,
    error = CoralAlert,
    onError = Color.White
)

val RegainShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val glassControls = remember { LiquidGlassControlsState() }
    CompositionLocalProvider(LocalLiquidGlassControls provides glassControls) {
        MaterialTheme(
            colorScheme = RegainColorScheme,
            typography = Typography,
            shapes = RegainShapes,
            content = content
        )
    }
}
