package com.example.journalofdream.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.Location
import com.example.journalofdream.model.DreamLocationCrossRef
import com.example.journalofdream.model.Category
import com.example.journalofdream.model.CategoryDao

@Database(
    entities = [Dream::class, Location::class, DreamLocationCrossRef::class, Category::class],
    version = 1,
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
