package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarEmoji: String,
    val screenTimeLimitMinutes: Int = 120,
    val screenTimeUsedMinutes: Int = 0,
    val isDeviceLocked: Boolean = false,
    val latitude: Double = 37.4220, // default (e.g. Mountain View)
    val longitude: Double = -122.0841,
    val currentStatus: String = "Within Safe Zone (Home)",
    val pocketMoneyBalance: Double = 0.0
)

@Entity(tableName = "chore_tasks")
data class ChoreTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: Long,
    val title: String,
    val points: Int,
    val isCompletedByChild: Boolean = false,
    val isApprovedByParent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val logType: String, // SCREEN_TIME, LOCATION, APP_BLOCK, CHORE, GENERAL
    val message: String,
    val severity: String // INFO, WARNING, ALERT
)

@Entity(tableName = "app_controls")
data class AppControl(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: Long,
    val appName: String,
    val packageName: String,
    val isBlocked: Boolean = false,
    val category: String, // CHAT, GAME, STUDY, MEDIA
    val iconEmoji: String
)
