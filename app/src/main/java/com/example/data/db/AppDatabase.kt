package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ClaimEntity
import com.example.data.model.ItemEntity
import com.example.data.model.MatchEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ItemEntity::class,
        MatchEntity::class,
        ClaimEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun matchDao(): MatchDao
    abstract fun claimDao(): ClaimDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "college_lost_found.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
