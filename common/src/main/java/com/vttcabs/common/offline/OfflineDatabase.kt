package com.vttcabs.common.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedBooking::class,
        PendingGpsLocation::class,
        PendingAction::class,
        DriverOfflineStatus::class,
        SyncStatus::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    
    abstract fun offlineDao(): OfflineDao
    
    companion object {
        private const val DATABASE_NAME = "vtt_offline_db"
        
        @Volatile
        private var instance: OfflineDatabase? = null
        
        fun getInstance(context: Context): OfflineDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        
        private fun buildDatabase(context: Context): OfflineDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                OfflineDatabase::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
