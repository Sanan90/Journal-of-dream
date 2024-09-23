package com.example.journalofdream.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.journalofdream.model.*

@Database(
    entities = [Dream::class, Location::class, DreamLocationCrossRef::class, Category::class],
    version = 3, // Увеличьте версию базы данных
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dreamDao(): DreamDao
    abstract fun locationDao(): LocationDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dream_database"
                )
                    .fallbackToDestructiveMigration() // Если вы не хотите определять миграции
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
