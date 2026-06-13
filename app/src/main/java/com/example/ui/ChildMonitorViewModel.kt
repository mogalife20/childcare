package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityLog
import com.example.data.AppDatabase
import com.example.data.AppControl
import com.example.data.ChildProfile
import com.example.data.ChildMonitorRepository
import com.example.data.ChoreTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChildMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChildMonitorRepository
    val children: StateFlow<List<ChildProfile>>

    private val _selectedChildId = MutableStateFlow<Long?>(null)
    val selectedChildId: StateFlow<Long?> = _selectedChildId.asStateFlow()

    // Observe active child details dynamically based on active selection
    val selectedChild: StateFlow<ChildProfile?> = _selectedChildId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getChildById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe active child's chores
    val chores: StateFlow<List<ChoreTask>> = _selectedChildId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getChoresForChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe active child's logs
    val logs: StateFlow<List<ActivityLog>> = _selectedChildId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getLogsForChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe active child's apps
    val apps: StateFlow<List<AppControl>> = _selectedChildId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getAppsForChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Location Simulation Machine Tracker
    private var locationStep = 0

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChildMonitorRepository(database.dao())

        // Fetch child list
        children = repository.allChildren.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data and set standard initial selection
        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
            // Auto select first child when loaded
            repository.allChildren.collect { list ->
                if (_selectedChildId.value == null && list.isNotEmpty()) {
                    _selectedChildId.value = list.first().id
                }
            }
        }
    }

    fun selectChild(id: Long) {
        _selectedChildId.value = id
    }

    // Toggle Device Lock State
    fun toggleDeviceLock(child: ChildProfile) {
        viewModelScope.launch {
            val updated = child.copy(
                isDeviceLocked = !child.isDeviceLocked,
                currentStatus = if (!child.isDeviceLocked) {
                    "Device Locked - Remote Admin"
                } else if (child.screenTimeUsedMinutes >= child.screenTimeLimitMinutes) {
                    "Device Locked - Limit Reached"
                } else {
                    "Device Unlocked - Free Use"
                }
            )
            repository.updateChild(updated)

            val logMsg = if (updated.isDeviceLocked) {
                "🔒 Clicked INSTANT LOCK command for ${child.name}'s device"
            } else {
                "🔓 Sent INSTANT UNLOCK command for ${child.name}'s device"
            }
            repository.insertLog(
                ActivityLog(
                    childId = child.id,
                    logType = "GENERAL",
                    message = logMsg,
                    severity = if (updated.isDeviceLocked) "ALERT" else "INFO"
                )
            )
        }
    }

    // Change Screen Time Limit Slider
    fun setScreenTimeLimit(child: ChildProfile, newLimit: Int) {
        viewModelScope.launch {
            val status = if (child.screenTimeUsedMinutes >= newLimit) {
                "Device Locked - Limit Reached"
            } else {
                child.currentStatus
            }
            val updated = child.copy(
                screenTimeLimitMinutes = newLimit,
                currentStatus = status,
                isDeviceLocked = if (child.screenTimeUsedMinutes >= newLimit) true else child.isDeviceLocked
            )
            repository.updateChild(updated)
            repository.insertLog(
                ActivityLog(
                    childId = child.id,
                    logType = "SCREEN_TIME",
                    message = "⏳ Daily screen time limit adjusted to $newLimit minutes",
                    severity = "INFO"
                )
            )
            if (child.screenTimeUsedMinutes >= newLimit) {
                repository.insertLog(
                    ActivityLog(
                        childId = child.id,
                        logType = "SCREEN_TIME",
                        message = "🚨 Device locked automatically. Daily limit exceeded ($newLimit min limit vs ${child.screenTimeUsedMinutes} min used)",
                        severity = "ALERT"
                    )
                )
            }
        }
    }

    // Adjust screen time used minutes (Simulation input)
    fun adjustScreenTimeUsed(child: ChildProfile, minutesDelta: Int) {
        viewModelScope.launch {
            val nextUsed = (child.screenTimeUsedMinutes + minutesDelta).coerceAtLeast(0)
            val reachedLimit = nextUsed >= child.screenTimeLimitMinutes
            val status = if (reachedLimit) "Device Locked - Limit Reached" else "Within limits (${nextUsed}/${child.screenTimeLimitMinutes}m)"
            val updated = child.copy(
                screenTimeUsedMinutes = nextUsed,
                currentStatus = status,
                isDeviceLocked = if (reachedLimit) true else child.isDeviceLocked
            )
            repository.updateChild(updated)
            repository.insertLog(
                ActivityLog(
                    childId = child.id,
                    logType = "SCREEN_TIME",
                    message = "⏰ Simulated screen usage updated by ${if (minutesDelta > 0) "+" else ""}$minutesDelta min (Total: $nextUsed min)",
                    severity = if (reachedLimit) "ALERT" else "INFO"
                )
            )
            if (reachedLimit) {
                repository.insertLog(
                    ActivityLog(
                        childId = child.id,
                        logType = "SCREEN_TIME",
                        message = "🔒 Device locked. Limit ($nextUsed min) surpassed",
                        severity = "ALERT"
                    )
                )
            }
        }
    }

    // Toggle app blockage
    fun toggleAppBlock(app: AppControl) {
        viewModelScope.launch {
            val updated = app.copy(isBlocked = !app.isBlocked)
            repository.updateAppControl(updated)
            repository.insertLog(
                ActivityLog(
                    childId = app.childId,
                    logType = "APP_BLOCK",
                    message = "🛡️ ${app.appName} block policy toggled to: ${if (updated.isBlocked) "BLOCKED ❌" else "APPROVED ✅"}",
                    severity = "INFO"
                )
            )
        }
    }

    // Trigger simulation of an app block alert (simulation trigger from parent dashboard)
    fun simulateAppOpenAttempt(childId: Long, app: AppControl) {
        viewModelScope.launch {
            if (app.isBlocked) {
                repository.insertLog(
                    ActivityLog(
                        childId = childId,
                        logType = "APP_BLOCK",
                        message = "🚫 Attempted to launch blocked app: ${app.appName} (${app.packageName})",
                        severity = "ALERT"
                    )
                )
            } else {
                repository.insertLog(
                    ActivityLog(
                        childId = childId,
                        logType = "APP_BLOCK",
                        message = "🟢 Opened approved app successfully: ${app.appName}",
                        severity = "INFO"
                    )
                )
            }
        }
    }

    // Add Chore
    fun addNewChore(childId: Long, title: String, points: Int) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch
            val chore = ChoreTask(
                childId = childId,
                title = title.trim(),
                points = points,
                isCompletedByChild = false,
                isApprovedByParent = false
            )
            repository.insertChore(chore)
            repository.insertLog(
                ActivityLog(
                    childId = childId,
                    logType = "CHORE",
                    message = "📝 Admin assigned a new chore: '$title' Worth $points points.",
                    severity = "INFO"
                )
            )
        }
    }

    // Chore Action: Mark complete by child (Simulates child finishing work)
    fun completeChore(childId: Long, chore: ChoreTask) {
        viewModelScope.launch {
            val updated = chore.copy(isCompletedByChild = !chore.isCompletedByChild)
            repository.updateChore(updated)
            repository.insertLog(
                ActivityLog(
                    childId = childId,
                    logType = "CHORE",
                    message = if (updated.isCompletedByChild) {
                        "🧑‍💻 ${selectedChild.value?.name ?: "Child"} marked chore complete: '${chore.title}'"
                    } else {
                        "↩️ ${selectedChild.value?.name ?: "Child"} undid completion of chore: '${chore.title}'"
                    },
                    severity = "INFO"
                )
            )
        }
    }

    // Chore Action: Parent approves (Simulates parent checking work, paying pocket money)
    fun approveChore(childId: Long, chore: ChoreTask, childProfile: ChildProfile) {
        viewModelScope.launch {
            val updated = chore.copy(isApprovedByParent = true)
            repository.updateChore(updated)

            // Reward child with allowance: 1 point = $0.50 pocket money
            val rewardMoney = chore.points * 0.50
            val updatedChildProfile = childProfile.copy(
                pocketMoneyBalance = childProfile.pocketMoneyBalance + rewardMoney
            )
            repository.updateChild(updatedChildProfile)

            repository.insertLog(
                ActivityLog(
                    childId = childId,
                    logType = "CHORE",
                    message = "⭐ Chore Approved! Paid info: '${chore.title}' generated $rewardMoney allowance reward.",
                    severity = "INFO"
                )
            )
        }
    }

    // Delete Chore
    fun deleteChore(chore: ChoreTask) {
        viewModelScope.launch {
            repository.deleteChore(chore)
        }
    }

    // Add child profile
    fun createChildProfile(name: String, avatar: String, dailyLimit: Int) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val newChild = ChildProfile(
                name = name.trim(),
                avatarEmoji = avatar,
                screenTimeLimitMinutes = dailyLimit,
                screenTimeUsedMinutes = 0,
                isDeviceLocked = false,
                currentStatus = "Safe Zone (Home)",
                pocketMoneyBalance = 0.0
            )
            val childId = repository.insertChild(newChild)

            // Populate app defaults
            val defaultApps = listOf(
                AppControl(childId = childId, appName = "YouTube", packageName = "com.google.android.youtube", isBlocked = true, category = "MEDIA", iconEmoji = "🎬"),
                AppControl(childId = childId, appName = "Minecraft", packageName = "com.mojang.minecraftpe", isBlocked = false, category = "GAME", iconEmoji = "⛏️"),
                AppControl(childId = childId, appName = "TikTok", packageName = "com.zhiliaoapp.musically", isBlocked = true, category = "MEDIA", iconEmoji = "🎵"),
                AppControl(childId = childId, appName = "Duolingo", packageName = "com.duolingo", isBlocked = false, category = "STUDY", iconEmoji = "🦉"),
                AppControl(childId = childId, appName = "WhatsApp", packageName = "com.whatsapp", isBlocked = false, category = "CHAT", iconEmoji = "💬")
            )
            defaultApps.forEach { repository.insertAppControl(it) }

            _selectedChildId.value = childId

            repository.insertLog(
                ActivityLog(
                    childId = childId,
                    logType = "GENERAL",
                    message = "✨ Welcome new profile setup completed for $name",
                    severity = "INFO"
                )
            )
        }
    }

    // Delete child profile
    fun deleteChildProfile(child: ChildProfile) {
        viewModelScope.launch {
            repository.deleteChild(child)
            val currentList = children.value.filter { it.id != child.id }
            if (currentList.isNotEmpty()) {
                _selectedChildId.value = currentList.first().id
            } else {
                _selectedChildId.value = null
            }
        }
    }

    // Clear log histories
    fun clearLogs(childId: Long) {
        viewModelScope.launch {
            repository.clearLogsForChild(childId)
            repository.insertLog(
                ActivityLog(
                    childId = childId,
                    logType = "GENERAL",
                    message = "🗑️ Activity logs cleared for profile",
                    severity = "INFO"
                )
            )
        }
    }

    // Simulated Movements: Coordinates cycling with safety zones
    fun simulateLocationMovement(child: ChildProfile) {
        viewModelScope.launch {
            locationStep = (locationStep + 1) % 5
            val locationData = when (locationStep) {
                0 -> Triple(37.4220, -122.0841, "Safe Zone (Home)")
                1 -> Triple(37.4272, -122.0805, "Safe Zone (School)")
                2 -> Triple(37.4250, -122.0825, "Within Safe Zone (Library)")
                3 -> Triple(37.4124, -122.0963, "🚨 Left Safe Zone (Transit Center)")
                4 -> Triple(37.4235, -122.0790, "Within Safe Zone (Park)")
                else -> Triple(37.4220, -122.0841, "Safe Zone (Home)")
            }

            val updated = child.copy(
                latitude = locationData.first,
                longitude = locationData.second,
                currentStatus = locationData.third
            )
            repository.updateChild(updated)

            val severityLevel = if (locationData.third.contains("🚨")) "ALERT" else "INFO"
            repository.insertLog(
                ActivityLog(
                    childId = child.id,
                    logType = "LOCATION",
                    message = "📍 Simulated location updated. Resolved to: ${locationData.third}",
                    severity = severityLevel
                )
            )
        }
    }
}
