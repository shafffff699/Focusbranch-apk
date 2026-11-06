package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ai.StudyClassifierRepository
import com.example.data.local.FastStateManager
import com.example.data.local.RegainDatabase
import com.example.data.remote.FirestoreRepository
import com.example.ui.analytics.AnalyticsAndShieldScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.filter.StudyFilterScreen
import com.example.ui.rooms.StudyRoomsScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyanInfo
import com.example.ui.theme.EmeraldCardGlass
import com.example.ui.theme.LiquidGlassAnimatedBackdrop
import com.example.ui.theme.LiquidGlassCircle
import com.example.ui.theme.LiquidGlassControlsDialog
import com.example.ui.theme.LiquidGlassPill
import com.example.ui.theme.LiquidGlassShapeType
import com.example.ui.theme.LocalLiquidGlassControls
import com.example.ui.theme.MintBright
import com.example.ui.theme.MutedSageText
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.liquidGlassEffect
import com.example.ui.timer.FocusTimerScreen
import com.example.util.configureAppCheck
import com.example.viewmodel.RegainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LiquidGlassAnimatedBackdrop {
                        RegainAppNavigation()
                    }
                }
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun RegainAppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        MainSanctuaryScreen(
            currentUserId = user.uid,
            currentUserName = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")
                ?: "Focused Student"
        )
    }
}

private data class NavTabItem(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainSanctuaryScreen(
    currentUserId: String,
    currentUserName: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val glassControls = LocalLiquidGlassControls.current
    var showGlassControlsModal by remember { mutableStateOf(false) }

    val viewModel: RegainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(databaseId)
                val roomDb = RegainDatabase.getInstance(app)
                val fastStateManager = FastStateManager(app)
                val classifierRepo = StudyClassifierRepository(
                    videoCacheDao = roomDb.videoCacheDao(),
                    whitelistDao = roomDb.whitelistDao()
                )
                val firestoreRepo = FirestoreRepository(firestore)
                RegainViewModel(
                    fastStateManager = fastStateManager,
                    videoCacheDao = roomDb.videoCacheDao(),
                    whitelistDao = roomDb.whitelistDao(),
                    blockRuleDao = roomDb.blockRuleDao(),
                    classifierRepository = classifierRepo,
                    firestoreRepository = firestoreRepo,
                    currentUserId = currentUserId,
                    currentUserName = currentUserName
                )
            }
        }
    )

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val fastState by viewModel.fastState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val distractionAlert by viewModel.distractionAlertMessage.collectAsStateWithLifecycle()

    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    val navItems = remember {
        listOf(
            NavTabItem(0, "Focus", Icons.Default.Timer, "nav_tab_focus"),
            NavTabItem(1, "Study Tube", Icons.Default.PlayCircleFilled, "nav_tab_filter"),
            NavTabItem(2, "Rooms", Icons.Default.Groups, "nav_tab_rooms"),
            NavTabItem(3, "Analytics", Icons.Default.Analytics, "nav_tab_analytics")
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .liquidGlassEffect(
                        shapeType = LiquidGlassShapeType.ROUNDED,
                        cornerRadius = 26.dp,
                        accentTint = MintBright
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        LiquidGlassCircle(
                            size = 40.dp,
                            accentTint = MintBright,
                            isNested = true
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Regain Focus",
                                tint = MintBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Regain Focus",
                                style = MaterialTheme.typography.titleLarge,
                                color = SoftMintText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$currentUserName • ${fastState.activeSubject}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSageText
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Live dashersw/liquid-glass-js Shader Tuner Trigger Pill
                        LiquidGlassPill(
                            accentTint = CyanInfo,
                            isNested = true,
                            onClick = { showGlassControlsModal = true },
                            modifier = Modifier.testTag("liquid_glass_controls_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Liquid Glass Shader Controls",
                                    tint = CyanInfo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Glass FX",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftMintText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Active Focus Status Liquid Glass Pill
                        LiquidGlassPill(
                            accentTint = if (fastState.isStudyModeActive) MintBright else AmberWarning,
                            isNested = true
                        ) {
                            Text(
                                text = if (fastState.isStudyModeActive) {
                                    if (fastState.isStrictMode) "🔒 STRICT" else "🟢 ACTIVE"
                                } else {
                                    "STANDBY"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (fastState.isStudyModeActive) MintBright else MutedSageText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = statusBanner != null) {
                    statusBanner?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .liquidGlassEffect(
                                    shapeType = LiquidGlassShapeType.ROUNDED,
                                    cornerRadius = 14.dp,
                                    accentTint = MintBright,
                                    isNested = true
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftMintText,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearStatusBanner() }) {
                                Text("OK", color = MintBright)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Floating Liquid Glass Capsule Navigation Dock (modeled after dashersw/liquid-glass-js pill bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassEffect(
                            shapeType = LiquidGlassShapeType.PILL,
                            accentTint = MintBright,
                            isNested = false
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { item ->
                        val isSelected = selectedTab == item.index
                        val itemModifier = if (isSelected) {
                            Modifier
                                .weight(1f)
                                .liquidGlassEffect(
                                    shapeType = LiquidGlassShapeType.PILL,
                                    accentTint = MintBright,
                                    isNested = true
                                )
                                .clickable { selectedTab = item.index }
                                .padding(vertical = 8.dp)
                                .testTag(item.testTag)
                        } else {
                            Modifier
                                .weight(1f)
                                .clickable { selectedTab = item.index }
                                .padding(vertical = 8.dp)
                                .testTag(item.testTag)
                        }

                        Column(
                            modifier = itemModifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MintBright else SoftMintText.copy(alpha = 0.72f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) SoftMintText else MutedSageText.copy(alpha = 0.78f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedTab) {
                0 -> FocusTimerScreen(viewModel = viewModel)
                1 -> StudyFilterScreen(viewModel = viewModel)
                2 -> StudyRoomsScreen(viewModel = viewModel)
                3 -> AnalyticsAndShieldScreen(
                    viewModel = viewModel,
                    onSignOut = {
                        signOutUser(
                            context = context,
                            credentialManager = credentialManager,
                            onSignOutComplete = {},
                            scope = coroutineScope
                        )
                    }
                )
            }
        }
    }

    if (showGlassControlsModal) {
        LiquidGlassControlsDialog(
            controls = glassControls,
            onDismiss = { showGlassControlsModal = false }
        )
    }

    // Real-Time Tab/App Switch Distraction Alert Modal
    if (distractionAlert != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDistractionAlertAndResumeFocus() },
            containerColor = Color(0xFF0B1126),
            titleContentColor = SoftMintText,
            textContentColor = MutedSageText,
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Distraction Alert",
                    tint = CoralAlert,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Focus Interrupted — Tab/App Switch Detected!",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = distractionAlert!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftMintText
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissDistractionAlertAndResumeFocus() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintBright,
                        contentColor = Color(0xFF060B1E)
                    ),
                    modifier = Modifier.testTag("resume_focus_after_distraction_button")
                ) {
                    Text("Resume Focus Now", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
