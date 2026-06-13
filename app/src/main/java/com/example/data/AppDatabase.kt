package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildMonitorDao {
    // Child Profiles
    @Query("SELECT * FROM child_profiles ORDER BY name ASC")
    fun getAllChildren(): Flow<List<ChildProfile>>

    @Query("SELECT * FROM child_profiles WHERE id = :id")
    fun getChildById(id: Long): Flow<ChildProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildProfile): Long

    @Update
    suspend fun updateChild(child: ChildProfile)

    @Delete
    suspend fun deleteChild(child: ChildProfile)

    // Chore Tasks
    @Query("SELECT * FROM chore_tasks WHERE childId = :childId ORDER BY timestamp DESC")
    fun getChoresForChild(childId: Long): Flow<List<ChoreTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChore(chore: ChoreTask)

    @Update
    suspend fun updateChore(chore: ChoreTask)

    @Delete
    suspend fun deleteChore(chore: ChoreTask)

    // Activity Logs
    @Query("SELECT * FROM activity_logs WHERE childId = :childId ORDER BY timestamp DESC LIMIT 60")
    fun getLogsForChild(childId: Long): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE childId = :childId")
    suspend fun clearLogsForChild(childId: Long)

    // App Controls
    @Query("SELECT * FROM app_controls WHERE childId = :childId ORDER BY appName ASC")
    fun getAppsForChild(childId: Long): Flow<List<AppControl>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppControl(app: AppControl)

    @Update
    suspend fun updateAppControl(app: AppControl)
}

@Database(
    entities = [ChildProfile::class, ChoreTask::class, ActivityLog::class, AppControl::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ChildMonitorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "child_monitor_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
