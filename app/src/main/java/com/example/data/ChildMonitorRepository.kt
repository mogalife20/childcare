package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ChildMonitorRepository(private val dao: ChildMonitorDao) {

    val allChildren: Flow<List<ChildProfile>> = dao.getAllChildren()

    fun getChildById(id: Long): Flow<ChildProfile?> = dao.getChildById(id)

    fun getChoresForChild(childId: Long): Flow<List<ChoreTask>> = dao.getChoresForChild(childId)

    fun getLogsForChild(childId: Long): Flow<List<ActivityLog>> = dao.getLogsForChild(childId)

    fun getAppsForChild(childId: Long): Flow<List<AppControl>> = dao.getAppsForChild(childId)

    suspend fun insertChild(profile: ChildProfile): Long = dao.insertChild(profile)

    suspend fun updateChild(profile: ChildProfile) = dao.updateChild(profile)

    suspend fun deleteChild(profile: ChildProfile) = dao.deleteChild(profile)

    suspend fun insertChore(chore: ChoreTask) = dao.insertChore(chore)

    suspend fun updateChore(chore: ChoreTask) = dao.updateChore(chore)

    suspend fun deleteChore(chore: ChoreTask) = dao.deleteChore(chore)

    suspend fun insertLog(log: ActivityLog) = dao.insertLog(log)

    suspend fun clearLogsForChild(childId: Long) = dao.clearLogsForChild(childId)

    suspend fun insertAppControl(app: AppControl) = dao.insertAppControl(app)

    suspend fun updateAppControl(app: AppControl) = dao.updateAppControl(app)

    suspend fun seedDefaultDataIfEmpty() {
        val children = dao.getAllChildren().first()
        if (children.isEmpty()) {
            // Seed Child 1: Ethan
            val ethanId = dao.insertChild(
                ChildProfile(
                    name = "Ethan",
                    avatarEmoji = "🧒",
                    screenTimeLimitMinutes = 120,
                    screenTimeUsedMinutes = 45,
                    isDeviceLocked = false,
                    latitude = 37.4220,
                    longitude = -122.0841,
                    currentStatus = "Safe Zone (Home)",
                    pocketMoneyBalance = 5.0
                )
            )

            // Seed Child 2: Maya
            val mayaId = dao.insertChild(
                ChildProfile(
                    name = "Maya",
                    avatarEmoji = "👧",
                    screenTimeLimitMinutes = 90,
                    screenTimeUsedMinutes = 85,
                    isDeviceLocked = true,
                    latitude = 37.4272,
                    longitude = -122.0805,
                    currentStatus = "Device Locked - Limit Reached",
                    pocketMoneyBalance = 12.50
                )
            )

            // Seed App Controls for Ethan
            val appsEthan = listOf(
                AppControl(childId = ethanId, appName = "YouTube", packageName = "com.google.android.youtube", isBlocked = true, category = "MEDIA", iconEmoji = "🎬"),
                AppControl(childId = ethanId, appName = "Minecraft", packageName = "com.mojang.minecraftpe", isBlocked = false, category = "GAME", iconEmoji = "⛏️"),
                AppControl(childId = ethanId, appName = "TikTok", packageName = "com.zhiliaoapp.musically", isBlocked = true, category = "MEDIA", iconEmoji = "🎵"),
                AppControl(childId = ethanId, appName = "Duolingo", packageName = "com.duolingo", isBlocked = false, category = "STUDY", iconEmoji = "🦉"),
                AppControl(childId = ethanId, appName = "Roblox", packageName = "com.roblox.client", isBlocked = false, category = "GAME", iconEmoji = "🧱"),
                AppControl(childId = ethanId, appName = "WhatsApp", packageName = "com.whatsapp", isBlocked = false, category = "CHAT", iconEmoji = "💬")
            )
            appsEthan.forEach { dao.insertAppControl(it) }

            // Seed App Controls for Maya
            val appsMaya = listOf(
                AppControl(childId = mayaId, appName = "YouTube", packageName = "com.google.android.youtube", isBlocked = true, category = "MEDIA", iconEmoji = "🎬"),
                AppControl(childId = mayaId, appName = "Minecraft", packageName = "com.mojang.minecraftpe", isBlocked = true, category = "GAME", iconEmoji = "⛏️"),
                AppControl(childId = mayaId, appName = "TikTok", packageName = "com.zhiliaoapp.musically", isBlocked = true, category = "MEDIA", iconEmoji = "🎵"),
                AppControl(childId = mayaId, appName = "Duolingo", packageName = "com.duolingo", isBlocked = false, category = "STUDY", iconEmoji = "🦉"),
                AppControl(childId = mayaId, appName = "Roblox", packageName = "com.roblox.client", isBlocked = true, category = "GAME", iconEmoji = "🧱"),
                AppControl(childId = mayaId, appName = "WhatsApp", packageName = "com.whatsapp", isBlocked = false, category = "CHAT", iconEmoji = "💬")
            )
            appsMaya.forEach { dao.insertAppControl(it) }

            // Seed Chores for Ethan
            val choresEthan = listOf(
                ChoreTask(childId = ethanId, title = "Tidy up bedroom toys", points = 5, isCompletedByChild = true, isApprovedByParent = false),
                ChoreTask(childId = ethanId, title = "Do 30 mins primary reading", points = 3, isCompletedByChild = false, isApprovedByParent = false),
                ChoreTask(childId = ethanId, title = "Walk the dog around block", points = 4, isCompletedByChild = false, isApprovedByParent = false)
            )
            choresEthan.forEach { dao.insertChore(it) }

            // Seed Chores for Maya
            val choresMaya = listOf(
                ChoreTask(childId = mayaId, title = "Finish weekly Algebra unit", points = 8, isCompletedByChild = true, isApprovedByParent = true),
                ChoreTask(childId = mayaId, title = "Clear dinner table & load dishwasher", points = 4, isCompletedByChild = true, isApprovedByParent = false),
                ChoreTask(childId = mayaId, title = "Practise piano scales 20m", points = 5, isCompletedByChild = false, isApprovedByParent = false)
            )
            choresMaya.forEach { dao.insertChore(it) }

            // Seed logs for Ethan
            val logsEthan = listOf(
                ActivityLog(childId = ethanId, logType = "LOCATION", message = "Ethan entered Home Safe Zone", severity = "INFO"),
                ActivityLog(childId = ethanId, logType = "APP_BLOCK", message = "YouTube launched but blocked target", severity = "WARNING"),
                ActivityLog(childId = ethanId, logType = "GENERAL", message = "Parent increased screen budget by +30m", severity = "INFO"),
                ActivityLog(childId = ethanId, logType = "CHORE", message = "Ethan marked 'Tidy up bedroom toys' complete", severity = "INFO")
            )
            logsEthan.forEach { dao.insertLog(it) }

            // Seed logs for Maya
            val logsMaya = listOf(
                ActivityLog(childId = mayaId, logType = "SCREEN_TIME", message = "Maya hit screen limit budget of 90m", severity = "ALERT"),
                ActivityLog(childId = mayaId, logType = "GENERAL", message = "Parent clicked instant lock for Maya's device", severity = "ALERT"),
                ActivityLog(childId = mayaId, logType = "CHORE", message = "Parent approved finished chore 'Finish weekly Algebra unit' +$8 allowance awarded", severity = "INFO")
            )
            logsMaya.forEach { dao.insertLog(it) }
        }
    }
}
