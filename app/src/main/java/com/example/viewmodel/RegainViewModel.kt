package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.QuizQuestion
import com.example.data.ai.StudyClassifierRepository
import com.example.data.ai.VideoClassificationDecision
import com.example.data.local.BlockRuleDao
import com.example.data.local.BlockRuleEntity
import com.example.data.local.FastFocusState
import com.example.data.local.FastStateManager
import com.example.data.local.VideoCacheDao
import com.example.data.local.VideoCacheEntity
import com.example.data.local.WhitelistDao
import com.example.data.local.WhitelistEntity
import com.example.data.remote.FirestoreRepository
import com.example.data.remote.RoomMemberModel
import com.example.data.remote.StudyRoomModel
import com.example.data.remote.StudySessionModel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val TAG = "RegainViewModel"
private const val STOP_TIMEOUT_MILLIS = 5000L

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface VideoEvalState {
    data object Idle : VideoEvalState
    data class Evaluating(val title: String, val stepMessage: String) : VideoEvalState
    data class Result(val decision: VideoClassificationDecision) : VideoEvalState
    data class Error(val message: String) : VideoEvalState
}

sealed interface QuizUiState {
    data object Idle : QuizUiState
    data object Loading : QuizUiState
    data class Active(
        val questions: List<QuizQuestion>,
        val selectedOptions: Map<Int, Int> = emptyMap(),
        val submitted: Boolean = false,
        val score: Int = 0
    ) : QuizUiState
    data object Passed : QuizUiState
    data class Error(val message: String) : QuizUiState
}

class RegainViewModel(
    private val fastStateManager: FastStateManager,
    private val videoCacheDao: VideoCacheDao,
    private val whitelistDao: WhitelistDao,
    private val blockRuleDao: BlockRuleDao,
    private val classifierRepository: StudyClassifierRepository,
    private val firestoreRepository: FirestoreRepository,
    val currentUserId: String,
    val currentUserName: String
) : ViewModel() {

    val fastState: StateFlow<FastFocusState> = fastStateManager.state

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _distractionAlertMessage = MutableStateFlow<String?>(null)
    val distractionAlertMessage: StateFlow<String?> = _distractionAlertMessage.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _videoEvalState = MutableStateFlow<VideoEvalState>(VideoEvalState.Idle)
    val videoEvalState: StateFlow<VideoEvalState> = _videoEvalState.asStateFlow()

    private val _quizState = MutableStateFlow<QuizUiState>(QuizUiState.Idle)
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    private val _studyNotes = MutableStateFlow("")
    val studyNotes: StateFlow<String> = _studyNotes.asStateFlow()

    private val _streakCelebrationTrigger = MutableStateFlow(0)
    val streakCelebrationTrigger: StateFlow<Int> = _streakCelebrationTrigger.asStateFlow()

    // Local Room SQLite Reactive Flows
    val cachedVideos: StateFlow<List<VideoCacheEntity>> = videoCacheDao.observeAllCachedVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val whitelistItems: StateFlow<List<WhitelistEntity>> = whitelistDao.observeWhitelist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val blockRules: StateFlow<List<BlockRuleEntity>> = blockRuleDao.observeBlockRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    // Cloud Firestore Reactive Flows (Two-tier error handling)
    val studySessionsState: StateFlow<UiState<List<StudySessionModel>>> =
        firestoreRepository.observeUserStudySessions(currentUserId)
            .map<List<StudySessionModel>, UiState<List<StudySessionModel>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing study sessions", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load study sessions"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val studyRoomsState: StateFlow<UiState<List<StudyRoomModel>>> =
        firestoreRepository.observePublicStudyRooms()
            .map<List<StudyRoomModel>, UiState<List<StudyRoomModel>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing public study rooms", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load study rooms"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    // Simulated university study companions that coexist with live Firestore room members
    private val simulatedPeers = MutableStateFlow(
        listOf(
            RoomMemberModel(
                id = "peer_aarav",
                roomId = "global",
                userId = "peer_1",
                displayName = "Aarav Sharma (IIT-M)",
                subjectTag = "Engineering",
                status = "Focusing",
                focusedMinutes = 115,
                distractionCount = 0,
                lastReaction = "🔥"
            ),
            RoomMemberModel(
                id = "peer_maya",
                roomId = "global",
                userId = "peer_2",
                displayName = "Maya Lin (MIT CS)",
                subjectTag = "Coding",
                status = "Focusing",
                focusedMinutes = 90,
                distractionCount = 1,
                lastReaction = "🎯"
            ),
            RoomMemberModel(
                id = "peer_lucas",
                roomId = "global",
                userId = "peer_3",
                displayName = "Lucas Silva (Stanford)",
                subjectTag = "Mathematics",
                status = "On Break",
                focusedMinutes = 65,
                distractionCount = 2,
                lastReaction = "📚"
            )
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRoomMembers: StateFlow<List<RoomMemberModel>> = _activeRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) {
                flowOf(emptyList())
            } else {
                firestoreRepository.observeRoomMembers(roomId)
                    .catch { emit(emptyList()) }
            }
        }
        .combine(simulatedPeers) { liveMembers, peers ->
            val merged = mutableListOf<RoomMemberModel>()
            merged.addAll(liveMembers)
            peers.forEach { peer ->
                if (merged.none { it.userId == peer.userId }) {
                    merged.add(peer)
                }
            }
            merged.sortedWith(
                compareByDescending<RoomMemberModel> { it.focusedMinutes }
                    .thenBy { it.distractionCount }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        startTimerLoop()
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            while (true) {
                val current = fastState.value
                if (current.isStudyModeActive && current.endTimeMillis > 0L) {
                    val diffMillis = current.endTimeMillis - System.currentTimeMillis()
                    if (diffMillis <= 0L) {
                        _remainingSeconds.value = 0L
                        completeFocusSession(current.totalDurationMinutes, current.activeSubject)
                    } else {
                        _remainingSeconds.value = diffMillis / 1000L
                    }
                } else {
                    _remainingSeconds.value = (current.totalDurationMinutes * 60L)
                }
                delay(1000L)
            }
        }
    }

    fun startFocusSession(durationMinutes: Int, subjectTag: String, strictMode: Boolean) {
        val endMillis = System.currentTimeMillis() + (durationMinutes * 60_000L)
        fastStateManager.updateState {
            it.copy(
                isStudyModeActive = true,
                endTimeMillis = endMillis,
                totalDurationMinutes = durationMinutes,
                activeSubject = subjectTag,
                isStrictMode = strictMode
            )
        }
        _remainingSeconds.value = durationMinutes * 60L
        _statusBannerMessage.value = "Focus session started ($durationMinutes min • $subjectTag)"
        syncMemberStatusInActiveRoom(status = "Focusing")
    }

    fun stopFocusSessionEarly() {
        val current = fastState.value
        if (current.isStudyModeActive && current.isStrictMode) {
            _statusBannerMessage.value = "Strict Mode is locked! Pass the 3-question AI Subject Quiz to unlock early."
            return
        }
        fastStateManager.updateState {
            it.copy(
                isStudyModeActive = false,
                endTimeMillis = 0L,
                isStrictMode = false
            )
        }
        syncMemberStatusInActiveRoom(status = "On Break")
    }

    private fun completeFocusSession(durationMinutes: Int, subjectTag: String) {
        fastStateManager.updateState {
            it.copy(
                isStudyModeActive = false,
                endTimeMillis = 0L,
                isStrictMode = false
            )
        }
        recordDailyFocusProgress(durationMinutes)
        viewModelScope.launch {
            firestoreRepository.logStudySession(
                subjectTag = subjectTag,
                durationMinutes = durationMinutes,
                distractionsBlocked = fastState.value.shortsBlockedCount
            )
            _statusBannerMessage.value = "Completed $durationMinutes min $subjectTag session! Saved to Cloud Analytics."
            syncMemberStatusInActiveRoom(status = "On Break", addFocusedMinutes = durationMinutes)
        }
    }

    fun logManualCompletedSession(durationMinutes: Int, subjectTag: String) {
        recordDailyFocusProgress(durationMinutes)
        viewModelScope.launch {
            val result = firestoreRepository.logStudySession(
                subjectTag = subjectTag,
                durationMinutes = durationMinutes,
                distractionsBlocked = fastState.value.shortsBlockedCount
            )
            if (result.isSuccess) {
                val st = fastState.value
                _statusBannerMessage.value = if (st.isTodayGoalMet) {
                    "🎉 Daily Goal Met (${st.todayFocusedMinutes}/${st.dailyFocusGoalMinutes}m)! Streak extended to ${st.consecutiveStreakDays} days!"
                } else {
                    "Logged $durationMinutes min $subjectTag session (${st.todayFocusedMinutes}/${st.dailyFocusGoalMinutes}m today)!"
                }
                syncMemberStatusInActiveRoom(status = "Focusing", addFocusedMinutes = durationMinutes)
            } else {
                _statusBannerMessage.value = "Logged locally (${fastState.value.todayFocusedMinutes}/${fastState.value.dailyFocusGoalMinutes}m today)!"
            }
        }
    }

    private fun recordDailyFocusProgress(minutesAdded: Int) {
        var newlyMet = false
        fastStateManager.updateState { current ->
            val nextToday = (current.todayFocusedMinutes + minutesAdded).coerceAtLeast(0)
            val goalReached = nextToday >= current.dailyFocusGoalMinutes
            val shouldIncrementStreak = goalReached && !current.isTodayGoalMet
            if (shouldIncrementStreak) {
                newlyMet = true
            }
            val nextStreak = if (shouldIncrementStreak) {
                current.consecutiveStreakDays + 1
            } else {
                current.consecutiveStreakDays
            }
            val nextBest = maxOf(current.bestStreakDays, nextStreak)
            current.copy(
                todayFocusedMinutes = nextToday,
                consecutiveStreakDays = nextStreak,
                bestStreakDays = nextBest,
                isTodayGoalMet = current.isTodayGoalMet || goalReached
            )
        }
        if (newlyMet || fastState.value.isTodayGoalMet) {
            _streakCelebrationTrigger.value += 1
        }
    }

    fun updateDailyFocusGoal(goalMinutes: Int) {
        val target = goalMinutes.coerceIn(15, 240)
        var newlyMet = false
        fastStateManager.updateState { current ->
            val goalReached = current.todayFocusedMinutes >= target
            val shouldIncrementStreak = goalReached && !current.isTodayGoalMet
            if (shouldIncrementStreak) {
                newlyMet = true
            }
            val nextStreak = if (shouldIncrementStreak) {
                current.consecutiveStreakDays + 1
            } else {
                current.consecutiveStreakDays
            }
            current.copy(
                dailyFocusGoalMinutes = target,
                consecutiveStreakDays = nextStreak,
                bestStreakDays = maxOf(current.bestStreakDays, nextStreak),
                isTodayGoalMet = current.isTodayGoalMet || goalReached
            )
        }
        if (newlyMet) {
            _streakCelebrationTrigger.value += 1
            _statusBannerMessage.value = "🎉 Daily focus goal (${target}m) achieved! Streak is now ${fastState.value.consecutiveStreakDays} days!"
        }
    }

    fun triggerStreakCelebration() {
        _streakCelebrationTrigger.value += 1
    }

    fun updateSubjectTag(subject: String) {
        fastStateManager.updateState { it.copy(activeSubject = subject) }
    }

    fun updateTimerPresetMinutes(minutes: Int) {
        if (!fastState.value.isStudyModeActive) {
            fastStateManager.updateState { it.copy(totalDurationMinutes = minutes.coerceIn(5, 180)) }
            _remainingSeconds.value = minutes.coerceIn(5, 180) * 60L
        }
    }

    fun toggleStrictMode(enabled: Boolean) {
        if (fastState.value.isStudyModeActive && fastState.value.isStrictMode && !enabled) {
            _statusBannerMessage.value = "Cannot disable Strict Mode during an active timer without passing the AI Quiz!"
            return
        }
        fastStateManager.updateState { it.copy(isStrictMode = enabled) }
    }

    fun toggleBlockShorts(enabled: Boolean) {
        if (fastState.value.isStudyModeActive && fastState.value.isStrictMode && !enabled) {
            _statusBannerMessage.value = "Strict Mode prevents disabling the YouTube Shorts Blocker during study!"
            return
        }
        fastStateManager.updateState { it.copy(blockShorts = enabled) }
    }

    fun togglePermissionSimulation(permissionType: String, enabled: Boolean) {
        fastStateManager.updateState {
            when (permissionType) {
                "ACCESSIBILITY" -> it.copy(accessibilityPermissionEnabled = enabled)
                "USAGE_STATS" -> it.copy(usageStatsPermissionEnabled = enabled)
                "OVERLAY" -> it.copy(overlayPermissionEnabled = enabled)
                else -> it
            }
        }
    }

    // Real-time App/Tab Switch Distraction Detection
    fun onAppBackgroundedOrTabSwitched(source: String = "App Minimized / Switched") {
        val current = fastState.value
        if (!current.isStudyModeActive) return

        fastStateManager.updateState {
            it.copy(totalDistractionsCount = it.totalDistractionsCount + 1)
        }
        _distractionAlertMessage.value =
            "Distraction Detected ($source)! Your Study Room status was updated to 'Distracted'. Return to focus immediately."
        syncMemberStatusInActiveRoom(status = "Distracted", incrementDistraction = true)
    }

    fun dismissDistractionAlertAndResumeFocus() {
        _distractionAlertMessage.value = null
        if (fastState.value.isStudyModeActive) {
            syncMemberStatusInActiveRoom(status = "Focusing")
        }
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun updateStudyNotes(notes: String) {
        _studyNotes.value = notes
    }

    // --- Smart YouTube Study Filter Pipeline ---
    fun evaluateYoutubeVideo(urlOrQuery: String, title: String, channelName: String) {
        viewModelScope.launch {
            _videoEvalState.value = VideoEvalState.Evaluating(
                title = title.ifBlank { urlOrQuery },
                stepMessage = "Running 5-Step Focus Pipeline (Shorts -> Whitelist -> Room SQLite Cache -> Gemini 3.8 Flash)..."
            )
            val result = classifierRepository.evaluateVideoPipeline(
                rawUrlOrQuery = urlOrQuery,
                videoTitle = title,
                channelName = channelName,
                activeSubject = fastState.value.activeSubject,
                blockShortsEnabled = fastState.value.blockShorts
            )
            result.fold(
                onSuccess = { decision ->
                    if (decision.isShortsBlocked || !decision.isStudyRelated) {
                        fastStateManager.updateState {
                            it.copy(
                                shortsBlockedCount = it.shortsBlockedCount + 1,
                                totalDistractionsCount = it.totalDistractionsCount + 1
                            )
                        }
                    }
                    _videoEvalState.value = VideoEvalState.Result(decision)
                },
                onFailure = { err ->
                    _videoEvalState.value = VideoEvalState.Error(
                        err.localizedMessage ?: "Failed to classify video with Gemini AI."
                    )
                }
            )
        }
    }

    fun resetVideoEvaluation() {
        _videoEvalState.value = VideoEvalState.Idle
    }

    // --- Strict Mode AI Quiz Unlock ---
    fun startStrictModeUnlockQuiz() {
        val subject = fastState.value.activeSubject
        viewModelScope.launch {
            _quizState.value = QuizUiState.Loading
            val res = classifierRepository.generateStrictModeQuiz(subject)
            res.fold(
                onSuccess = { questions ->
                    _quizState.value = QuizUiState.Active(questions = questions)
                },
                onFailure = { err ->
                    _quizState.value = QuizUiState.Error(
                        err.localizedMessage ?: "Failed to generate Strict Mode quiz."
                    )
                }
            )
        }
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        val current = _quizState.value as? QuizUiState.Active ?: return
        if (current.submitted) return
        val updated = current.selectedOptions.toMutableMap().apply {
            put(questionIndex, optionIndex)
        }
        _quizState.value = current.copy(selectedOptions = updated)
    }

    fun submitStrictModeQuiz() {
        val current = _quizState.value as? QuizUiState.Active ?: return
        var correct = 0
        current.questions.forEachIndexed { index, q ->
            if (current.selectedOptions[index] == q.correctIndex) {
                correct++
            }
        }
        if (correct >= 2) {
            // Unlock strict mode and stop timer
            fastStateManager.updateState {
                it.copy(
                    isStudyModeActive = false,
                    isStrictMode = false,
                    endTimeMillis = 0L
                )
            }
            _quizState.value = QuizUiState.Passed
            _statusBannerMessage.value = "Quiz passed ($correct/${current.questions.size})! Strict Mode unlocked."
        } else {
            _quizState.value = current.copy(submitted = true, score = correct)
        }
    }

    fun dismissQuizModal() {
        _quizState.value = QuizUiState.Idle
    }

    // --- Local Room Database Management ---
    fun addWhitelistItem(nameOrKeyword: String, type: String, subjectTag: String) {
        val trimmed = nameOrKeyword.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            whitelistDao.insertWhitelist(
                WhitelistEntity(
                    nameOrKeyword = trimmed,
                    type = if (type == "CHANNEL") "CHANNEL" else "KEYWORD",
                    subjectTag = subjectTag.ifBlank { fastState.value.activeSubject }
                )
            )
            _statusBannerMessage.value = "Added '$trimmed' to Local Whitelist"
        }
    }

    fun removeWhitelistItem(id: Int) {
        viewModelScope.launch {
            whitelistDao.deleteWhitelistById(id)
        }
    }

    fun clearAllVideoCache() {
        viewModelScope.launch {
            videoCacheDao.clearAllCache()
            _statusBannerMessage.value = "Cleared local Room video_cache table"
        }
    }

    fun deleteCachedVideo(videoId: String) {
        viewModelScope.launch {
            videoCacheDao.deleteCachedVideo(videoId)
        }
    }

    fun toggleAppBlockRule(rule: BlockRuleEntity) {
        if (fastState.value.isStudyModeActive && fastState.value.isStrictMode && rule.isBlockActive) {
            _statusBannerMessage.value = "Strict Mode is active! Cannot unblock ${rule.appName} until session ends."
            return
        }
        viewModelScope.launch {
            blockRuleDao.insertBlockRule(rule.copy(isBlockActive = !rule.isBlockActive))
        }
    }

    fun updateAppDailyLimit(rule: BlockRuleEntity, newLimitMinutes: Int) {
        if (fastState.value.isStudyModeActive && fastState.value.isStrictMode) {
            _statusBannerMessage.value = "Strict Mode prevents editing block limits during an active focus session."
            return
        }
        viewModelScope.launch {
            blockRuleDao.insertBlockRule(rule.copy(dailyLimitMinutes = newLimitMinutes.coerceIn(0, 180)))
        }
    }

    // --- Collaborative Study Rooms ---
    fun createCollaborativeRoom(roomName: String, subjectTag: String, timerDurationMinutes: Int) {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        viewModelScope.launch {
            val res = firestoreRepository.createStudyRoom(
                roomCode = code,
                roomName = roomName,
                subjectTag = subjectTag,
                hostName = currentUserName,
                timerDurationMinutes = timerDurationMinutes
            )
            res.fold(
                onSuccess = { roomId ->
                    _activeRoomId.value = roomId
                    _statusBannerMessage.value = "Created Study Room '$roomName' (Code: $code)"
                },
                onFailure = { err ->
                    _statusBannerMessage.value = "Could not create room: ${err.localizedMessage}"
                }
            )
        }
    }

    fun joinStudyRoom(room: StudyRoomModel) {
        _activeRoomId.value = room.id
        viewModelScope.launch {
            firestoreRepository.upsertRoomMember(
                roomId = room.id,
                displayName = currentUserName,
                subjectTag = fastState.value.activeSubject,
                status = if (fastState.value.isStudyModeActive) "Focusing" else "On Break",
                focusedMinutes = 25,
                distractionCount = fastState.value.totalDistractionsCount,
                lastReaction = "🔥"
            )
            _statusBannerMessage.value = "Joined Study Room '${room.roomName}' (${room.roomCode})"
        }
    }

    fun joinStudyRoomByCode(codeInput: String) {
        val clean = codeInput.trim().uppercase()
        val rooms = (studyRoomsState.value as? UiState.Success)?.data.orEmpty()
        val matched = rooms.firstOrNull { it.roomCode.equals(clean, ignoreCase = true) }
        if (matched != null) {
            joinStudyRoom(matched)
        } else {
            _statusBannerMessage.value = "No active room found with code '$clean'. Create one instead!"
        }
    }

    fun leaveActiveRoom() {
        _activeRoomId.value = null
    }

    fun sendAccountabilityReaction(reaction: String) {
        val roomId = _activeRoomId.value ?: return
        val membersCount = activeRoomMembers.value.size.coerceAtLeast(1)
        viewModelScope.launch {
            firestoreRepository.sendRoomReaction(roomId, reaction, membersCount)
            syncMemberStatusInActiveRoom(
                status = if (fastState.value.isStudyModeActive) "Focusing" else "On Break",
                reaction = reaction
            )
            _statusBannerMessage.value = "Sent silent accountability reaction $reaction to room!"
        }
    }

    private fun syncMemberStatusInActiveRoom(
        status: String,
        addFocusedMinutes: Int = 0,
        incrementDistraction: Boolean = false,
        reaction: String = "🔥"
    ) {
        val roomId = _activeRoomId.value ?: return
        val existingSelf = activeRoomMembers.value.firstOrNull { it.userId == currentUserId }
        val nextFocused = (existingSelf?.focusedMinutes ?: 25) + addFocusedMinutes
        val nextDistractions = (existingSelf?.distractionCount ?: fastState.value.totalDistractionsCount) +
            (if (incrementDistraction) 1 else 0)

        viewModelScope.launch {
            firestoreRepository.upsertRoomMember(
                roomId = roomId,
                displayName = currentUserName,
                subjectTag = fastState.value.activeSubject,
                status = status,
                focusedMinutes = nextFocused,
                distractionCount = nextDistractions,
                lastReaction = reaction
            )
        }
    }
}
