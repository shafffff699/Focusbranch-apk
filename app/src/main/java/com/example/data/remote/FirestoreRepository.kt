package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class StudySessionModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subjectTag: String = "Engineering",
    val durationMinutes: Int = 25,
    val distractionsBlocked: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "subjectTag" to subjectTag,
        "durationMinutes" to durationMinutes,
        "distractionsBlocked" to distractionsBlocked,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@IgnoreExtraProperties
data class StudyRoomModel(
    @DocumentId val id: String = "",
    val roomCode: String = "",
    val roomName: String = "",
    val subjectTag: String = "Engineering",
    val hostId: String = "",
    val hostName: String = "",
    val visibility: String = "public",
    val timerDurationMinutes: Int = 50,
    val activeMembersCount: Int = 1,
    val lastReaction: String = "🔥",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "roomCode" to roomCode,
        "roomName" to roomName,
        "subjectTag" to subjectTag,
        "hostId" to hostId,
        "hostName" to hostName,
        "visibility" to visibility,
        "timerDurationMinutes" to timerDurationMinutes,
        "activeMembersCount" to activeMembersCount,
        "lastReaction" to lastReaction,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@IgnoreExtraProperties
data class RoomMemberModel(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val subjectTag: String = "Engineering",
    val status: String = "Focusing",
    val focusedMinutes: Int = 0,
    val distractionCount: Int = 0,
    val lastReaction: String = "🎯",
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "roomId" to roomId,
        "userId" to userId,
        "displayName" to displayName,
        "subjectTag" to subjectTag,
        "status" to status,
        "focusedMinutes" to focusedMinutes,
        "distractionCount" to distractionCount,
        "lastReaction" to lastReaction,
        "visibility" to visibility,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

class FirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. Study Sessions ---

    suspend fun logStudySession(
        subjectTag: String,
        durationMinutes: Int,
        distractionsBlocked: Int
    ): Result<String> {
        val uid = requireUserId()
        val sessionsRef = db.collection("study_sessions")
        return try {
            val model = StudySessionModel(
                userId = uid,
                subjectTag = subjectTag.take(60).ifBlank { "General Study" },
                durationMinutes = durationMinutes.coerceIn(1, 600),
                distractionsBlocked = distractionsBlocked.coerceIn(0, 1000)
            )
            val docRef = sessionsRef.add(model.toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "study_sessions")
            Result.failure(e)
        }
    }

    suspend fun getStudySessionById(sessionId: String): Result<StudySessionModel?> {
        val docRef = db.collection("study_sessions").document(sessionId)
        return try {
            val snap = docRef.get().await()
            val item = snap.toObject(
                StudySessionModel::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeUserStudySessions(userId: String): Flow<List<StudySessionModel>> {
        val path = "study_sessions"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    StudySessionModel::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    // --- 2. Collaborative Study Rooms ---

    suspend fun createStudyRoom(
        roomCode: String,
        roomName: String,
        subjectTag: String,
        hostName: String,
        timerDurationMinutes: Int
    ): Result<String> {
        val uid = requireUserId()
        val roomsRef = db.collection("study_rooms")
        return try {
            val cleanCode = roomCode.uppercase().filter { it.isLetterOrDigit() }.padEnd(6, 'X').take(6)
            val roomModel = StudyRoomModel(
                roomCode = cleanCode,
                roomName = roomName.trim().take(60).ifBlank { "Focus Sanctuary" },
                subjectTag = subjectTag.trim().take(40).ifBlank { "Engineering" },
                hostId = uid,
                hostName = hostName.trim().take(60).ifBlank { "Student" },
                visibility = "public",
                timerDurationMinutes = timerDurationMinutes.coerceIn(5, 180),
                activeMembersCount = 1,
                lastReaction = "🔥"
            )
            val docRef = roomsRef.add(roomModel.toCreateMap()).await()
            upsertRoomMember(
                roomId = docRef.id,
                displayName = roomModel.hostName,
                subjectTag = roomModel.subjectTag,
                status = "Focusing",
                focusedMinutes = 0,
                distractionCount = 0,
                lastReaction = "🔥"
            )
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "study_rooms")
            Result.failure(e)
        }
    }

    fun observePublicStudyRooms(): Flow<List<StudyRoomModel>> {
        val path = "study_rooms"
        return db.collection(path)
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    StudyRoomModel::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun sendRoomReaction(roomId: String, reaction: String, activeMembersCount: Int): Result<Unit> {
        requireUserId()
        val docRef = db.collection("study_rooms").document(roomId)
        return try {
            docRef.update(
                mapOf(
                    "lastReaction" to reaction.take(40),
                    "activeMembersCount" to activeMembersCount.coerceIn(1, 200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 3. Room Members & Leaderboard ---

    suspend fun upsertRoomMember(
        roomId: String,
        displayName: String,
        subjectTag: String,
        status: String,
        focusedMinutes: Int,
        distractionCount: Int,
        lastReaction: String
    ): Result<String> {
        val uid = requireUserId()
        val cleanRoomId = roomId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(60)
        val cleanUid = uid.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(60)
        val memberDocId = "${cleanRoomId}_${cleanUid}"
        val docRef = db.collection("room_members").document(memberDocId)
        return try {
            val validStatus = if (status in listOf("Focusing", "On Break", "Distracted")) status else "Focusing"
            val model = RoomMemberModel(
                roomId = roomId,
                userId = uid,
                displayName = displayName.take(60).ifBlank { "Student" },
                subjectTag = subjectTag.take(40).ifBlank { "Engineering" },
                status = validStatus,
                focusedMinutes = focusedMinutes.coerceIn(0, 10000),
                distractionCount = distractionCount.coerceIn(0, 1000),
                lastReaction = lastReaction.take(40),
                visibility = "public"
            )
            docRef.set(model.toCreateMap()).await()
            Result.success(memberDocId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeRoomMembers(roomId: String): Flow<List<RoomMemberModel>> {
        val path = "room_members"
        return db.collection(path)
            .whereEqualTo("visibility", "public")
            .whereEqualTo("roomId", roomId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    RoomMemberModel::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }
}
