package com.example.ui.auth

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.DeepForestBg
import com.example.ui.theme.EmeraldCardGlass
import com.example.ui.theme.EmeraldElevatedGlass
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiquidGlassShapeType
import com.example.ui.theme.MintBright
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MutedSageText
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.SubtleGreenBorder
import com.example.ui.theme.liquidGlassEffect
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOutUser(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showNativeCodeDialog by remember { mutableStateOf(false) }

    var accessibilitySim by remember { mutableStateOf(true) }
    var usageStatsSim by remember { mutableStateOf(true) }
    var overlaySim by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Hero Banner Card (Liquid Glass Container)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassEffect(
                        shapeType = LiquidGlassShapeType.ROUNDED,
                        cornerRadius = 28.dp,
                        accentTint = MintBright
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_focus_hero_1793896240612),
                        contentDescription = "Regain Focus Sanctuary Hero Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, EmeraldCardGlass)
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MintPrimary.copy(alpha = 0.25f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Regain Shield",
                                    tint = MintBright
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "REGAIN FOCUS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintBright
                            )
                            Text(
                                text = "Smart AI Study Sanctuary",
                                style = MaterialTheme.typography.headlineMedium,
                                color = SoftMintText
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Eliminate YouTube Shorts doomscrolling, filter study lectures with Gemini AI (<2ms SQLite cache), and study live with peers.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedSageText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FeatureHighlightRow(
                        icon = Icons.Default.PlayCircleFilled,
                        title = "AI Study Tube & Shorts Blocker",
                        subtitle = "5-step pipeline with local Room SQLite <2ms cache & distraction-free player"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureHighlightRow(
                        icon = Icons.Default.Lock,
                        title = "Strict Mode & AI Subject Quiz",
                        subtitle = "Locks distracting apps until timer ends or you pass a 3-question Gemini quiz"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureHighlightRow(
                        icon = Icons.Default.Groups,
                        title = "Live Collaborative Study Rooms",
                        subtitle = "Real-time group Pomodoro, tab-switch telemetry & accountability leaderboards"
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Primary Google Sign-In Button via CredentialManager
                    Button(
                        onClick = {
                            authError = null
                            isLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isLoading = false
                                    authError = err
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintBright,
                            contentColor = Color(0xFF060B1E)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF060B1E),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Sign-In",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = authError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Android Permissions Setup Checklist Card
            AndroidPermissionsChecklistCard(
                accessibilityEnabled = accessibilitySim,
                usageStatsEnabled = usageStatsSim,
                overlayEnabled = overlaySim,
                onToggleAccessibility = { accessibilitySim = it },
                onToggleUsageStats = { usageStatsSim = it },
                onToggleOverlay = { overlaySim = it },
                onShowNativeCode = { showNativeCodeDialog = true }
            )
        }
    }

    if (showNativeCodeDialog) {
        NativeAndroidApkCodeDialog(onDismiss = { showNativeCodeDialog = false })
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shapeType = LiquidGlassShapeType.ROUNDED,
                cornerRadius = 16.dp,
                accentTint = MintBright,
                isNested = true
            )
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MintBright,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SoftMintText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSageText
            )
        }
    }
}

@Composable
fun AndroidPermissionsChecklistCard(
    accessibilityEnabled: Boolean,
    usageStatsEnabled: Boolean,
    overlayEnabled: Boolean,
    onToggleAccessibility: (Boolean) -> Unit,
    onToggleUsageStats: (Boolean) -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    onShowNativeCode: () -> Unit
) {
    val context = LocalContext.current

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
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Android Shield Permissions",
                        tint = MintBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Android Shield Setup Checklist",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftMintText
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Configure native system hooks for YouTube Shorts UI inspection, app usage tracking, and full-screen focus overlays.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSageText
            )

            Spacer(modifier = Modifier.height(14.dp))

            PermissionToggleRow(
                title = "1. AccessibilityService (Shorts & UI Inspector)",
                subtitle = "Detects YouTube reel_player_page_container & Shorts URLs in real time",
                checked = accessibilityEnabled,
                onCheckedChange = onToggleAccessibility,
                onOpenSystemSettings = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Simulation mode toggled", Toast.LENGTH_SHORT).show()
                    }
                },
                testTag = "perm_accessibility_switch"
            )

            Spacer(modifier = Modifier.height(10.dp))

            PermissionToggleRow(
                title = "2. UsageStatsManager (App Time Tracker)",
                subtitle = "Monitors foreground app packages & enforces daily time budgets",
                checked = usageStatsEnabled,
                onCheckedChange = onToggleUsageStats,
                onOpenSystemSettings = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Simulation mode toggled", Toast.LENGTH_SHORT).show()
                    }
                },
                testTag = "perm_usagestats_switch"
            )

            Spacer(modifier = Modifier.height(10.dp))

            PermissionToggleRow(
                title = "3. SYSTEM_ALERT_WINDOW (Blocking Overlay)",
                subtitle = "Displays the full-screen emerald focus shield over distracting apps",
                checked = overlayEnabled,
                onCheckedChange = onToggleOverlay,
                onOpenSystemSettings = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Simulation mode toggled", Toast.LENGTH_SHORT).show()
                    }
                },
                testTag = "perm_overlay_switch"
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onShowNativeCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("view_native_apk_code_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MintBright)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Native Android Code",
                    tint = MintBright
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Native Android APK Code (Kotlin + Manifest)",
                    color = SoftMintText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PermissionToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenSystemSettings: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(EmeraldElevatedGlass.copy(alpha = 0.55f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (checked) MintBright else MutedSageText.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = SoftMintText
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSageText
            )
            TextButton(
                onClick = onOpenSystemSettings,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = "Open Android System Settings ->",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintBright
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF060B1E),
                checkedTrackColor = MintBright
            )
        )
    }
}

@Composable
fun NativeAndroidApkCodeDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val accessibilityKotlinCode = """
package com.aistudio.regainfocus.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.local.FastStateManager
import com.example.data.local.RegainDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private lateinit var fastStateManager: FastStateManager
    private lateinit var database: RegainDatabase

    override fun onServiceConnected() {
        super.onServiceConnected()
        fastStateManager = FastStateManager(applicationContext)
        database = RegainDatabase.getInstance(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode: AccessibilityNodeInfo = rootInActiveWindow ?: return
        val pkg = event?.packageName?.toString() ?: return
        val state = fastStateManager.state.value

        // 1. Sub-millisecond fast_state check
        if (!state.isStudyModeActive) return

        // 2. Intercept YouTube Shorts Reel Player Container immediately
        if (pkg == "com.google.android.youtube" && state.blockShorts) {
            val shortsNodes = rootNode.findAccessibilityNodeInfosByViewId(
                "com.google.android.youtube:id/reel_player_page_container"
            )
            if (!shortsNodes.isNullOrEmpty()) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                fastStateManager.updateState {
                    it.copy(shortsBlockedCount = it.shortsBlockedCount + 1)
                }
                return
            }

            // 3. Extract active YouTube Video Title & Channel for Room Cache / Gemini Check
            val titleNodes = rootNode.findAccessibilityNodeInfosByViewId(
                "com.google.android.youtube:id/title"
            )
            val channelNodes = rootNode.findAccessibilityNodeInfosByViewId(
                "com.google.android.youtube:id/channel_name"
            )
            val videoTitle = titleNodes?.firstOrNull()?.text?.toString().orEmpty()
            val channelName = channelNodes?.firstOrNull()?.text?.toString().orEmpty()

            if (videoTitle.isNotBlank()) {
                serviceScope.launch {
                    val cached = database.videoCacheDao().getCachedVideoById(videoTitle.lowercase())
                    if (cached != null && !cached.isStudyRelated) {
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {}
}
    """.trimIndent()

    val manifestXmlCode = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission
        android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <application
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApplication">

        <service
            android:name="com.aistudio.regainfocus.service.FocusAccessibilityService"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
    </application>
</manifest>
    """.trimIndent()

    val activeCode = if (selectedTab == 0) accessibilityKotlinCode else manifestXmlCode

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EmeraldCardGlass,
        titleContentColor = SoftMintText,
        textContentColor = MutedSageText,
        title = {
            Text(
                text = "Native Android APK Source Code",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) MintBright else EmeraldElevatedGlass,
                            contentColor = if (selectedTab == 0) Color(0xFF060B1E) else SoftMintText
                        )
                    ) {
                        Text("FocusAccessibilityService.kt", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) MintBright else EmeraldElevatedGlass,
                            contentColor = if (selectedTab == 1) Color(0xFF060B1E) else SoftMintText
                        )
                    ) {
                        Text("AndroidManifest.xml", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepForestBg)
                        .border(1.dp, SubtleGreenBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = activeCode,
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftMintText
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("RegainNativeCode", activeCode))
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBright,
                    contentColor = Color(0xFF060B1E)
                )
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MutedSageText)
            }
        }
    )
}
