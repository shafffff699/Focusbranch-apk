package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun logStudySession_andObserve_authenticatedOwnerSucceeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.logStudySession("Engineering", 50, 4)
        }
        assertTrue(createResult.isSuccess)
        val sessionId = createResult.getOrThrow()
        assertTrue(sessionId.isNotBlank())

        val sessions = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserStudySessions(uid).first { list -> list.any { it.id == sessionId } }
        }
        assertTrue(sessions.any { it.id == sessionId && it.subjectTag == "Engineering" })
    }

    @Test
    fun getStudySessionById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreRepository(firestore)
        val sessionId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.logStudySession("Coding", 25, 1).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreRepository(firestore)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getStudySessionById(sessionId).getOrThrow()
            }
            fail("Expected FirebaseFirestoreException for cross-user session read")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun observePublicStudyRooms_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = FirestoreRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observePublicStudyRooms().first()
            }
            fail("Expected exception for unauthenticated study_rooms query")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun createStudyRoom_andObserveMembers_succeedsForAuthenticatedUsers() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestoreRepository(firestore)
        val roomId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createStudyRoom(
                roomCode = "MIT501",
                roomName = "MIT Eigenvalues Group",
                subjectTag = "Mathematics",
                hostName = "Alice",
                timerDurationMinutes = 50
            ).getOrThrow()
        }

        val rooms = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePublicStudyRooms().first { list -> list.any { it.id == roomId } }
        }
        assertTrue(rooms.any { it.id == roomId && it.roomCode == "MIT501" })

        val members = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeRoomMembers(roomId).first { list -> list.isNotEmpty() }
        }
        assertTrue(members.any { it.displayName == "Alice" && it.status == "Focusing" })
    }

    private companion object {
        const val ALICE_EMAIL = "alice@regain.test"
        const val BOB_EMAIL = "bob@regain.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3500L
    }
}
