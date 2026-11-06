package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. fast_state (Sub-millisecond Key-Value Store)
data class FastFocusState(
    val isStudyModeActive: Boolean = false,
    val endTimeMillis: Long = 0L,
    val totalDurationMinutes: Int = 25,
    val isStrictMode: Boolean = false,
    val blockShorts: Boolean = true,
    val activeSubject: String = "Engineering",
    val shortsBlockedCount: Int = 14,
    val totalDistractionsCount: Int = 3,
    val accessibilityPermissionEnabled: Boolean = true,
    val usageStatsPermissionEnabled: Boolean = true,
    val overlayPermissionEnabled: Boolean = true,
    val dailyFocusGoalMinutes: Int = 60,
    val todayFocusedMinutes: Int = 45,
    val consecutiveStreakDays: Int = 7,
    val bestStreakDays: Int = 14,
    val isTodayGoalMet: Boolean = false
)

class FastStateManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("regain_fast_state", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(readFromPrefs())
    val state: StateFlow<FastFocusState> = _state.asStateFlow()

    private fun readFromPrefs(): FastFocusState {
        return FastFocusState(
            isStudyModeActive = prefs.getBoolean("isStudyModeActive", false),
            endTimeMillis = prefs.getLong("endTimeMillis", 0L),
            totalDurationMinutes = prefs.getInt("totalDurationMinutes", 25),
            isStrictMode = prefs.getBoolean("isStrictMode", false),
            blockShorts = prefs.getBoolean("blockShorts", true),
            activeSubject = prefs.getString("activeSubject", "Engineering") ?: "Engineering",
            shortsBlockedCount = prefs.getInt("shortsBlockedCount", 14),
            totalDistractionsCount = prefs.getInt("totalDistractionsCount", 3),
            accessibilityPermissionEnabled = prefs.getBoolean("accessibilityPermissionEnabled", true),
            usageStatsPermissionEnabled = prefs.getBoolean("usageStatsPermissionEnabled", true),
            overlayPermissionEnabled = prefs.getBoolean("overlayPermissionEnabled", true),
            dailyFocusGoalMinutes = prefs.getInt("dailyFocusGoalMinutes", 60),
            todayFocusedMinutes = prefs.getInt("todayFocusedMinutes", 45),
            consecutiveStreakDays = prefs.getInt("consecutiveStreakDays", 7),
            bestStreakDays = prefs.getInt("bestStreakDays", 14),
            isTodayGoalMet = prefs.getBoolean("isTodayGoalMet", false)
        )
    }

    fun updateState(transform: (FastFocusState) -> FastFocusState) {
        val next = transform(_state.value)
        prefs.edit()
            .putBoolean("isStudyModeActive", next.isStudyModeActive)
            .putLong("endTimeMillis", next.endTimeMillis)
            .putInt("totalDurationMinutes", next.totalDurationMinutes)
            .putBoolean("isStrictMode", next.isStrictMode)
            .putBoolean("blockShorts", next.blockShorts)
            .putString("activeSubject", next.activeSubject)
            .putInt("shortsBlockedCount", next.shortsBlockedCount)
            .putInt("totalDistractionsCount", next.totalDistractionsCount)
            .putBoolean("accessibilityPermissionEnabled", next.accessibilityPermissionEnabled)
            .putBoolean("usageStatsPermissionEnabled", next.usageStatsPermissionEnabled)
            .putBoolean("overlayPermissionEnabled", next.overlayPermissionEnabled)
            .putInt("dailyFocusGoalMinutes", next.dailyFocusGoalMinutes)
            .putInt("todayFocusedMinutes", next.todayFocusedMinutes)
            .putInt("consecutiveStreakDays", next.consecutiveStreakDays)
            .putInt("bestStreakDays", next.bestStreakDays)
            .putBoolean("isTodayGoalMet", next.isTodayGoalMet)
            .apply()
        _state.value = next
    }
}

// 2. video_cache table
@Entity(tableName = "video_cache")
data class VideoCacheEntity(
    @PrimaryKey val videoId: String,
    val videoTitle: String,
    val channelName: String,
    val isStudyRelated: Boolean,
    val category: String,
    val confidence: Double,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

// 3. whitelist table
@Entity(tableName = "whitelist")
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameOrKeyword: String,
    val type: String, // "CHANNEL" or "KEYWORD"
    val subjectTag: String,
    val addedAt: Long = System.currentTimeMillis()
)

// 4. block_rules table
@Entity(tableName = "block_rules")
data class BlockRuleEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String,
    val dailyLimitMinutes: Int,
    val usedTodayMinutes: Int,
    val isBlockActive: Boolean
)

@Dao
interface VideoCacheDao {
    @Query("SELECT * FROM video_cache ORDER BY timestamp DESC")
    fun observeAllCachedVideos(): Flow<List<VideoCacheEntity>>

    @Query("SELECT * FROM video_cache WHERE videoId = :videoId LIMIT 1")
    suspend fun getCachedVideoById(videoId: String): VideoCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedVideo(video: VideoCacheEntity)

    @Query("DELETE FROM video_cache WHERE videoId = :videoId")
    suspend fun deleteCachedVideo(videoId: String)

    @Query("DELETE FROM video_cache")
    suspend fun clearAllCache()
}

@Dao
interface WhitelistDao {
    @Query("SELECT * FROM whitelist ORDER BY addedAt DESC")
    fun observeWhitelist(): Flow<List<WhitelistEntity>>

    @Query("SELECT * FROM whitelist")
    suspend fun getAllWhitelist(): List<WhitelistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhitelist(item: WhitelistEntity)

    @Query("DELETE FROM whitelist WHERE id = :id")
    suspend fun deleteWhitelistById(id: Int)
}

@Dao
interface BlockRuleDao {
    @Query("SELECT * FROM block_rules ORDER BY appName ASC")
    fun observeBlockRules(): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rules")
    suspend fun getAllBlockRules(): List<BlockRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockRule(rule: BlockRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBlockRules(rules: List<BlockRuleEntity>)
}

@Database(
    entities = [VideoCacheEntity::class, WhitelistEntity::class, BlockRuleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RegainDatabase : RoomDatabase() {
    abstract fun videoCacheDao(): VideoCacheDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun blockRuleDao(): BlockRuleDao

    companion object {
        @Volatile
        private var INSTANCE: RegainDatabase? = null

        fun getInstance(context: Context): RegainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RegainDatabase::class.java,
                    "regain_focus_local.db"
                ).build()
                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    seedInitialDataIfNeeded(instance)
                }
                instance
            }
        }

        private suspend fun seedInitialDataIfNeeded(db: RegainDatabase) {
            if (db.whitelistDao().getAllWhitelist().isEmpty()) {
                val defaults = listOf(
                    WhitelistEntity(nameOrKeyword = "MIT OpenCourseWare", type = "CHANNEL", subjectTag = "Engineering"),
                    WhitelistEntity(nameOrKeyword = "NPTEL", type = "CHANNEL", subjectTag = "Engineering"),
                    WhitelistEntity(nameOrKeyword = "Physics Wallah", type = "CHANNEL", subjectTag = "Exam Prep"),
                    WhitelistEntity(nameOrKeyword = "3Blue1Brown", type = "CHANNEL", subjectTag = "Mathematics"),
                    WhitelistEntity(nameOrKeyword = "freeCodeCamp", type = "CHANNEL", subjectTag = "Coding"),
                    WhitelistEntity(nameOrKeyword = "Eigenvalues", type = "KEYWORD", subjectTag = "Mathematics"),
                    WhitelistEntity(nameOrKeyword = "AutoCAD", type = "KEYWORD", subjectTag = "Engineering"),
                    WhitelistEntity(nameOrKeyword = "Data Structures", type = "KEYWORD", subjectTag = "Coding")
                )
                defaults.forEach { db.whitelistDao().insertWhitelist(it) }
            }

            if (db.blockRuleDao().getAllBlockRules().isEmpty()) {
                val defaultRules = listOf(
                    BlockRuleEntity(
                        packageName = "com.google.android.youtube.shorts",
                        appName = "YouTube Shorts",
                        category = "Short-Form Feed",
                        dailyLimitMinutes = 0,
                        usedTodayMinutes = 12,
                        isBlockActive = true
                    ),
                    BlockRuleEntity(
                        packageName = "com.instagram.android",
                        appName = "Instagram Reels & Feed",
                        category = "Social Media",
                        dailyLimitMinutes = 15,
                        usedTodayMinutes = 22,
                        isBlockActive = true
                    ),
                    BlockRuleEntity(
                        packageName = "com.snapchat.android",
                        appName = "Snapchat",
                        category = "Messaging & Stories",
                        dailyLimitMinutes = 10,
                        usedTodayMinutes = 8,
                        isBlockActive = true
                    ),
                    BlockRuleEntity(
                        packageName = "com.twitter.android",
                        appName = "X (Twitter)",
                        category = "Social Feed",
                        dailyLimitMinutes = 15,
                        usedTodayMinutes = 19,
                        isBlockActive = true
                    ),
                    BlockRuleEntity(
                        packageName = "com.supercell.brawlstars",
                        appName = "Mobile Games",
                        category = "Gaming",
                        dailyLimitMinutes = 20,
                        usedTodayMinutes = 35,
                        isBlockActive = true
                    )
                )
                db.blockRuleDao().insertAllBlockRules(defaultRules)
            }
        }
    }
}
