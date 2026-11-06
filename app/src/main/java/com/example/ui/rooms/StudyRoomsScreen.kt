package com.example.ui.rooms

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.RoomMemberModel
import com.example.data.remote.StudyRoomModel
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
import com.example.viewmodel.UiState

private val ROOM_SUBJECTS = listOf("Engineering", "Coding", "Exam Prep", "Mathematics", "Medicine")
private val ACCOUNTABILITY_REACTIONS = listOf("🔥", "🎯", "💪", "📚", "⚡")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyRoomsScreen(
    viewModel: RegainViewModel
) {
    val roomsState by viewModel.studyRoomsState.collectAsStateWithLifecycle()
    val activeRoomId by viewModel.activeRoomId.collectAsStateWithLifecycle()
    val activeMembers by viewModel.activeRoomMembers.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()

    var roomNameInput by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Engineering") }
    var joinCodeInput by remember { mutableStateOf("") }

    val roomsList = (roomsState as? UiState.Success)?.data.orEmpty()
    val currentRoom = roomsList.firstOrNull { it.id == activeRoomId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        if (activeRoomId != null) {
            ActiveStudyRoomView(
                room = currentRoom,
                members = activeMembers,
                remainingSeconds = remainingSeconds,
                onSendReaction = { viewModel.sendAccountabilityReaction(it) },
                onLeaveRoom = { viewModel.leaveActiveRoom() }
            )
        } else {
            // Create New Study Room Card (Liquid Glass Container)
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
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Create Study Room",
                            tint = MintBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Live Multiplayer Study Room",
                            style = MaterialTheme.typography.titleLarge,
                            color = SoftMintText
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Generates a 6-character invite code with real-time Firestore presence, group Pomodoro, and live distraction leaderboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSageText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = roomNameInput,
                        onValueChange = { roomNameInput = it },
                        label = { Text("Study Room Title") },
                        placeholder = { Text("e.g. Finals Deep Focus Sprint") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintBright,
                            unfocusedBorderColor = SubtleGreenBorder,
                            focusedTextColor = SoftMintText,
                            unfocusedTextColor = SoftMintText
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Select Subject Tag",
                        style = MaterialTheme.typography.labelLarge,
                        color = SoftMintText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ROOM_SUBJECTS.forEach { tag ->
                            FilterChip(
                                selected = selectedSubject == tag,
                                onClick = { selectedSubject = tag },
                                label = { Text(tag) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintPrimary,
                                    selectedLabelColor = SoftMintText
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.createCollaborativeRoom(
                                roomName = roomNameInput.ifBlank { "$selectedSubject Focus Hall" },
                                subjectTag = selectedSubject,
                                timerDurationMinutes = 50
                            )
                            roomNameInput = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("create_room_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintBright,
                            contentColor = Color(0xFF060B1E)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Study Room & Generate 6-Char Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Join by 6-Character Code Card (Liquid Glass Container)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it.uppercase().take(6) },
                        label = { Text("6-Char Room Code") },
                        placeholder = { Text("e.g. ENG101") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("join_room_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintBright,
                            unfocusedBorderColor = SubtleGreenBorder,
                            focusedTextColor = SoftMintText,
                            unfocusedTextColor = SoftMintText
                        )
                    )
                    Button(
                        onClick = { viewModel.joinStudyRoomByCode(joinCodeInput) },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("join_by_code_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldElevatedGlass,
                            contentColor = MintBright
                        )
                    ) {
                        Text("Join Code", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Public Collaborative Study Rooms",
                style = MaterialTheme.typography.titleLarge,
                color = SoftMintText
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (val st = roomsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MintBright)
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = st.message,
                        color = CoralAlert,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is UiState.Success -> {
                    if (st.data.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SubtleGreenBorder, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "No public rooms created yet — tap 'Create Study Room' above to launch the first live room!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MutedSageText
                                )
                            }
                        }
                    } else {
                        st.data.forEach { room ->
                            StudyRoomListItem(
                                room = room,
                                onJoin = { viewModel.joinStudyRoom(room) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyRoomListItem(
    room: StudyRoomModel,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SubtleGreenBorder, RoundedCornerShape(20.dp))
            .clickable { onJoin() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintPrimary.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = room.roomCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = MintBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = room.subjectTag,
                        style = MaterialTheme.typography.labelLarge,
                        color = MutedSageText
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = room.roomName,
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftMintText
                )
                Text(
                    text = "Host: ${room.hostName} • ${room.timerDurationMinutes}m Pomodoro • Latest: ${room.lastReaction}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSageText
                )
            }
            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintBright,
                    contentColor = Color(0xFF060B1E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Join", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveStudyRoomView(
    room: StudyRoomModel?,
    members: List<RoomMemberModel>,
    remainingSeconds: Long,
    onSendReaction: (String) -> Unit,
    onLeaveRoom: () -> Unit
) {
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MintBright, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVE STUDY ROOM • CODE: ${room?.roomCode ?: "FOCUS6"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MintBright
                    )
                    Text(
                        text = room?.roomName ?: "Collaborative Focus Sanctuary",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SoftMintText
                    )
                }
                OutlinedButton(
                    onClick = onLeaveRoom,
                    modifier = Modifier.testTag("leave_room_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Leave Room",
                        tint = CoralAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Leave", color = SoftMintText)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Synchronized Group Pomodoro Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepForestBg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Group Timer",
                        tint = MintBright
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Synchronized Group Pomodoro",
                            style = MaterialTheme.typography.labelLarge,
                            color = SoftMintText
                        )
                        Text(
                            text = "${members.size} Students Active • Latest Reaction: ${room?.lastReaction ?: "🔥"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSageText
                        )
                    }
                }
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MintBright
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Silent Accountability Reactions
            Text(
                text = "Send Silent Accountability Reaction",
                style = MaterialTheme.typography.labelLarge,
                color = SoftMintText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ACCOUNTABILITY_REACTIONS.forEach { emoji ->
                    OutlinedButton(
                        onClick = { onSendReaction(emoji) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Live Member Grid
    Text(
        text = "Live Student Presence Grid",
        style = MaterialTheme.typography.titleLarge,
        color = SoftMintText
    )
    Spacer(modifier = Modifier.height(8.dp))

    members.forEach { member ->
        val statusColor = when (member.status) {
            "Focusing" -> MintBright
            "Distracted" -> CoralAlert
            else -> AmberWarning
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, SubtleGreenBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCardGlass)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = MintPrimary.copy(alpha = 0.25f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = member.displayName,
                                tint = MintBright
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${member.displayName} ${member.lastReaction}",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftMintText
                        )
                        Text(
                            text = "${member.subjectTag} • ${member.focusedMinutes}m focused • ${member.distractionCount} distractions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSageText
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = member.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Live Room Leaderboard
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SubtleGreenBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldElevatedGlass)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Room Leaderboard",
                    tint = AmberWarning
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Room Leaderboard (Focused Mins & Lowest Distractions)",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftMintText
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            members.forEachIndexed { idx, m ->
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
                    Text(
                        text = "#${idx + 1}  ${m.displayName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (idx == 0) MintBright else SoftMintText
                    )
                    Text(
                        text = "${m.focusedMinutes}m • ${m.distractionCount} alerts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSageText
                    )
                }
            }
        }
    }
}
