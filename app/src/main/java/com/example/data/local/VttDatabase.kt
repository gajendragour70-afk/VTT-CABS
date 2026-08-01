package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.BookingEntity
import com.example.data.model.DriverEntity
import com.example.data.model.FareRuleEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DriverEntity::class,
        BookingEntity::class,
        NotificationEntity::class,
        FareRuleEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(VttTypeConverters::class)
abstract class VttDatabase : RoomDatabase() {
    abstract fun dao(): VttDao

    companion object {
        @Volatile
        private var INSTANCE: VttDatabase? = null

        fun getInstance(context: Context): VttDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VttDatabase::class.java,
                    "vtt_cabs.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
