package com.jlss.smartDairy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jlss.smartDairy.data.dao.*
import com.jlss.smartDairy.data.model.*

@Database(
    entities = [
        AppLockEntity::class,
        User::class,
        Members::class,
        Entry::class,
        Rates::class,
        ListOfEntry::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appLockDao(): AppLockDao
    abstract fun userDao(): UserDao
    abstract fun memberDao(): MemberDao
    abstract fun entryDao(): EntryDao
    abstract fun fatRateDao(): FatRateDao
    abstract fun listEntryDao(): ListEntryDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_dairy_db"
                )
                    .fallbackToDestructiveMigration() // ✅ important for dev
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
